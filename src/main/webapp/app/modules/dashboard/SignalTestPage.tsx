import React, { useState } from 'react';
import { Button, Card, CardBody, CardHeader, Row, Col, Form, FormGroup, Label, Input, Alert } from 'reactstrap';
import './SignalTestPage.scss';

interface ResponseItem {
  id: number;
  message: string;
  isSuccess: boolean;
  timestamp: string;
}

const SignalTestPage = () => {
  const [responses, setResponses] = useState<ResponseItem[]>([
    { id: 1, message: 'Ready to test signals...', isSuccess: true, timestamp: new Date().toLocaleTimeString() },
  ]);

  const [customSignal, setCustomSignal] = useState({
    symbol: 'AAPL',
    price: 150.25,
    indicator: 'RSI',
    interval: '1h',
    direction: 'BUY',
    signalName: 'CUSTOM_SIGNAL',
    message: 'Custom test signal message',
  });

  const addResponse = (message: string, isSuccess = true) => {
    const newResponse: ResponseItem = {
      id: Date.now(),
      message,
      isSuccess,
      timestamp: new Date().toLocaleTimeString(),
    };

    setResponses(prev => [newResponse, ...prev.slice(0, 9)]); // Keep only 10 responses
  };

  const API_BASE = '/api/v1/testing';

  const simulateRandomSignal = async () => {
    try {
      const response = await fetch(`${API_BASE}/simulate-signal`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });

      const result = await response.json();

      if (response.ok) {
        const signal = result.signal;
        addResponse(`✅ Random signal created: ${signal.direction} ${signal.symbol} at $${signal.price} (${signal.indicator})`, true);
      } else {
        addResponse(`❌ Error: ${result.message}`, false);
      }
    } catch (error) {
      addResponse(`❌ Network error: ${error.message}`, false);
    }
  };

  const simulateBulkSignals = async () => {
    try {
      const response = await fetch(`${API_BASE}/simulate-bulk-signals?count=5`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });

      const result = await response.json();

      if (response.ok) {
        addResponse(`📊 Successfully created ${result.count} bulk signals`, true);
      } else {
        addResponse(`❌ Error: ${result.message}`, false);
      }
    } catch (error) {
      addResponse(`❌ Network error: ${error.message}`, false);
    }
  };

  const startAutoSimulation = async () => {
    try {
      const response = await fetch(`${API_BASE}/start-auto-simulation`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });

      const result = await response.json();

      if (response.ok) {
        addResponse(`⚡ Auto simulation started: ${result.message}`, true);
      } else {
        addResponse(`❌ Error: ${result.message}`, false);
      }
    } catch (error) {
      addResponse(`❌ Network error: ${error.message}`, false);
    }
  };

  const simulateCustomSignal = async () => {
    try {
      const response = await fetch(`${API_BASE}/simulate-specific-signal`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customSignal),
      });

      const result = await response.json();

      if (response.ok) {
        addResponse(
          `🎯 Custom signal created: ${customSignal.direction} ${customSignal.symbol} at $${customSignal.price} (${customSignal.indicator})`,
          true,
        );
      } else {
        addResponse(`❌ Error: ${result.message}`, false);
      }
    } catch (error) {
      addResponse(`❌ Network error: ${error.message}`, false);
    }
  };

  const clearResponses = () => {
    setResponses([
      { id: 1, message: 'Log cleared - Ready to test signals...', isSuccess: true, timestamp: new Date().toLocaleTimeString() },
    ]);
  };

  const handleInputChange = (field: string, value: any) => {
    setCustomSignal(prev => ({ ...prev, [field]: value }));
  };

  // Auto-update price when symbol changes
  const handleSymbolChange = (symbol: string) => {
    const prices = {
      AAPL: 150.25,
      GOOGL: 2750.8,
      TSLA: 220.15,
      MSFT: 380.9,
      AMZN: 3200.45,
      NVDA: 450.3,
      META: 320.75,
      'BTC-USD': 42150.0,
      'ETH-USD': 2500.0,
    };

    setCustomSignal(prev => ({
      ...prev,
      symbol,
      price: prices[symbol] || 100.0,
    }));
  };

  return (
    <div className="signal-test-page">
      <div className="container-fluid">
        <Row>
          <Col md="12">
            <Card>
              <CardHeader>
                <h3>🚀 Trading Signal Event Testing</h3>
                <p className="mb-0">Generate test signals to see real-time notifications in your dashboard</p>
              </CardHeader>
              <CardBody>
                {/* Action Buttons */}
                <Row className="mb-4">
                  <Col md="3" sm="6" className="mb-2">
                    <Button color="primary" onClick={simulateRandomSignal} block>
                      🎲 Random Signal
                    </Button>
                  </Col>
                  <Col md="3" sm="6" className="mb-2">
                    <Button color="success" onClick={simulateBulkSignals} block>
                      📊 Bulk Signals (5)
                    </Button>
                  </Col>
                  <Col md="3" sm="6" className="mb-2">
                    <Button color="warning" onClick={startAutoSimulation} block>
                      ⚡ Auto Simulation
                    </Button>
                  </Col>
                  <Col md="3" sm="6" className="mb-2">
                    <Button color="danger" onClick={clearResponses} block>
                      🗑️ Clear Log
                    </Button>
                  </Col>
                </Row>

                {/* Custom Signal Form */}
                <Card className="mb-4">
                  <CardHeader>
                    <h5>📝 Create Custom Signal</h5>
                  </CardHeader>
                  <CardBody>
                    <Form>
                      <Row>
                        <Col md="6">
                          <FormGroup>
                            <Label for="symbol">Symbol</Label>
                            <Input type="select" id="symbol" value={customSignal.symbol} onChange={e => handleSymbolChange(e.target.value)}>
                              <option value="AAPL">AAPL</option>
                              <option value="GOOGL">GOOGL</option>
                              <option value="TSLA">TSLA</option>
                              <option value="MSFT">MSFT</option>
                              <option value="AMZN">AMZN</option>
                              <option value="NVDA">NVDA</option>
                              <option value="META">META</option>
                              <option value="BTC-USD">BTC-USD</option>
                              <option value="ETH-USD">ETH-USD</option>
                            </Input>
                          </FormGroup>
                        </Col>
                        <Col md="6">
                          <FormGroup>
                            <Label for="price">Price</Label>
                            <Input
                              type="number"
                              id="price"
                              step="0.01"
                              value={customSignal.price}
                              onChange={e => handleInputChange('price', parseFloat(e.target.value))}
                            />
                          </FormGroup>
                        </Col>
                      </Row>

                      <Row>
                        <Col md="6">
                          <FormGroup>
                            <Label for="indicator">Indicator</Label>
                            <Input
                              type="select"
                              id="indicator"
                              value={customSignal.indicator}
                              onChange={e => handleInputChange('indicator', e.target.value)}
                            >
                              <option value="RSI">RSI</option>
                              <option value="MACD">MACD</option>
                              <option value="Bollinger Bands">Bollinger Bands</option>
                              <option value="EMA">EMA</option>
                              <option value="Support/Resistance">Support/Resistance</option>
                              <option value="Volume">Volume</option>
                              <option value="Stochastic">Stochastic</option>
                            </Input>
                          </FormGroup>
                        </Col>
                        <Col md="6">
                          <FormGroup>
                            <Label for="interval">Interval</Label>
                            <Input
                              type="select"
                              id="interval"
                              value={customSignal.interval}
                              onChange={e => handleInputChange('interval', e.target.value)}
                            >
                              <option value="15m">15m</option>
                              <option value="30m">30m</option>
                              <option value="1h">1h</option>
                              <option value="2h">2h</option>
                              <option value="4h">4h</option>
                              <option value="6h">6h</option>
                              <option value="1d">1d</option>
                            </Input>
                          </FormGroup>
                        </Col>
                      </Row>

                      <Row>
                        <Col md="6">
                          <FormGroup>
                            <Label for="direction">Direction</Label>
                            <Input
                              type="select"
                              id="direction"
                              value={customSignal.direction}
                              onChange={e => handleInputChange('direction', e.target.value)}
                            >
                              <option value="BUY">🟢 BUY</option>
                              <option value="SELL">🔴 SELL</option>
                              <option value="HOLD">🟡 HOLD</option>
                            </Input>
                          </FormGroup>
                        </Col>
                        <Col md="6">
                          <FormGroup>
                            <Label for="signalName">Signal Name</Label>
                            <Input
                              type="text"
                              id="signalName"
                              value={customSignal.signalName}
                              onChange={e => handleInputChange('signalName', e.target.value)}
                            />
                          </FormGroup>
                        </Col>
                      </Row>

                      <FormGroup>
                        <Label for="message">Message</Label>
                        <Input
                          type="text"
                          id="message"
                          value={customSignal.message}
                          onChange={e => handleInputChange('message', e.target.value)}
                        />
                      </FormGroup>

                      <Button color="primary" onClick={simulateCustomSignal} block>
                        🎯 Create Custom Signal
                      </Button>
                    </Form>
                  </CardBody>
                </Card>

                {/* Response Log */}
                <Card>
                  <CardHeader>
                    <h5>📝 Response Log</h5>
                  </CardHeader>
                  <CardBody className="response-log">
                    {responses.map(response => (
                      <Alert key={response.id} color={response.isSuccess ? 'success' : 'danger'} className="mb-2">
                        <small className="text-muted">{response.timestamp}</small>
                        <div>{response.message}</div>
                      </Alert>
                    ))}
                  </CardBody>
                </Card>
              </CardBody>
            </Card>
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default SignalTestPage;
