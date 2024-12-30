import React from 'react';
import CurrentTradesTable from './CurrentTradesTable';
import SignalsTable from './SignalsTable';
import EventsByTimeframeTable from './EventsByTimeframeTable';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Row, Col, Card } from 'react-bootstrap';

const Dashboard = () => {
  return (
    <Container fluid className="py-4">
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

      <Row>
        <Col>
          <Card>
            <Card.Header>
              <Card.Title>Events by Timeframe</Card.Title>
            </Card.Header>
            <Card.Body>
              <EventsByTimeframeTable />
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Dashboard;
