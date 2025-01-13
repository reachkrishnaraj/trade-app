export interface Event {
  id: number;
  datetime: string; // Use ISO string for LocalDateTime
  symbol: string;
  source: string;
  indicator: string;
  derivedValue: string;
  direction: string;
  category: string;
  rawMsg: string;
  price: string;
  interval: string;
  created: string;
  lastUpdated: string;
  strategy: boolean;
  importance: string;
  sinceCreatedStr: string;
  tradeAction: string;
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
