import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Storage } from 'react-jhipster';
import { Stomp } from '@stomp/stompjs';
import { Card, Form, Badge } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowUp, faArrowDown, faExclamationCircle, faChevronUp, faChevronDown } from '@fortawesome/free-solid-svg-icons';
import {
  TradeSignalScoreSnapshot,
  CandleIntervalGroupedRecord,
  IndicatorScoreRecord,
  IndicatorSubCategoryScoreRecord,
} from 'app/shared/model/trade-app-module';

interface IndicatorMainTableProps {
  symbol: string;
  wsUrl: string;
}

interface FilterOption {
  value: string;
  label: string;
}

interface ExpandedSections {
  [key: string]: boolean;
}

const IndicatorMainTableV2: React.FC<IndicatorMainTableProps> = ({ symbol, wsUrl }) => {
  const [data, setData] = useState<TradeSignalScoreSnapshot | null>(null);
  const [expandedSections, setExpandedSections] = useState<ExpandedSections>({});

  // Filters
  const [selectedCandleType, setSelectedCandleType] = useState<string>('all');
  const [selectedInterval, setSelectedInterval] = useState<string>('all');
  const [selectedIndicator, setSelectedIndicator] = useState<string>('all');
  const [selectedSubCategory, setSelectedSubCategory] = useState<string>('all');

  // Filter Options
  const [candleTypeOptions, setCandleTypeOptions] = useState<FilterOption[]>([{ value: 'all', label: 'All Candle Types' }]);
  const [intervalOptions, setIntervalOptions] = useState<FilterOption[]>([{ value: 'all', label: 'All Intervals' }]);
  const [indicatorOptions, setIndicatorOptions] = useState<FilterOption[]>([{ value: 'all', label: 'All Indicators' }]);
  const [subCategoryOptions, setSubCategoryOptions] = useState<FilterOption[]>([{ value: 'all', label: 'All Sub-Categories' }]);

  useEffect(() => {
    if (!symbol) {
      console.log('No symbol provided');
      return;
    }

    const sock = new SockJS(wsUrl);
    const stompClient = Stomp.over(sock);

    const fetchInitialData = async () => {
      try {
        const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
        const response = await window.fetch(`/api/v1/dashboard/signal-snapshot/${symbol}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        const responseData = await response.json();
        setData(responseData);
      } catch (error) {
        console.error('Error fetching initial data:', error);
      }
    };

    fetchInitialData();

    stompClient.connect({}, () => {
      stompClient.subscribe(`/topic/signal-snapshot/${symbol}`, message => {
        const parsedData = JSON.parse(message.body);
        setData(parsedData);
      });
    });

    return () => {
      stompClient.disconnect();
    };
  }, [symbol, wsUrl]);

  useEffect(() => {
    if (!data) return;

    // Extract unique candle types and intervals
    const candleTypes = new Set<string>();
    const intervals = new Set<string>();
    const indicators = new Set<string>();
    const subCategories = new Set<string>();

    data?.candleIntervalGroupedRecords?.forEach(record => {
      candleTypes.add(record.candleType);
      intervals.add(record.interval);

      record.indicatorScoreRecords.forEach(indicator => {
        indicators.add(indicator.name);

        indicator.subCategoryScores.forEach(sub => {
          subCategories.add(sub.name);
        });
      });
    });

    setCandleTypeOptions([
      { value: 'all', label: 'All Candle Types' },
      ...Array.from(candleTypes).map(type => ({ value: type, label: type })),
    ]);

    setIntervalOptions([
      { value: 'all', label: 'All Intervals' },
      ...Array.from(intervals).map(interval => ({ value: interval, label: interval })),
    ]);

    setIndicatorOptions([{ value: 'all', label: 'All Indicators' }, ...Array.from(indicators).map(name => ({ value: name, label: name }))]);

    setSubCategoryOptions([
      { value: 'all', label: 'All Sub-Categories' },
      ...Array.from(subCategories).map(name => ({ value: name, label: name })),
    ]);
  }, [data]);

  const getScoreColor = (score: string, minScore: string, maxScore: string): string => {
    const normalizedScore = Math.abs((Number(score) / Math.max(Math.abs(Number(minScore)), Math.abs(Number(maxScore)))) * 100);

    if (normalizedScore >= 90) {
      return Number(score) >= 0 ? 'success' : 'danger';
    } else if (normalizedScore >= 60) {
      return Number(score) >= 0 ? 'info' : 'warning';
    } else {
      return 'secondary';
    }
  };

  const DirectionIcon = ({ direction }: { direction: string }) => {
    if (!direction) return <FontAwesomeIcon icon={faExclamationCircle} className="text-warning" />;
    if (direction.includes('BULL')) {
      return <FontAwesomeIcon icon={faArrowUp} className="text-success" />;
    }
    if (direction.includes('BEAR')) {
      return <FontAwesomeIcon icon={faArrowDown} className="text-danger" />;
    }
    return <FontAwesomeIcon icon={faExclamationCircle} className="text-warning" />;
  };

  const toggleSection = (key: string) => {
    setExpandedSections(prev => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const filterCandleIntervalRecords = (records: CandleIntervalGroupedRecord[]): CandleIntervalGroupedRecord[] => {
    return (
      records?.filter(
        record =>
          (selectedCandleType === 'all' || record.candleType === selectedCandleType) &&
          (selectedInterval === 'all' || record.interval === selectedInterval),
      ) || []
    );
  };

  const filterIndicators = (records: IndicatorScoreRecord[]): IndicatorScoreRecord[] => {
    return records?.filter(indicator => selectedIndicator === 'all' || indicator.name === selectedIndicator) || [];
  };

  const filterSubCategories = (subCategories: IndicatorSubCategoryScoreRecord[]): IndicatorSubCategoryScoreRecord[] => {
    return subCategories?.filter(sub => selectedSubCategory === 'all' || sub.name === selectedSubCategory) || [];
  };

  if (!data) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '200px' }}>
        <div className="h4 text-muted">Loading...</div>
      </div>
    );
  }

  return (
    <div className="mb-4">
      {/* Main Filters */}
      <Card className="mb-3">
        <Card.Body>
          <div className="row">
            <div className="col-md-3">
              <Form.Group>
                <Form.Label>Candle Type</Form.Label>
                <Form.Select value={selectedCandleType} onChange={e => setSelectedCandleType(e.target.value)}>
                  {candleTypeOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </div>
            <div className="col-md-3">
              <Form.Group>
                <Form.Label>Interval</Form.Label>
                <Form.Select value={selectedInterval} onChange={e => setSelectedInterval(e.target.value)}>
                  {intervalOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </div>
            <div className="col-md-3">
              <Form.Group>
                <Form.Label>Indicator</Form.Label>
                <Form.Select value={selectedIndicator} onChange={e => setSelectedIndicator(e.target.value)}>
                  {indicatorOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </div>
            <div className="col-md-3">
              <Form.Group>
                <Form.Label>Sub-Category</Form.Label>
                <Form.Select value={selectedSubCategory} onChange={e => setSelectedSubCategory(e.target.value)}>
                  {subCategoryOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </div>
          </div>
        </Card.Body>
      </Card>

      {/* Grouped Records */}
      {filterCandleIntervalRecords(data.candleIntervalGroupedRecords).map(groupRecord => (
        <Card key={groupRecord.key} className="mb-3">
          <Card.Header>
            <div className="d-flex justify-content-between align-items-center">
              <div className="d-flex align-items-center gap-2">
                <Badge bg="secondary">{groupRecord.candleType}</Badge>
                <Badge bg="info">{groupRecord.interval}</Badge>
                <Badge bg="info">{groupRecord.direction}</Badge>
                <span className={`text-${getScoreColor(groupRecord.score, groupRecord.minScore, groupRecord.maxScore)}`}>
                  Score: {groupRecord.score}, {groupRecord.scorePercentage}
                </span>
              </div>
              <DirectionIcon direction={groupRecord.direction} />
            </div>
          </Card.Header>
          <Card.Body>
            {filterIndicators(groupRecord.indicatorScoreRecords).map(indicator => (
              <Card key={indicator.key} className="mb-3">
                <Card.Header className="cursor-pointer" onClick={() => toggleSection(indicator.key)} style={{ cursor: 'pointer' }}>
                  <div className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">{indicator.displayName}</h5>
                    <div className="d-flex align-items-center gap-3">
                      <span className={`text-${getScoreColor(indicator.score, indicator.minScore, indicator.maxScore)}`}>
                        Score: {indicator.score}, {indicator.scorePercentage}
                      </span>
                      <DirectionIcon direction={indicator.direction} />
                      <FontAwesomeIcon icon={expandedSections[indicator.key] ? faChevronUp : faChevronDown} />
                    </div>
                  </div>
                </Card.Header>
                {expandedSections[indicator.key] && (
                  <Card.Body>
                    <div className="row mb-3">
                      <div className="col">
                        <div className="text-muted">Last Message</div>
                        <div>{indicator.lastMsg}</div>
                      </div>
                    </div>
                    {filterSubCategories(indicator.subCategoryScores).map(subScore => (
                      <div key={subScore.key} className="border-top pt-3">
                        <div className="row">
                          <div className="col-md-4">
                            <div className="fw-bold">{subScore.displayName}</div>
                            <div className={`text-${getScoreColor(subScore.score, subScore.minScore, subScore.maxScore)}`}>
                              Score: {subScore.score}, {subScore.scorePercentage}
                            </div>
                          </div>
                          <div className="col-md-4">
                            <div className="d-flex align-items-center gap-2">
                              <DirectionIcon direction={subScore.direction} />
                              <span>{subScore.direction}</span>
                            </div>
                          </div>
                          <div className="col-md-4">
                            <div>
                              <div className="text-muted small">{subScore.lastMsg}</div>
                              <div className="text-muted smaller">
                                {subScore.lastMsgDateTime ? new Date(subScore.lastMsgDateTime).toLocaleString() : 'N/A'}
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </Card.Body>
                )}
              </Card>
            ))}
          </Card.Body>
        </Card>
      ))}
    </div>
  );
};

export default IndicatorMainTableV2;
