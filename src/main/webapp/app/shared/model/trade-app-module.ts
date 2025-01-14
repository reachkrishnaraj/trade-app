export interface Event {
  id: number;
  datetime: string; // LocalDateTime
  symbol: string;
  source: string;
  indicator: string;
  indicatorDisplayName: string;
  direction: string;
  indicatorSubCategory: string;
  indicatorSubCategoryDisplayName: string;
  rawAlertMsg: string;
  rawPayload: string;
  price: string; // BigDecimal
  interval: string;
  candleType: string;
  created: string; // LocalDateTime
  lastUpdated: string; // LocalDateTime
  tradeSignalProcessStatus: string;
  score: string; // BigDecimal
  minScore: string; // BigDecimal
  maxScore: string; // BigDecimal
  scorePercent: string; // BigDecimal
  isStrategy: boolean;
  strategyName: string | null;
  strategyProcessStatus: string;
  strategyProcessedAt: string | null; // Optional LocalDateTime
  strategyProcessMsg: string | null;
  isAlertable: boolean;
}

export interface TradeSignal {
  indicator: string;
  symbol: string;
  direction: string;
  status: string;
  signal: string;
  createdTs: string;
  price: string;
  source: string;
  strategy: string;
  sinceCreatedStr: string;
}

export interface AccTrade {
  accountName: string;
  symbol: string;
  direction: string;
  status: string;
  price: string;
  openedDatetime: string;
  closedDatetime?: string;
}

export interface EventsByTimeInterval {
  [key: string]: Event[];
}

export interface IndicatorSubCategoryScoreRecord {
  key: string;
  symbol: string;
  candleType: string;
  interval: string;
  indicatorName: string;
  indicatorDisplayName: string;
  name: string;
  displayName: string;
  minScore: string;
  maxScore: string;
  score: string;
  direction: string;
  scorePercentage: string;
  lastMsg: string;
  lastMsgDateTime: string;
  isStrategy: boolean;
  strategyName: string;
}

export interface IndicatorScoreRecord {
  symbol: string;
  key: string;
  candleType: string;
  interval: string;
  name: string;
  displayName: string;
  dateTime: string;
  minScore: string;
  maxScore: string;
  score: string;
  direction: string;
  scorePercentage: string;
  lastMsg: string;
  subCategoryScores: IndicatorSubCategoryScoreRecord[];
}

export interface CandleIntervalGroupedRecord {
  symbol: string;
  key: string;
  candleType: string;
  interval: string;
  direction: string;
  lastMsg: string;
  lastMsgDateTime: string;
  minScore: string;
  maxScore: string;
  score: string;
  scorePercentage: string;
  dateTime: string;
  indicatorScoreRecords: IndicatorScoreRecord[];
}

export interface TradeSignalScoreSnapshot {
  id: string;
  symbol: string;
  dateTime: string;
  candleIntervalGroupedRecords: CandleIntervalGroupedRecord[];
  minScore: string;
  maxScore: string;
  score: string;
  direction: string;
  scorePercentage: string;
}
