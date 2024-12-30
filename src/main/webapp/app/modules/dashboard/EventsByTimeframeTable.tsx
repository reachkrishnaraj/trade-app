import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import { Container, Row, Col, Card, Table, Spinner, Form, Button, Nav, Tab } from 'react-bootstrap';
import { Subscription } from 'rxjs';
import { connectWebSocket, disconnectWebSocket, subscribeToTopic, unsubscribeFromTopic } from 'app/utils/websocket-utils';

const EventsByTimeframeTable = () => {
  const [data, setData] = useState([]);
  const [filter, setFilter] = useState(Storage.local.get('filter') || '5m');
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
      const newData = event;

      setData(prevData => [...newData, ...prevData]);

      // Trigger the updated indicator
      setUpdated(true);
      setTimeout(() => setUpdated(false), 1000);
    });

    return () => {
      unsubscribeFromTopic('/topic/events');
      disconnectWebSocket();
      subscription.unsubscribe();
    };
  }, []);

  useEffect(() => {
    Storage.local.set('filter', filter);
  }, [filter]);

  const handleFilterChange = event => {
    setFilter(event.target.value);
  };

  const getFilteredData = () => {
    const now = new Date();
    const filteredData = data.filter(event => {
      const eventTime = new Date(event.dateTime);
      switch (filter) {
        case '1m':
          return now.getTime() - eventTime.getTime() <= 1 * 60 * 1000;
        case '3m':
          return now.getTime() - eventTime.getTime() <= 3 * 60 * 1000;
        case '5m':
          return now.getTime() - eventTime.getTime() <= 5 * 60 * 1000;
        case '15m':
          return now.getTime() - eventTime.getTime() <= 15 * 60 * 1000;
        case '30m':
          return now.getTime() - eventTime.getTime() <= 30 * 60 * 1000;
        case '1hr':
          return now.getTime() - eventTime.getTime() <= 60 * 60 * 1000;
        case '4hr':
          return now.getTime() - eventTime.getTime() <= 4 * 60 * 60 * 1000;
        case '24hr':
          return now.getTime() - eventTime.getTime() <= 24 * 60 * 60 * 1000;
        case 'all':
          return true;
        default:
          return now.getTime() - eventTime.getTime() <= 5 * 60 * 1000;
      }
    });
    return filteredData;
  };

  const toggleMinimize = () => {
    setMinimized(!minimized);
  };

  return (
    <Container fluid className="py-4">
      <Row className="mb-4">
        <Col>
          <Card>
            <Card.Header>
              <div className="d-flex justify-content-between align-items-center">
                <Tab.Container defaultActiveKey={filter}>
                  <Nav variant="tabs" className="mb-3">
                    <Nav.Item>
                      <Nav.Link eventKey="1m" onClick={() => setFilter('1m')}>
                        1m
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="3m" onClick={() => setFilter('3m')}>
                        3m
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="5m" onClick={() => setFilter('5m')}>
                        5m
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="15m" onClick={() => setFilter('15m')}>
                        15m
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="30m" onClick={() => setFilter('30m')}>
                        30m
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="1hr" onClick={() => setFilter('1hr')}>
                        1hr
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="4hr" onClick={() => setFilter('4hr')}>
                        4hr
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="24hr" onClick={() => setFilter('24hr')}>
                        24hr
                      </Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                      <Nav.Link eventKey="all" onClick={() => setFilter('all')}>
                        all
                      </Nav.Link>
                    </Nav.Item>
                  </Nav>
                </Tab.Container>
                <Button variant="link" onClick={toggleMinimize}>
                  {minimized ? '▼' : '▲'}
                </Button>
              </div>
            </Card.Header>
            <Card.Body>
              {loading ? (
                <div className="text-center">
                  <Spinner animation="border" />
                </div>
              ) : (
                !minimized && (
                  <Table responsive striped bordered hover>
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
                          <td className="text-truncate" style={{ maxWidth: '300px' }}>
                            {event.rawMessage}
                          </td>
                          <td>{new Date(event.dateTime).toLocaleString()}</td>
                          <td className={`signal ${event.signal.toLowerCase()}`}>{event.signal}</td>
                          <td>{event.symbol}</td>
                        </tr>
                      ))}
                      {getFilteredData().length === 0 && (
                        <tr>
                          <td colSpan={5} className="text-center">
                            No events found
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </Table>
                )
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default EventsByTimeframeTable;
