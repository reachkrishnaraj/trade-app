import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import { Table, Card, Button } from 'react-bootstrap';
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
      const newData = event;

      setData(prevData => [...newData, ...prevData]);

      // Trigger the updated indicator
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
    <Card className={`table-container ${loading ? 'loading' : ''} ${updated ? 'updated' : ''}`}>
      <Card.Header className="d-flex justify-content-between align-items-center">
        <Button onClick={toggleMinimize} variant="outline-secondary" size="sm">
          {minimized ? '▼' : '▲'}
        </Button>
      </Card.Header>
      {!minimized && (
        <Card.Body>
          <Table responsive striped bordered hover>
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
          </Table>
        </Card.Body>
      )}
    </Card>
  );
};

export default CurrentTradesTable;
