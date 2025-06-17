import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import { Subscription } from 'rxjs';
import { connectWebSocket, disconnectWebSocket, subscribeToTopic, unsubscribeFromTopic } from 'app/utils/websocket-utils';
import './SignalActionsTable.scss';

interface SignalAction {
  id: number;
  symbol: string;
  price: number;
  signalName: string;
  dateTime: string;
  status: 'PENDING' | 'EXECUTED' | 'CANCELLED';
  indicatorName: string;
  interval: string;
  message: string;
  direction: 'BUY' | 'SELL' | 'HOLD';
}

interface FilterState {
  symbol: string;
  interval: string;
  indicatorName: string;
  fromDate: string;
  toDate: string;
}

interface Notification {
  id: number;
  message: string;
  type: 'success' | 'info' | 'warning' | 'error';
  timestamp: number;
}

const SignalActionsTable = () => {
  const [data, setData] = useState<SignalAction[]>([]);
  const [filteredData, setFilteredData] = useState<SignalAction[]>([]);
  const [loading, setLoading] = useState(true);
  const [updated, setUpdated] = useState(false);
  const [minimized, setMinimized] = useState(false);
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [filters, setFilters] = useState<FilterState>({
    symbol: '',
    interval: '',
    indicatorName: '',
    fromDate: '',
    toDate: '',
  });

  // Get unique values for filter dropdowns
  const [uniqueSymbols, setUniqueSymbols] = useState<string[]>([]);
  const [uniqueIntervals, setUniqueIntervals] = useState<string[]>([]);
  const [uniqueIndicators, setUniqueIndicators] = useState<string[]>([]);

  // Notification management
  const addNotification = (message: string, type: 'success' | 'info' | 'warning' | 'error' = 'info') => {
    const notification: Notification = {
      id: Date.now(),
      message,
      type,
      timestamp: Date.now(),
    };

    setNotifications(prev => [notification, ...prev]);

    // Auto-remove notification after 5 seconds
    setTimeout(() => {
      setNotifications(prev => prev.filter(n => n.id !== notification.id));
    }, 5000);
  };

  const removeNotification = (id: number) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  // Check for new signals and show notifications
  const checkForNewSignals = (newData: SignalAction[], oldData: SignalAction[]) => {
    if (oldData.length === 0) return; // Skip on initial load

    const newSignals = newData.filter(newSignal => !oldData.some(oldSignal => oldSignal.id === newSignal.id));

    newSignals.forEach(signal => {
      const directionColor = signal.direction === 'BUY' ? '🟢' : signal.direction === 'SELL' ? '🔴' : '🟡';
      const message = `${directionColor} New ${signal.direction} signal: ${signal.symbol} (${signal.indicatorName}) - ${signal.price}`;
      addNotification(message, 'info');

      // Browser notification if permission granted
      if (Notification.permission === 'granted') {
        new Notification('New Trading Signal', {
          body: `${signal.direction} signal for ${signal.symbol} at ${signal.price}`,
          icon: '/favicon.ico',
        });
      }
    });
  };

  useEffect(() => {
    // Request notification permission
    if (Notification.permission === 'default') {
      Notification.requestPermission();
    }

    const fetchData = async () => {
      try {
        const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
        const response = await fetch('/api/v1/dashboard/signal-actions', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        const result: SignalAction[] = await response.json();

        // Sort by datetime (latest first) by default
        const sortedData = result.sort(
          (a: SignalAction, b: SignalAction) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime(),
        );

        setData(sortedData);
        setFilteredData(sortedData);

        // Extract unique values for filters
        setUniqueSymbols([...new Set(result.map((item: SignalAction) => item.symbol))]);
        setUniqueIntervals([...new Set(result.map((item: SignalAction) => item.interval))]);
        setUniqueIndicators([...new Set(result.map((item: SignalAction) => item.indicatorName))]);

        setLoading(false);
      } catch (error) {
        console.error('Error fetching signal actions:', error);
        setLoading(false);
      }
    };

    fetchData();

    // WebSocket connection
    connectWebSocket();
    const subscription: Subscription = subscribeToTopic('/topic/signal-actions').subscribe(event => {
      const sortedEvent: SignalAction[] = (event as SignalAction[]).sort(
        (a: SignalAction, b: SignalAction) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime(),
      );

      // Check for new signals before updating state
      checkForNewSignals(sortedEvent, data);

      setData(sortedEvent);
      applyFilters(sortedEvent, filters);
      setUpdated(true);
      setTimeout(() => setUpdated(false), 1000);
    });

    return () => {
      unsubscribeFromTopic('/topic/signal-actions');
      disconnectWebSocket();
      subscription.unsubscribe();
    };
  }, []);

  const applyFilters = (dataToFilter: SignalAction[], currentFilters: FilterState) => {
    let filtered = [...dataToFilter];

    if (currentFilters.symbol) {
      filtered = filtered.filter(item => item.symbol === currentFilters.symbol);
    }
    if (currentFilters.interval) {
      filtered = filtered.filter(item => item.interval === currentFilters.interval);
    }
    if (currentFilters.indicatorName) {
      filtered = filtered.filter(item => item.indicatorName === currentFilters.indicatorName);
    }
    if (currentFilters.fromDate) {
      filtered = filtered.filter(item => new Date(item.dateTime) >= new Date(currentFilters.fromDate));
    }
    if (currentFilters.toDate) {
      filtered = filtered.filter(item => new Date(item.dateTime) <= new Date(currentFilters.toDate));
    }

    setFilteredData(filtered);
  };

  const handleFilterChange = (field: keyof FilterState, value: string) => {
    const newFilters = { ...filters, [field]: value };
    setFilters(newFilters);
  };

  const handleApplyFilters = () => {
    applyFilters(data, filters);
  };

  const handleResetFilters = () => {
    const resetFilters = {
      symbol: '',
      interval: '',
      indicatorName: '',
      fromDate: '',
      toDate: '',
    };
    setFilters(resetFilters);
    setFilteredData(data);
  };

  const handleQuickRange = (hours: number) => {
    const now = new Date();
    const fromDate = new Date(now.getTime() - hours * 60 * 60 * 1000);
    const newFilters = {
      ...filters,
      fromDate: fromDate.toISOString().slice(0, 16),
      toDate: now.toISOString().slice(0, 16),
    };
    setFilters(newFilters);
    applyFilters(data, newFilters);
  };

  const handleClearRange = () => {
    const newFilters = { ...filters, fromDate: '', toDate: '' };
    setFilters(newFilters);
    applyFilters(data, newFilters);
  };

  const handleDateTimeSort = () => {
    const newSortOrder = sortOrder === 'desc' ? 'asc' : 'desc';
    setSortOrder(newSortOrder);

    const sorted = [...filteredData].sort((a, b) => {
      const dateA = new Date(a.dateTime).getTime();
      const dateB = new Date(b.dateTime).getTime();
      return newSortOrder === 'desc' ? dateB - dateA : dateA - dateB;
    });

    setFilteredData(sorted);
  };

  const handleExecute = async (signalId: number) => {
    try {
      const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
      const response = await fetch(`/api/v1/dashboard/signal-actions/${signalId}/execute`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const signal = data.find(s => s.id === signalId);
        addNotification(`✅ Successfully executed signal for ${signal?.symbol}`, 'success');
      } else {
        addNotification('❌ Failed to execute signal', 'error');
      }
    } catch (error) {
      console.error('Error executing signal:', error);
      addNotification('❌ Error executing signal', 'error');
    }
  };

  const handleCancel = async (signalId: number) => {
    try {
      const token = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
      const response = await fetch(`/api/v1/dashboard/signal-actions/${signalId}/cancel`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const signal = data.find(s => s.id === signalId);
        addNotification(`⏹️ Successfully cancelled signal for ${signal?.symbol}`, 'warning');
      } else {
        addNotification('❌ Failed to cancel signal', 'error');
      }
    } catch (error) {
      console.error('Error cancelling signal:', error);
      addNotification('❌ Error cancelling signal', 'error');
    }
  };

  const toggleMinimize = () => {
    setMinimized(!minimized);
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 4,
    }).format(price);
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'EXECUTED':
        return 'status-executed';
      case 'CANCELLED':
        return 'status-cancelled';
      case 'PENDING':
      default:
        return 'status-pending';
    }
  };

  const getDirectionClass = (direction: string) => {
    switch (direction) {
      case 'BUY':
        return 'direction-buy';
      case 'SELL':
        return 'direction-sell';
      case 'HOLD':
      default:
        return 'direction-hold';
    }
  };

  return (
    <div className={`table-container ${loading ? 'loading' : ''} ${updated ? 'updated' : ''}`}>
      {/* Notifications */}
      <div className="notifications-container">
        {notifications.map(notification => (
          <div
            key={notification.id}
            className={`notification notification-${notification.type}`}
            onClick={() => removeNotification(notification.id)}
          >
            <span className="notification-message">{notification.message}</span>
            <button className="notification-close">×</button>
          </div>
        ))}
      </div>

      <div className="table-header">
        <h3>Signal Actions</h3>
        <div className="header-actions">
          {notifications.length > 0 && <span className="notification-badge">{notifications.length}</span>}
          <button onClick={toggleMinimize} className="minimize-button">
            {minimized ? '▼' : '▲'}
          </button>
        </div>
      </div>

      {!minimized && (
        <>
          {/* Filters Section */}
          <div className="filters-section">
            <div className="filters-row">
              <div className="filter-group">
                <label>Filter by Symbol</label>
                <select value={filters.symbol} onChange={e => handleFilterChange('symbol', e.target.value)} className="filter-select">
                  <option value="">Select symbol...</option>
                  {uniqueSymbols.map(symbol => (
                    <option key={symbol} value={symbol}>
                      {symbol}
                    </option>
                  ))}
                </select>
              </div>

              <div className="filter-group">
                <label>Filter by Interval</label>
                <select value={filters.interval} onChange={e => handleFilterChange('interval', e.target.value)} className="filter-select">
                  <option value="">Select interval...</option>
                  {uniqueIntervals.map(interval => (
                    <option key={interval} value={interval}>
                      {interval}
                    </option>
                  ))}
                </select>
              </div>

              <div className="filter-group">
                <label>Filter by Indicator</label>
                <select
                  value={filters.indicatorName}
                  onChange={e => handleFilterChange('indicatorName', e.target.value)}
                  className="filter-select"
                >
                  <option value="">Select indicator...</option>
                  {uniqueIndicators.map(indicator => (
                    <option key={indicator} value={indicator}>
                      {indicator}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="filters-row">
              <div className="filter-group">
                <label>From Date</label>
                <input
                  type="datetime-local"
                  value={filters.fromDate}
                  onChange={e => handleFilterChange('fromDate', e.target.value)}
                  className="filter-input"
                />
              </div>

              <div className="filter-group">
                <label>To Date</label>
                <input
                  type="datetime-local"
                  value={filters.toDate}
                  onChange={e => handleFilterChange('toDate', e.target.value)}
                  className="filter-input"
                />
              </div>

              <div className="filter-actions">
                <button onClick={handleApplyFilters} className="btn btn-primary">
                  Apply Filters
                </button>
                <button onClick={handleResetFilters} className="btn btn-secondary">
                  Reset
                </button>
              </div>
            </div>

            {/* Quick Ranges */}
            <div className="quick-ranges">
              <label>Quick Ranges</label>
              <div className="range-buttons">
                <button onClick={() => handleQuickRange(1)} className="btn btn-range">
                  Last 1H
                </button>
                <button onClick={() => handleQuickRange(2)} className="btn btn-range">
                  Last 2H
                </button>
                <button onClick={() => handleQuickRange(4)} className="btn btn-range">
                  Last 4H
                </button>
                <button onClick={() => handleQuickRange(6)} className="btn btn-range">
                  Last 6H
                </button>
                <button onClick={() => handleQuickRange(12)} className="btn btn-range">
                  Last 12H
                </button>
                <button onClick={() => handleQuickRange(24)} className="btn btn-range">
                  Last 24H
                </button>
                <button onClick={handleClearRange} className="btn btn-range">
                  Clear Range
                </button>
              </div>
            </div>

            <div className="results-info">
              Showing {filteredData.length} of {data.length} events
            </div>
          </div>

          <div className="table-wrapper">
            {loading ? (
              <div className="loading-spinner">Loading...</div>
            ) : (
              <table className="signal-actions-table">
                <thead>
                  <tr>
                    <th className="sortable" onClick={handleDateTimeSort}>
                      Date Time {sortOrder === 'desc' ? '↓' : '↑'}
                    </th>
                    <th>Symbol</th>
                    <th>Indicator Name</th>
                    <th>Interval</th>
                    <th>Message</th>
                    <th>Direction</th>
                    <th>Price</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredData.length === 0 ? (
                    <tr>
                      <td colSpan={9} className="no-data">
                        No signal actions available
                      </td>
                    </tr>
                  ) : (
                    filteredData.map(signal => (
                      <tr key={signal.id}>
                        <td className="date-time">
                          {new Date(signal.dateTime).toLocaleString('en-US', {
                            hour: 'numeric',
                            minute: 'numeric',
                            second: 'numeric',
                            hour12: true,
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                          })}
                        </td>
                        <td className="symbol">{signal.symbol}</td>
                        <td className="indicator-name">{signal.indicatorName}</td>
                        <td className="interval">{signal.interval}</td>
                        <td className="message">{signal.message}</td>
                        <td className={`direction ${getDirectionClass(signal.direction)}`}>{signal.direction}</td>
                        <td className="price">{formatPrice(signal.price)}</td>
                        <td className={`status ${getStatusClass(signal.status)}`}>{signal.status}</td>
                        <td className="actions">
                          {signal.status === 'PENDING' && (
                            <div className="action-buttons">
                              <button className="btn btn-execute" onClick={() => handleExecute(signal.id)}>
                                Execute
                              </button>
                              <button className="btn btn-cancel" onClick={() => handleCancel(signal.id)}>
                                Cancel
                              </button>
                            </div>
                          )}
                          {signal.status === 'EXECUTED' && <span className="executed-label">Completed</span>}
                          {signal.status === 'CANCELLED' && <span className="cancelled-label">Cancelled</span>}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default SignalActionsTable;
