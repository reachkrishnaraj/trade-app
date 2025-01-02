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
