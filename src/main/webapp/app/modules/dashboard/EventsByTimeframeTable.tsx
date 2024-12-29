import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import './eventsByTimeframeTable.scss';
import { Subscription } from 'rxjs';
import { connectWebSocket, disconnectWebSocket, subscribeToTopic, unsubscribeFromTopic } from 'app/utils/websocket-utils';

const EventsByTimeframeTable = () => {
  const [data, setData] = useState([]);
  const [filter, setFilter] = useState('last 5m');
  const [loading, setLoading] = useState(true);
  const [updated, setUpdated] = useState(false);
  const [minimized, setMinimized] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
      const response = await fetch('/api/v1/dashboard/events', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      const result = await response.json();
      setData(result);
      setLoading(false);
    };
    fetchData();

    connectWebSocket();
    const subscription: Subscription = subscribeToTopic('/topic/events').subscribe(event => {
      setData(event);
      setUpdated(true);
      setTimeout(() => setUpdated(false), 1000);
    });

    return () => {
      unsubscribeFromTopic('/topic/events');
      disconnectWebSocket();
      subscription.unsubscribe();
    };
  }, []);

  const handleFilterChange = event => {
    setFilter(event.target.value);
  };

  const getFilteredData = () => {
    const now = new Date();
    const filteredData = data.filter(event => {
      const eventTime = new Date(event.dateTime);
      switch (filter) {
        case 'last 1m':
          return now.getTime() - eventTime.getTime() <= 1 * 60 * 1000;
        case 'last 3m':
          return now.getTime() - eventTime.getTime() <= 3 * 60 * 1000;
        case 'last 5m':
          return now.getTime() - eventTime.getTime() <= 5 * 60 * 1000;
        case 'last 15m':
          return now.getTime() - eventTime.getTime() <= 15 * 60 * 1000;
        case 'last 30m':
          return now.getTime() - eventTime.getTime() <= 30 * 60 * 1000;
        case 'last 1hr':
          return now.getTime() - eventTime.getTime() <= 60 * 60 * 1000;
        case 'last 4hr':
          return now.getTime() - eventTime.getTime() <= 4 * 60 * 60 * 1000;
        case 'last 24hr':
          return now.getTime() - eventTime.getTime() <= 24 * 60 * 60 * 1000;
        default:
          return true;
      }
    });
    return filteredData;
  };

  const toggleMinimize = () => {
    setMinimized(!minimized);
  };

  return (
    <div className={`table-container ${loading ? 'loading' : ''} ${updated ? 'updated' : ''}`}>
      <div className="table-header">
        <h3>Events by Timeframe</h3>
        <select value={filter} onChange={handleFilterChange} className="filter-select">
          <option value="last 1m">Last 1m</option>
          <option value="last 3m">Last 3m</option>
          <option value="last 5m">Last 5m</option>
          <option value="last 15m">Last 15m</option>
          <option value="last 30m">Last 30m</option>
          <option value="last 1hr">Last 1hr</option>
          <option value="last 4hr">Last 4hr</option>
          <option value="last 24hr">Last 24hr</option>
        </select>
        <button onClick={toggleMinimize} className="minimize-button">
          {minimized ? '▼' : '▲'}
        </button>
      </div>
      {!minimized && (
        <table>
          <thead>
            <tr>
              <th>Indicator Name</th>
              <th>Raw Message</th>
              <th>DateTime</th>
              <th>Signal</th>
              <th>Symbol</th>
            </tr>
          </thead>
          <tbody>
            {getFilteredData().map((event, index) => (
              <tr key={index}>
                <td>{event.indicatorName}</td>
                <td>{event.rawMessage}</td>
                <td>{new Date(event.dateTime).toLocaleString()}</td>
                <td className={`signal ${event.signal.toLowerCase()}`}>{event.signal}</td>
                <td>{event.symbol}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default EventsByTimeframeTable;
