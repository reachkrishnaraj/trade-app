import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import { Table, Card, Button } from 'react-bootstrap';
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
      const newData = event;

      setData(prevData => [...newData, ...prevData]);

      // Trigger the updated indicator
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
          </Table>
        </Card.Body>
      )}
    </Card>
  );
};

export default SignalsTable;
