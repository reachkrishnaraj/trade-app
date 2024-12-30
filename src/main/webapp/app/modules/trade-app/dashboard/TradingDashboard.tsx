import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Row, Col, Card, Nav, Tab, Table } from 'react-bootstrap';

const TradingDashboard = () => {
  // Sample data structures - replace with your actual data
  const [currentTrades, setCurrentTrades] = useState([]);
  const [events, setEvents] = useState({
    '1m': [],
    '3m': [],
    '5m': [],
    '15m': [],
    '30m': [],
    '1hr': [],
    '4hr': [],
  });
  const [signals, setSignals] = useState([]);

  // Component for current trades table
  const CurrentTradesTable = () => (
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
        {currentTrades.map((trade, index) => (
          <tr key={index}>
            <td>{trade.accountName}</td>
            <td>{trade.id}</td>
            <td className={trade.openPnL >= 0 ? 'text-success' : 'text-danger'}>
              {trade.openPnL > 0 ? '+' : ''}
              {trade.openPnL}
            </td>
            <td>{trade.openTime}</td>
            <td>
              <span
                className={`badge ${trade.status === 'Active' ? 'bg-success' : trade.status === 'Pending' ? 'bg-warning' : 'bg-secondary'}`}
              >
                {trade.status}
              </span>
            </td>
          </tr>
        ))}
        {currentTrades.length === 0 && (
          <tr>
            <td colSpan={5} className="text-center">
              No active trades
            </td>
          </tr>
        )}
      </tbody>
    </Table>
  );

  // Component for rendering event tables
  const EventTable = ({ data }) => (
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
        {data.map((event, index) => (
          <tr key={index}>
            <td>{event.indicatorName}</td>
            <td style={{ maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{event.rawMessage}</td>
            <td>{event.datetime}</td>
            <td>{event.signal}</td>
            <td>{event.symbol}</td>
          </tr>
        ))}
        {data.length === 0 && (
          <tr>
            <td colSpan={5} className="text-center">
              No events found
            </td>
          </tr>
        )}
      </tbody>
    </Table>
  );

  // Component for signals table
  const SignalsTable = () => (
    <Table responsive striped bordered hover>
      <thead>
        <tr>
          <th>Signal</th>
          <th>Source</th>
          <th>DateTime</th>
        </tr>
      </thead>
      <tbody>
        {signals.map((signal, index) => (
          <tr key={index}>
            <td>{signal.signal}</td>
            <td>{signal.source}</td>
            <td>{signal.datetime}</td>
          </tr>
        ))}
        {signals.length === 0 && (
          <tr>
            <td colSpan={3} className="text-center">
              No signals found
            </td>
          </tr>
        )}
      </tbody>
    </Table>
  );

  return (
    <Container fluid className="py-4">
      {/* Current Trades Section */}
      <Row className="mb-4">
        <Col>
          <Card>
            <Card.Header className="bg-primary text-white">
              <Card.Title className="mb-0">Current Trades</Card.Title>
            </Card.Header>
            <Card.Body>
              <CurrentTradesTable />
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Signals Section */}
      <Row className="mb-4">
        <Col>
          <Card>
            <Card.Header>
              <Card.Title>Signals</Card.Title>
            </Card.Header>
            <Card.Body>
              <SignalsTable />
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Events Section */}
      <Row>
        <Col>
          <Card>
            <Card.Header>
              <Card.Title>Events by Timeframe</Card.Title>
            </Card.Header>
            <Card.Body>
              <Tab.Container defaultActiveKey="1m">
                <Nav variant="tabs" className="mb-3">
                  {Object.keys(events).map(interval => (
                    <Nav.Item key={interval}>
                      <Nav.Link eventKey={interval}>{interval}</Nav.Link>
                    </Nav.Item>
                  ))}
                </Nav>
                <Tab.Content>
                  {Object.entries(events).map(([interval, data]) => (
                    <Tab.Pane key={interval} eventKey={interval}>
                      <EventTable data={data} />
                    </Tab.Pane>
                  ))}
                </Tab.Content>
              </Tab.Container>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default TradingDashboard;
