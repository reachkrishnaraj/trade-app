import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import './signalsTable.scss';
import { Subscription } from 'rxjs';
import { connectWebSocket, disconnectWebSocket, subscribeToTopic, unsubscribeFromTopic } from 'app/utils/websocket-utils';

const SignalsTable = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updated, setUpdated] = useState(false);
  const [minimized, setMinimized] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
      const response = await fetch('/api/v1/dashboard/trading-signals', {
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
    const subscription: Subscription = subscribeToTopic('/topic/trading-signals').subscribe(event => {
      setData(event);
      setUpdated(true);
      setTimeout(() => setUpdated(false), 1000);
    });

    return () => {
      unsubscribeFromTopic('/topic/trading-signals');
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
        <h3>Trading Signals</h3>
        <button onClick={toggleMinimize} className="minimize-button">
          {minimized ? '▼' : '▲'}
        </button>
      </div>
      {!minimized && (
        <table>
          <thead>
            <tr>
              <th>Signal</th>
              <th>Source</th>
              <th>DateTime</th>
            </tr>
          </thead>
          <tbody>
            {data.map((signal, index) => (
              <tr key={index}>
                <td>{signal.signal}</td>
                <td>{signal.source}</td>
                <td>{new Date(signal.dateTime).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default SignalsTable;
