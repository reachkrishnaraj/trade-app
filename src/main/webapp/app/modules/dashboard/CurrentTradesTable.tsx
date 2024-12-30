import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import './currentTradesTable.scss';
import { Subscription } from 'rxjs';
import { connectWebSocket, disconnectWebSocket, subscribeToTopic, unsubscribeFromTopic } from 'app/utils/websocket-utils';

const CurrentTradesTable = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updated, setUpdated] = useState(false);
  const [minimized, setMinimized] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
      const response = await fetch('/api/v1/dashboard/current-trades', {
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
    const subscription: Subscription = subscribeToTopic('/topic/current-trades').subscribe(event => {
      setData(event);
      setUpdated(true);
      setTimeout(() => setUpdated(false), 1000);
    });

    return () => {
      unsubscribeFromTopic('/topic/current-trades');
      disconnectWebSocket();
      subscription.unsubscribe();
    };
  }, []);

  const toggleMinimize = () => {
    setMinimized(!minimized);
  };

  return (
    <div className={`table-container ${loading ? 'loading' : ''} ${updated ? 'updated' : ''}`}>
      <div className="table-header">
        <h3>Current Trades</h3>
        <button onClick={toggleMinimize} className="minimize-button">
          {minimized ? '▼' : '▲'}
        </button>
      </div>
      {!minimized && (
        <table>
          <thead>
            <tr>
              <th>Account Name</th>
              <th>ID</th>
              <th>Open PnL</th>
              <th>Trade Open Time</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {data.map((trade, index) => (
              <tr key={index}>
                <td>{trade.accountName}</td>
                <td>{trade.id}</td>
                <td className={trade.openPnL >= 0 ? 'positive' : 'negative'}>
                  {trade.openPnL >= 0 ? `+$${trade.openPnL}` : `-$${Math.abs(trade.openPnL)}`}
                </td>
                <td>{new Date(trade.tradeOpenTime).toLocaleString()}</td>
                <td>{trade.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default CurrentTradesTable;
