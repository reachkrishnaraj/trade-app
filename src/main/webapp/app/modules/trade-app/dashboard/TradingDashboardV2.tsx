import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, Nav, Tab } from 'react-bootstrap';
import { Event, TradeSignal, AccTrade, EventsByTimeInterval } from 'app/shared/model/trade-app-module';
import SockJS from 'sockjs-client';
import { Storage } from 'react-jhipster';
import { Stomp } from '@stomp/stompjs';
import EventTable from './EventTable';
import EventTableV2 from './EventTableV2';
import AccTradesTable from './AccTradesTable';
import IndicatorMainTableV2 from './IndicatorMainTableV2';
import SignalsTable from './SignalsTable';
import { useParams } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';

const WS_URL = 'https://trade-app-production.up.railway.app/websocket/tracker';

const TradingDashboardV2: React.FC = () => {
  const { symbol } = useParams<{ symbol: string }>(); // Extract symbol from URL

  // Initialize state with sample data (replace with your actual data source)
  const [currentTrades, setCurrentTrades] = useState<AccTrade[]>([
    {
      accountName: 'Main Account',
      symbol: 'BTCUSDT',
      direction: 'LONG',
      status: 'ACTIVE',
      openedDatetime: '2024-12-31 10:00:00',
      price: '100',
    },
  ]);

  const [signals, setSignals] = useState<TradeSignal[]>([
    {
      strategy: 'Trend Following',
      signal: 'BUY',
      createdTs: '2024-12-31 10:00:00',
      symbol: 'BTCUSDT',
      indicator: 'RSI',
      direction: 'LONG',
      status: 'ACTIVE',
      price: '100',
      sinceCreatedStr: '1 minute ago',
      source: 'TradingView',
    },
  ]);

  const [events, setEvents] = useState<Event[]>();
  const [eventsByInterval, setEventsByInterval] = useState<EventsByTimeInterval>({});
  const [mainData, setMainData] = useState({
    symbol: symbol,
  });

  useEffect(() => {
    console.log('Symbol is 2:', symbol);
    if (!symbol) return;

    setMainData(prev => ({
      ...prev,
      symbol: symbol,
    }));

    const sock = new SockJS(WS_URL);
    const stompClient = Stomp.over(sock);

    // Fetch initial state from REST API
    const fetchInitialData = async () => {
      try {
        const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
        const response = await fetch(`/api/v1/dashboard/events/${symbol}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        const eventsRes = await response.json();
        setEvents(eventsRes);
      } catch (error) {
        console.error('Error fetching initial data:', error);
      }
    };
    fetchInitialData();

    stompClient.connect({}, () => {
      // Subscribe to symbol-specific topics
      //               stompClient.subscribe(`/topic/trades/${symbol}`, (message) => {
      //                   setCurrentTrades(JSON.parse(message.body));
      //               });
      //
      //               stompClient.subscribe(`/topic/signals/${symbol}`, (message) => {
      //                   setSignals(JSON.parse(message.body));
      //               });
      stompClient.subscribe(`/topic/events/${symbol}`, message => {
        setEvents(JSON.parse(message.body));
      });
    });

    return () => {
      stompClient.disconnect();
    };
  }, [symbol]);

  useEffect(() => {
    //slice events by time interval
    const eventsByTimeframe: EventsByTimeInterval = {};
    events?.forEach(event => {
      const interval = event.interval;
      if (!eventsByTimeframe[interval]) {
        eventsByTimeframe[interval] = [];
      }
      eventsByTimeframe[interval].push(event);
    });
    console.log('Events By Timeframe:', JSON.stringify(events));
    //filter for strategy events using isStrategy flag
    const strategyEvents = events?.filter(event => event.strategy);
    console.log('Strategy Events length:', JSON.stringify(strategyEvents));
    eventsByTimeframe['Strategy'] = strategyEvents || [];
    setEventsByInterval(eventsByTimeframe);
  }, [events]);

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
              <AccTradesTable trades={currentTrades} />
            </Card.Body>
          </Card>
        </Col>
      </Row>
      {/* Signals Section */}
      <Row className="mb-4">
        <Col>
          <Card>
            <Card.Header>
              <Card.Title>Trading Signals</Card.Title>
            </Card.Header>
            <Card.Body>
              <SignalsTable signals={signals} />
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
                  {Object.keys(eventsByInterval).map(interval => (
                    <Nav.Item key={interval}>
                      <Nav.Link eventKey={interval}>{interval}</Nav.Link>
                    </Nav.Item>
                  ))}
                </Nav>
                <Tab.Content>
                  {Object.entries(eventsByInterval).map(([interval, data]) => (
                    <Tab.Pane key={interval} eventKey={interval}>
                      <EventTableV2 data={data} />
                    </Tab.Pane>
                  ))}
                </Tab.Content>
              </Tab.Container>
            </Card.Body>
          </Card>
        </Col>
      </Row>
      Indicator Section
      <Row className="mb-4">
        <Col>
          <Card>
            <Card.Header className="bg-primary text-white">
              <Card.Title className="mb-0">Indicator Analysis</Card.Title>
            </Card.Header>
            <Card.Body>
              <IndicatorMainTableV2 symbol={symbol} wsUrl={WS_URL} />
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default TradingDashboardV2;
