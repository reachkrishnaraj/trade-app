import React, { useState, useEffect } from 'react';
import {
  Search,
  Filter,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Clock,
  XCircle,
  Play,
  Eye,
  Settings,
  Download,
  ChevronDown,
  ChevronRight,
  Calendar,
  Users,
  BarChart3,
  TrendingUp,
  AlertTriangle,
  Activity,
  FileText,
  Mail,
  Bell,
  Zap,
  Target,
  Shield,
  Database,
  Server,
} from 'lucide-react';

const MerchantReportingV2 = () => {
  const [merchants, setMerchants] = useState([]);
  const [filteredMerchants, setFilteredMerchants] = useState([]);
  const [viewMode, setViewMode] = useState('table'); // table, card, compact
  const [theme, setTheme] = useState('modern'); // modern, classic, dark
  const [filters, setFilters] = useState({
    search: '',
    statuses: [], // Multiple status selection
    reportTypes: [], // Multiple report type selection
    dateFrom: '',
    dateTo: '',
    slaStatus: 'all',
    priority: 'all',
  });
  const [expandedRows, setExpandedRows] = useState(new Set());
  const [selectedMerchant, setSelectedMerchant] = useState(null);
  const [showRetryModal, setShowRetryModal] = useState(false);
  const [retryItem, setRetryItem] = useState(null);
  const [showFilters, setShowFilters] = useState(false);

  // Enhanced mock data with expected delivery times
  useEffect(() => {
    const mockData = [
      {
        id: 1,
        merchantName: 'TechCorp Solutions',
        maid: 'MAID001',
        totalReports: 5,
        successful: 4,
        failed: 1,
        pending: 0,
        lastUpdate: '2025-05-27T08:30:00Z',
        slaStatus: 'within',
        priority: 'high',
        nextReportDue: '2025-05-27T14:00:00Z',
        reports: [
          {
            id: 'RPT001',
            type: 'Financial Summary',
            status: 'completed',
            rdmStatus: 'completed',
            reportBuilderStatus: 'completed',
            scheduledTime: '2025-05-27T06:00:00Z',
            expectedDelivery: '2025-05-27T06:30:00Z',
            actualDelivery: '2025-05-27T06:15:00Z',
            completedTime: '2025-05-27T06:15:00Z',
            sla: 30,
            priority: 'high',
            deliveryMethod: 'email',
            errors: [],
          },
          {
            id: 'RPT002',
            type: 'Transaction Report',
            status: 'failed',
            rdmStatus: 'completed',
            reportBuilderStatus: 'failed',
            scheduledTime: '2025-05-27T07:00:00Z',
            expectedDelivery: '2025-05-27T08:00:00Z',
            actualDelivery: null,
            completedTime: null,
            sla: 60,
            priority: 'medium',
            deliveryMethod: 'api',
            errors: ['Reconciliation mismatch', 'Data validation failed', 'Timeout in report builder'],
          },
          {
            id: 'RPT003',
            type: 'Compliance Report',
            status: 'processing',
            rdmStatus: 'completed',
            reportBuilderStatus: 'processing',
            scheduledTime: '2025-05-27T09:00:00Z',
            expectedDelivery: '2025-05-27T10:00:00Z',
            actualDelivery: null,
            completedTime: null,
            sla: 60,
            priority: 'high',
            deliveryMethod: 'dashboard',
            errors: [],
          },
        ],
      },
      {
        id: 2,
        merchantName: 'Global Retail Inc',
        maid: 'MAID002',
        totalReports: 8,
        successful: 6,
        failed: 1,
        pending: 1,
        lastUpdate: '2025-05-27T09:15:00Z',
        slaStatus: 'breach',
        priority: 'medium',
        nextReportDue: '2025-05-27T12:00:00Z',
        reports: [
          {
            id: 'RPT004',
            type: 'Daily Sales',
            status: 'processing',
            rdmStatus: 'completed',
            reportBuilderStatus: 'processing',
            scheduledTime: '2025-05-27T08:00:00Z',
            expectedDelivery: '2025-05-27T08:45:00Z',
            actualDelivery: null,
            completedTime: null,
            sla: 45,
            priority: 'medium',
            deliveryMethod: 'email',
            errors: [],
          },
          {
            id: 'RPT005',
            type: 'Inventory Report',
            status: 'pending',
            rdmStatus: 'pending',
            reportBuilderStatus: 'waiting',
            scheduledTime: '2025-05-27T10:00:00Z',
            expectedDelivery: '2025-05-27T11:00:00Z',
            actualDelivery: null,
            completedTime: null,
            sla: 60,
            priority: 'low',
            deliveryMethod: 'ftp',
            errors: [],
          },
        ],
      },
    ];
    setMerchants(mockData);
    setFilteredMerchants(mockData);
  }, []);

  // Enhanced filter logic
  useEffect(() => {
    let filtered = merchants.filter(merchant => {
      // Search filter
      const matchesSearch =
        merchant.merchantName.toLowerCase().includes(filters.search.toLowerCase()) ||
        merchant.maid.toLowerCase().includes(filters.search.toLowerCase());

      // Status filter (multiple selection)
      const hasSelectedStatuses =
        filters.statuses.length === 0 || merchant.reports.some(report => filters.statuses.includes(report.status));

      // Report type filter (multiple selection)
      const hasSelectedReportTypes =
        filters.reportTypes.length === 0 || merchant.reports.some(report => filters.reportTypes.includes(report.type));

      // Date range filter
      const matchesDateRange =
        (!filters.dateFrom && !filters.dateTo) ||
        merchant.reports.some(report => {
          const reportDate = new Date(report.scheduledTime);
          const fromDate = filters.dateFrom ? new Date(filters.dateFrom) : null;
          const toDate = filters.dateTo ? new Date(filters.dateTo) : null;

          if (fromDate && toDate) {
            return reportDate >= fromDate && reportDate <= toDate;
          } else if (fromDate) {
            return reportDate >= fromDate;
          } else if (toDate) {
            return reportDate <= toDate;
          }
          return true;
        });

      // SLA status filter
      const matchesSlaStatus = filters.slaStatus === 'all' || merchant.slaStatus === filters.slaStatus;

      // Priority filter
      const matchesPriority = filters.priority === 'all' || merchant.priority === filters.priority;

      return matchesSearch && hasSelectedStatuses && hasSelectedReportTypes && matchesDateRange && matchesSlaStatus && matchesPriority;
    });

    setFilteredMerchants(filtered);
  }, [merchants, filters]);

  const getStatusColor = (status: string): string => {
    switch (status) {
      case 'completed':
        return 'text-green-600 bg-green-50 border-green-200';
      case 'failed':
        return 'text-red-600 bg-red-50 border-red-200';
      case 'processing':
        return 'text-blue-600 bg-blue-50 border-blue-200';
      case 'pending':
        return 'text-yellow-600 bg-yellow-50 border-yellow-200';
      case 'waiting':
        return 'text-gray-600 bg-gray-50 border-gray-200';
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="w-4 h-4" />;
      case 'failed':
        return <XCircle className="w-4 h-4" />;
      case 'processing':
        return <RefreshCw className="w-4 h-4 animate-spin" />;
      case 'pending':
        return <Clock className="w-4 h-4" />;
      case 'waiting':
        return <AlertCircle className="w-4 h-4" />;
      default:
        return <AlertCircle className="w-4 h-4" />;
    }
  };

  const getPriorityColor = (priority: string): string => {
    switch (priority) {
      case 'high':
        return 'text-red-600 bg-red-50';
      case 'medium':
        return 'text-yellow-600 bg-yellow-50';
      case 'low':
        return 'text-green-600 bg-green-50';
      default:
        return 'text-gray-600 bg-gray-50';
    }
  };

  const toggleRowExpansion = (merchantId: number) => {
    const newExpanded = new Set(expandedRows);
    if (newExpanded.has(merchantId)) {
      newExpanded.delete(merchantId);
    } else {
      newExpanded.add(merchantId);
    }
    setExpandedRows(newExpanded);
  };

  const handleRetry = (merchant: any, report: any) => {
    setRetryItem({ merchant, report });
    setShowRetryModal(true);
  };

  const executeRetry = () => {
    console.log('Retrying report:', retryItem);
    setShowRetryModal(false);
    setRetryItem(null);
  };

  const formatTime = (timestamp: string | null): string => {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleString();
  };

  const calculateProgress = (scheduledTime: string, sla: number): number => {
    const now = new Date();
    const scheduled = new Date(scheduledTime);
    const elapsed = (now.getTime() - scheduled.getTime()) / (1000 * 60);
    return Math.min((elapsed / sla) * 100, 100);
  };

  const handleStatusToggle = (status: string) => {
    setFilters(prev => ({
      ...prev,
      statuses: prev.statuses.includes(status) ? prev.statuses.filter(s => s !== status) : [...prev.statuses, status],
    }));
  };

  const handleReportTypeToggle = (reportType: string) => {
    setFilters(prev => ({
      ...prev,
      reportTypes: prev.reportTypes.includes(reportType)
        ? prev.reportTypes.filter(t => t !== reportType)
        : [...prev.reportTypes, reportType],
    }));
  };

  const clearAllFilters = () => {
    setFilters({
      search: '',
      statuses: [],
      reportTypes: [],
      dateFrom: '',
      dateTo: '',
      slaStatus: 'all',
      priority: 'all',
    });
  };

  const getThemeClasses = () => {
    switch (theme) {
      case 'dark':
        return {
          bg: 'bg-gray-900',
          cardBg: 'bg-gray-800',
          text: 'text-white',
          textSecondary: 'text-gray-300',
          border: 'border-gray-700',
        };
      case 'classic':
        return {
          bg: 'bg-blue-50',
          cardBg: 'bg-white',
          text: 'text-gray-900',
          textSecondary: 'text-gray-600',
          border: 'border-gray-200',
        };
      default: // modern
        return {
          bg: 'bg-gray-50',
          cardBg: 'bg-white',
          text: 'text-gray-900',
          textSecondary: 'text-gray-600',
          border: 'border-gray-200',
        };
    }
  };

  const themeClasses = getThemeClasses();

  const renderCardView = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {filteredMerchants.map(merchant => (
        <div
          key={merchant.id}
          className={`${themeClasses.cardBg} rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 overflow-hidden border ${themeClasses.border}`}
        >
          <div className="p-6">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className={`text-lg font-semibold ${themeClasses.text}`}>{merchant.merchantName}</h3>
                <p className={`text-sm ${themeClasses.textSecondary}`}>MAID: {merchant.maid}</p>
              </div>
              <span
                className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getPriorityColor(merchant.priority)}`}
              >
                {merchant.priority.toUpperCase()}
              </span>
            </div>

            <div className="grid grid-cols-3 gap-4 mb-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">{merchant.successful}</div>
                <div className="text-xs text-green-600">Success</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-red-600">{merchant.failed}</div>
                <div className="text-xs text-red-600">Failed</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-yellow-600">{merchant.pending}</div>
                <div className="text-xs text-yellow-600">Pending</div>
              </div>
            </div>

            <div className="space-y-2 mb-4">
              <div className="flex justify-between text-sm">
                <span className={themeClasses.textSecondary}>Next Report:</span>
                <span className={themeClasses.text}>{formatTime(merchant.nextReportDue)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className={themeClasses.textSecondary}>SLA Status:</span>
                <span
                  className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                    merchant.slaStatus === 'within' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  }`}
                >
                  {merchant.slaStatus === 'within' ? 'Within SLA' : 'SLA Breach'}
                </span>
              </div>
            </div>

            <div className="flex space-x-2">
              <button className="flex-1 bg-blue-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-blue-700 transition-colors">
                View Details
              </button>
              <button className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                <Settings className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );

  const renderCompactView = () => (
    <div className={`${themeClasses.cardBg} rounded-lg shadow overflow-hidden`}>
      <div className="divide-y divide-gray-200">
        {filteredMerchants.map(merchant => (
          <div key={merchant.id} className="p-4 hover:bg-gray-50 transition-colors">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <div>
                  <h3 className={`font-medium ${themeClasses.text}`}>{merchant.merchantName}</h3>
                  <p className={`text-sm ${themeClasses.textSecondary}`}>MAID: {merchant.maid}</p>
                </div>
                <div className="flex space-x-2">
                  <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    ✓ {merchant.successful}
                  </span>
                  {merchant.failed > 0 && (
                    <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                      ✗ {merchant.failed}
                    </span>
                  )}
                  {merchant.pending > 0 && (
                    <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                      ⏳ {merchant.pending}
                    </span>
                  )}
                </div>
              </div>
              <div className="flex items-center space-x-4">
                <div className="text-right">
                  <div className={`text-sm ${themeClasses.textSecondary}`}>Next Report</div>
                  <div className={`text-xs ${themeClasses.text}`}>{formatTime(merchant.nextReportDue)}</div>
                </div>
                <button className="p-2 hover:bg-gray-100 rounded-lg">
                  <Eye className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  return (
    <div className={`min-h-screen ${themeClasses.bg} p-6`}>
      {/* Header with Theme Selector */}
      <div className="mb-8 flex justify-between items-start">
        <div>
          <h1 className={`text-3xl font-bold ${themeClasses.text} mb-2`}>Merchant Reporting Dashboard</h1>
          <p className={themeClasses.textSecondary}>Monitor report lifecycle, SLA compliance, and manage merchant reporting operations</p>
        </div>
        <div className="flex space-x-4">
          {/* Theme Selector */}
          <select
            value={theme}
            onChange={e => setTheme(e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            <option value="modern">Modern Theme</option>
            <option value="classic">Classic Theme</option>
            <option value="dark">Dark Theme</option>
          </select>

          {/* View Mode Selector */}
          <div className="flex rounded-lg border border-gray-300 overflow-hidden">
            <button
              onClick={() => setViewMode('table')}
              className={`px-3 py-2 text-sm ${viewMode === 'table' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'}`}
            >
              Table
            </button>
            <button
              onClick={() => setViewMode('card')}
              className={`px-3 py-2 text-sm ${viewMode === 'card' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'}`}
            >
              Cards
            </button>
            <button
              onClick={() => setViewMode('compact')}
              className={`px-3 py-2 text-sm ${viewMode === 'compact' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 hover:bg-gray-50'}`}
            >
              Compact
            </button>
          </div>
        </div>
      </div>

      {/* Enhanced Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-4 mb-8">
        <div className={`${themeClasses.cardBg} rounded-lg shadow p-4 border ${themeClasses.border}`}>
          <div className="flex items-center">
            <Users className="w-6 h-6 text-blue-500" />
            <div className="ml-3">
              <p className={`text-xs font-medium ${themeClasses.textSecondary}`}>Total Merchants</p>
              <p className={`text-xl font-bold ${themeClasses.text}`}>{merchants.length}</p>
            </div>
          </div>
        </div>
        <div className={`${themeClasses.cardBg} rounded-lg shadow p-4 border ${themeClasses.border}`}>
          <div className="flex items-center">
            <CheckCircle className="w-6 h-6 text-green-500" />
            <div className="ml-3">
              <p className={`text-xs font-medium ${themeClasses.textSecondary}`}>Successful</p>
              <p className={`text-xl font-bold ${themeClasses.text}`}>{merchants.reduce((acc, m) => acc + m.successful, 0)}</p>
            </div>
          </div>
        </div>
        <div className={`${themeClasses.cardBg} rounded-lg shadow p-4 border ${themeClasses.border}`}>
          <div className="flex items-center">
            <XCircle className="w-6 h-6 text-red-500" />
            <div className="ml-3">
              <p className={`text-xs font-medium ${themeClasses.textSecondary}`}>Failed</p>
              <p className={`text-xl font-bold ${themeClasses.text}`}>{merchants.reduce((acc, m) => acc + m.failed, 0)}</p>
            </div>
          </div>
        </div>
        <div className={`${themeClasses.cardBg} rounded-lg shadow p-4 border ${themeClasses.border}`}>
          <div className="flex items-center">
            <Clock className="w-6 h-6 text-yellow-500" />
            <div className="ml-3">
              <p className={`text-xs font-medium ${themeClasses.textSecondary}`}>Pending</p>
              <p className={`text-xl font-bold ${themeClasses.text}`}>{merchants.reduce((acc, m) => acc + m.pending, 0)}</p>
            </div>
          </div>
        </div>
        <div className={`${themeClasses.cardBg} rounded-lg shadow p-4 border ${themeClasses.border}`}>
          <div className="flex items-center">
            <TrendingUp className="w-6 h-6 text-purple-500" />
            <div className="ml-3">
              <p className={`text-xs font-medium ${themeClasses.textSecondary}`}>Success Rate</p>
              <p className={`text-xl font-bold ${themeClasses.text}`}>
                {Math.round(
                  (merchants.reduce((acc, m) => acc + m.successful, 0) / merchants.reduce((acc, m) => acc + m.totalReports, 0)) * 100,
                )}
                %
              </p>
            </div>
          </div>
        </div>
        <div className={`${themeClasses.cardBg} rounded-lg shadow p-4 border ${themeClasses.border}`}>
          <div className="flex items-center">
            <Shield className="w-6 h-6 text-indigo-500" />
            <div className="ml-3">
              <p className={`text-xs font-medium ${themeClasses.textSecondary}`}>SLA Compliance</p>
              <p className={`text-xl font-bold ${themeClasses.text}`}>
                {Math.round((merchants.filter(m => m.slaStatus === 'within').length / merchants.length) * 100)}%
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Enhanced Filters */}
      <div className={`${themeClasses.cardBg} rounded-lg shadow mb-6 border ${themeClasses.border}`}>
        <div className="p-4 border-b border-gray-200">
          <div className="flex justify-between items-center">
            <h3 className={`text-lg font-medium ${themeClasses.text}`}>Filters & Search</h3>
            <div className="flex space-x-2">
              <button
                onClick={() => setShowFilters(!showFilters)}
                className="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
              >
                <Filter className="w-4 h-4 mr-2" />
                {showFilters ? 'Hide Filters' : 'Show Filters'}
              </button>
              <button
                onClick={clearAllFilters}
                className="inline-flex items-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
              >
                Clear All
              </button>
            </div>
          </div>
        </div>

        <div className="p-4">
          {/* Basic Search */}
          <div className="mb-4">
            <div className="relative">
              <Search className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search merchants or MAID..."
                className="pl-10 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                value={filters.search}
                onChange={e => setFilters(prev => ({ ...prev, search: e.target.value }))}
              />
            </div>
          </div>

          {showFilters && (
            <div className="space-y-4">
              {/* Date Range Picker */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className={`block text-sm font-medium ${themeClasses.text} mb-1`}>From Date</label>
                  <input
                    type="datetime-local"
                    className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={filters.dateFrom}
                    onChange={e => setFilters(prev => ({ ...prev, dateFrom: e.target.value }))}
                  />
                </div>
                <div>
                  <label className={`block text-sm font-medium ${themeClasses.text} mb-1`}>To Date</label>
                  <input
                    type="datetime-local"
                    className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={filters.dateTo}
                    onChange={e => setFilters(prev => ({ ...prev, dateTo: e.target.value }))}
                  />
                </div>
              </div>

              {/* Multiple Status Filter */}
              <div>
                <label className={`block text-sm font-medium ${themeClasses.text} mb-2`}>Report Status (Multiple Selection)</label>
                <div className="flex flex-wrap gap-2">
                  {['completed', 'failed', 'processing', 'pending', 'waiting'].map(status => (
                    <label key={status} className="inline-flex items-center">
                      <input
                        type="checkbox"
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        checked={filters.statuses.includes(status)}
                        onChange={() => handleStatusToggle(status)}
                      />
                      <span className={`ml-2 text-sm ${themeClasses.text} capitalize`}>{status}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Multiple Report Type Filter */}
              <div>
                <label className={`block text-sm font-medium ${themeClasses.text} mb-2`}>Report Types (Multiple Selection)</label>
                <div className="flex flex-wrap gap-2">
                  {['Financial Summary', 'Transaction Report', 'Daily Sales', 'Compliance Report', 'Inventory Report'].map(type => (
                    <label key={type} className="inline-flex items-center">
                      <input
                        type="checkbox"
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        checked={filters.reportTypes.includes(type)}
                        onChange={() => handleReportTypeToggle(type)}
                      />
                      <span className={`ml-2 text-sm ${themeClasses.text}`}>{type}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Additional Filters */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className={`block text-sm font-medium ${themeClasses.text} mb-1`}>SLA Status</label>
                  <select
                    className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={filters.slaStatus}
                    onChange={e => setFilters(prev => ({ ...prev, slaStatus: e.target.value }))}
                  >
                    <option value="all">All SLA Status</option>
                    <option value="within">Within SLA</option>
                    <option value="breach">SLA Breach</option>
                  </select>
                </div>
                <div>
                  <label className={`block text-sm font-medium ${themeClasses.text} mb-1`}>Priority</label>
                  <select
                    className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    value={filters.priority}
                    onChange={e => setFilters(prev => ({ ...prev, priority: e.target.value }))}
                  >
                    <option value="all">All Priorities</option>
                    <option value="high">High Priority</option>
                    <option value="medium">Medium Priority</option>
                    <option value="low">Low Priority</option>
                  </select>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Active Filters Display */}
      {(filters.statuses.length > 0 ||
        filters.reportTypes.length > 0 ||
        filters.dateFrom ||
        filters.dateTo ||
        filters.slaStatus !== 'all' ||
        filters.priority !== 'all') && (
        <div className={`${themeClasses.cardBg} rounded-lg shadow mb-6 p-4 border ${themeClasses.border}`}>
          <div className="flex items-center justify-between mb-2">
            <h4 className={`text-sm font-medium ${themeClasses.text}`}>Active Filters:</h4>
            <button onClick={clearAllFilters} className="text-sm text-blue-600 hover:text-blue-800">
              Clear All
            </button>
          </div>
          <div className="flex flex-wrap gap-2">
            {filters.statuses.map(status => (
              <span key={status} className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                Status: {status}
                <button onClick={() => handleStatusToggle(status)} className="ml-1 hover:text-blue-600">
                  ×
                </button>
              </span>
            ))}
            {filters.reportTypes.map(type => (
              <span key={type} className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                Type: {type}
                <button onClick={() => handleReportTypeToggle(type)} className="ml-1 hover:text-green-600">
                  ×
                </button>
              </span>
            ))}
            {filters.dateFrom && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                From: {new Date(filters.dateFrom).toLocaleDateString()}
              </span>
            )}
            {filters.dateTo && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                To: {new Date(filters.dateTo).toLocaleDateString()}
              </span>
            )}
            {filters.slaStatus !== 'all' && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                SLA: {filters.slaStatus}
              </span>
            )}
            {filters.priority !== 'all' && (
              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                Priority: {filters.priority}
              </span>
            )}
          </div>
        </div>
      )}

      {/* Main Content Area */}
      {viewMode === 'card' && renderCardView()}
      {viewMode === 'compact' && renderCompactView()}
      {viewMode === 'table' && (
        <div className={`${themeClasses.cardBg} rounded-lg shadow overflow-hidden border ${themeClasses.border}`}>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant Details</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Report Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Next Report Due</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">SLA Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Last Update</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredMerchants.map(merchant => (
                  <React.Fragment key={merchant.id}>
                    <tr className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <button onClick={() => toggleRowExpansion(merchant.id)} className="mr-2 p-1 hover:bg-gray-100 rounded">
                            {expandedRows.has(merchant.id) ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
                          </button>
                          <div>
                            <div className="flex items-center space-x-2">
                              <div className="text-sm font-medium text-gray-900">{merchant.merchantName}</div>
                              <span
                                className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getPriorityColor(merchant.priority)}`}
                              >
                                {merchant.priority}
                              </span>
                            </div>
                            <div className="text-sm text-gray-500">MAID: {merchant.maid}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex space-x-2">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            ✓ {merchant.successful}
                          </span>
                          {merchant.failed > 0 && (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                              ✗ {merchant.failed}
                            </span>
                          )}
                          {merchant.pending > 0 && (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                              ⏳ {merchant.pending}
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">{formatTime(merchant.nextReportDue)}</div>
                        <div className="text-xs text-gray-500">
                          {new Date(merchant.nextReportDue) < new Date() ? 'Overdue' : 'Upcoming'}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            merchant.slaStatus === 'within' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                          }`}
                        >
                          {merchant.slaStatus === 'within' ? (
                            <>
                              <Shield className="w-3 h-3 mr-1" />
                              Within SLA
                            </>
                          ) : (
                            <>
                              <AlertTriangle className="w-3 h-3 mr-1" />
                              SLA Breach
                            </>
                          )}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formatTime(merchant.lastUpdate)}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex justify-end space-x-2">
                          <button className="text-blue-600 hover:text-blue-900 p-1 hover:bg-blue-50 rounded">
                            <Eye className="w-4 h-4" />
                          </button>
                          <button className="text-green-600 hover:text-green-900 p-1 hover:bg-green-50 rounded">
                            <RefreshCw className="w-4 h-4" />
                          </button>
                          <button className="text-purple-600 hover:text-purple-900 p-1 hover:bg-purple-50 rounded">
                            <Bell className="w-4 h-4" />
                          </button>
                          <button className="text-gray-600 hover:text-gray-900 p-1 hover:bg-gray-50 rounded">
                            <Settings className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>

                    {/* Expanded Row Content */}
                    {expandedRows.has(merchant.id) && (
                      <tr>
                        <td colSpan={6} className="px-6 py-4 bg-gray-50">
                          <div className="space-y-4">
                            <div className="flex justify-between items-center">
                              <h4 className="font-medium text-gray-900">Report Lifecycle Details</h4>
                              <div className="text-sm text-gray-500">
                                Total Reports: {merchant.totalReports} | Success Rate:{' '}
                                {Math.round((merchant.successful / merchant.totalReports) * 100)}%
                              </div>
                            </div>
                            <div className="grid gap-4">
                              {merchant.reports.map(report => (
                                <div key={report.id} className="bg-white rounded-lg p-4 border border-gray-200 shadow-sm">
                                  <div className="flex items-center justify-between mb-4">
                                    <div className="flex items-center space-x-3">
                                      <span
                                        className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(report.status)}`}
                                      >
                                        {getStatusIcon(report.status)}
                                        <span className="ml-2">{report.status.toUpperCase()}</span>
                                      </span>
                                      <span className="font-semibold text-gray-900">{report.type}</span>
                                      <span className="text-sm text-gray-500">ID: {report.id}</span>
                                      <span
                                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getPriorityColor(report.priority)}`}
                                      >
                                        {report.priority}
                                      </span>
                                    </div>
                                    <div className="flex space-x-2">
                                      {report.status === 'failed' && (
                                        <button
                                          onClick={() => handleRetry(merchant, report)}
                                          className="inline-flex items-center px-3 py-1 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 transition-colors"
                                        >
                                          <Play className="w-3 h-3 mr-1" />
                                          Retry
                                        </button>
                                      )}
                                      <button className="inline-flex items-center px-3 py-1 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition-colors">
                                        <Download className="w-3 h-3 mr-1" />
                                        Download
                                      </button>
                                      <button className="inline-flex items-center px-3 py-1 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition-colors">
                                        <Mail className="w-3 h-3 mr-1" />
                                        {report.deliveryMethod}
                                      </button>
                                    </div>
                                  </div>

                                  {/* Enhanced Pipeline Status */}
                                  <div className="mb-4">
                                    <div className="flex items-center space-x-1 mb-2">
                                      <span className="text-sm font-medium text-gray-700">Pipeline Status:</span>
                                    </div>
                                    <div className="flex items-center space-x-4">
                                      <div className="flex items-center space-x-2">
                                        <Database className="w-4 h-4 text-gray-500" />
                                        <span
                                          className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium border ${getStatusColor(report.rdmStatus)}`}
                                        >
                                          RDM: {report.rdmStatus}
                                        </span>
                                      </div>
                                      <div className="text-gray-400">→</div>
                                      <div className="flex items-center space-x-2">
                                        <Server className="w-4 h-4 text-gray-500" />
                                        <span
                                          className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium border ${getStatusColor(report.reportBuilderStatus)}`}
                                        >
                                          Report Builder: {report.reportBuilderStatus}
                                        </span>
                                      </div>
                                    </div>
                                  </div>

                                  {/* Progress Bar for Processing Reports */}
                                  {report.status === 'processing' && (
                                    <div className="mb-4">
                                      <div className="flex justify-between text-sm text-gray-600 mb-2">
                                        <span className="flex items-center">
                                          <Activity className="w-4 h-4 mr-1" />
                                          Processing Progress
                                        </span>
                                        <span className="font-medium">
                                          {Math.round(calculateProgress(report.scheduledTime, report.sla))}%
                                        </span>
                                      </div>
                                      <div className="w-full bg-gray-200 rounded-full h-3">
                                        <div
                                          className="bg-gradient-to-r from-blue-500 to-blue-600 h-3 rounded-full transition-all duration-500 ease-out"
                                          style={{ width: `${calculateProgress(report.scheduledTime, report.sla)}%` }}
                                        ></div>
                                      </div>
                                    </div>
                                  )}

                                  {/* Enhanced Timing Information */}
                                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm mb-4">
                                    <div className="bg-gray-50 p-3 rounded-lg">
                                      <div className="flex items-center text-gray-600 mb-1">
                                        <Calendar className="w-4 h-4 mr-1" />
                                        Scheduled
                                      </div>
                                      <div className="font-medium text-gray-900">{formatTime(report.scheduledTime)}</div>
                                    </div>
                                    <div className="bg-blue-50 p-3 rounded-lg">
                                      <div className="flex items-center text-blue-600 mb-1">
                                        <Target className="w-4 h-4 mr-1" />
                                        Expected Delivery
                                      </div>
                                      <div className="font-medium text-blue-900">{formatTime(report.expectedDelivery)}</div>
                                    </div>
                                    <div className={`p-3 rounded-lg ${report.actualDelivery ? 'bg-green-50' : 'bg-gray-50'}`}>
                                      <div
                                        className={`flex items-center mb-1 ${report.actualDelivery ? 'text-green-600' : 'text-gray-600'}`}
                                      >
                                        <CheckCircle className="w-4 h-4 mr-1" />
                                        Actual Delivery
                                      </div>
                                      <div className={`font-medium ${report.actualDelivery ? 'text-green-900' : 'text-gray-500'}`}>
                                        {formatTime(report.actualDelivery)}
                                      </div>
                                    </div>
                                    <div className="bg-yellow-50 p-3 rounded-lg">
                                      <div className="flex items-center text-yellow-600 mb-1">
                                        <Clock className="w-4 h-4 mr-1" />
                                        SLA
                                      </div>
                                      <div className="font-medium text-yellow-900">{report.sla} minutes</div>
                                    </div>
                                  </div>

                                  {/* Delivery Information */}
                                  <div className="mb-4 p-3 bg-gray-50 rounded-lg">
                                    <div className="text-sm font-medium text-gray-700 mb-2">Delivery Details</div>
                                    <div className="flex items-center space-x-4 text-sm">
                                      <div className="flex items-center">
                                        <Mail className="w-4 h-4 mr-1 text-gray-500" />
                                        <span>Method: {report.deliveryMethod}</span>
                                      </div>
                                      <div className="flex items-center">
                                        <Zap className="w-4 h-4 mr-1 text-gray-500" />
                                        <span>Priority: {report.priority}</span>
                                      </div>
                                    </div>
                                  </div>

                                  {/* Error Details */}
                                  {report.errors.length > 0 && (
                                    <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                                      <div className="flex items-center mb-2">
                                        <AlertCircle className="w-4 h-4 text-red-600 mr-1" />
                                        <span className="font-medium text-red-800">Error Details ({report.errors.length})</span>
                                      </div>
                                      <ul className="space-y-1">
                                        {report.errors.map((error, index) => (
                                          <li key={index} className="flex items-start text-sm text-red-700">
                                            <span className="inline-block w-1.5 h-1.5 bg-red-400 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                                            {error}
                                          </li>
                                        ))}
                                      </ul>
                                    </div>
                                  )}
                                </div>
                              ))}
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Enhanced Retry Modal */}
      {showRetryModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <div className="flex items-center mb-4">
                <Play className="w-6 h-6 text-blue-600 mr-2" />
                <h3 className="text-lg font-medium text-gray-900">Retry Report Generation</h3>
              </div>
              <div className="space-y-4">
                <div className="bg-gray-50 p-3 rounded-lg">
                  <div className="text-sm font-medium text-gray-700">Report Details:</div>
                  <div className="text-sm text-gray-600 mt-1">
                    <div>Merchant: {retryItem?.merchant?.merchantName}</div>
                    <div>Report: {retryItem?.report?.type}</div>
                    <div>Report ID: {retryItem?.report?.id}</div>
                  </div>
                </div>
                <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3">
                  <div className="flex">
                    <AlertCircle className="w-5 h-5 text-yellow-600 mr-2 flex-shrink-0" />
                    <div className="text-sm text-yellow-700">
                      <div className="font-medium mb-1">Retry Options:</div>
                      <ul className="list-disc list-inside space-y-1">
                        <li>Restart from RDM data production</li>
                        <li>Reset report builder pipeline</li>
                        <li>Clear previous error states</li>
                        <li>Send notification on completion</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button
                  onClick={() => setShowRetryModal(false)}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={executeRetry}
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 transition-colors"
                >
                  <Play className="w-4 h-4 mr-1 inline" />
                  Retry Report
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MerchantReportingV2;
