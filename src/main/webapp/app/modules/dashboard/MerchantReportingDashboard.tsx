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
} from 'lucide-react';

const MerchantReportingDashboard = () => {
  const [merchants, setMerchants] = useState([]);
  const [filteredMerchants, setFilteredMerchants] = useState([]);
  const [filters, setFilters] = useState({
    search: '',
    status: 'all',
    reportType: 'all',
    dateRange: 'today',
  });
  const [expandedRows, setExpandedRows] = useState(new Set());
  const [selectedMerchant, setSelectedMerchant] = useState(null);
  const [showRetryModal, setShowRetryModal] = useState(false);
  const [retryItem, setRetryItem] = useState(null);

  // Mock data - replace with actual API calls
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
        reports: [
          {
            id: 'RPT001',
            type: 'Financial Summary',
            status: 'completed',
            rdmStatus: 'completed',
            reportBuilderStatus: 'completed',
            scheduledTime: '2025-05-27T06:00:00Z',
            completedTime: '2025-05-27T06:15:00Z',
            sla: 30,
            errors: [],
          },
          {
            id: 'RPT002',
            type: 'Transaction Report',
            status: 'failed',
            rdmStatus: 'completed',
            reportBuilderStatus: 'failed',
            scheduledTime: '2025-05-27T07:00:00Z',
            completedTime: null,
            sla: 60,
            errors: ['Reconciliation mismatch', 'Data validation failed'],
          },
        ],
      },
      {
        id: 2,
        merchantName: 'Global Retail Inc',
        maid: 'MAID002',
        totalReports: 8,
        successful: 7,
        failed: 0,
        pending: 1,
        lastUpdate: '2025-05-27T09:15:00Z',
        slaStatus: 'within',
        reports: [
          {
            id: 'RPT003',
            type: 'Daily Sales',
            status: 'processing',
            rdmStatus: 'completed',
            reportBuilderStatus: 'processing',
            scheduledTime: '2025-05-27T08:00:00Z',
            completedTime: null,
            sla: 45,
            errors: [],
          },
        ],
      },
    ];
    setMerchants(mockData);
    setFilteredMerchants(mockData);
  }, []);

  // Filter logic
  useEffect(() => {
    let filtered = merchants.filter(merchant => {
      const matchesSearch =
        merchant.merchantName.toLowerCase().includes(filters.search.toLowerCase()) ||
        merchant.maid.toLowerCase().includes(filters.search.toLowerCase());

      const matchesStatus =
        filters.status === 'all' ||
        (filters.status === 'success' && merchant.failed === 0) ||
        (filters.status === 'failed' && merchant.failed > 0) ||
        (filters.status === 'pending' && merchant.pending > 0);

      return matchesSearch && matchesStatus;
    });

    setFilteredMerchants(filtered);
  }, [merchants, filters]);

  const getStatusColor = status => {
    switch (status) {
      case 'completed':
        return 'text-green-600 bg-green-50';
      case 'failed':
        return 'text-red-600 bg-red-50';
      case 'processing':
        return 'text-blue-600 bg-blue-50';
      case 'pending':
        return 'text-yellow-600 bg-yellow-50';
      default:
        return 'text-gray-600 bg-gray-50';
    }
  };

  const getStatusIcon = status => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="w-4 h-4" />;
      case 'failed':
        return <XCircle className="w-4 h-4" />;
      case 'processing':
        return <RefreshCw className="w-4 h-4 animate-spin" />;
      case 'pending':
        return <Clock className="w-4 h-4" />;
      default:
        return <AlertCircle className="w-4 h-4" />;
    }
  };

  const toggleRowExpansion = merchantId => {
    const newExpanded = new Set(expandedRows);
    if (newExpanded.has(merchantId)) {
      newExpanded.delete(merchantId);
    } else {
      newExpanded.add(merchantId);
    }
    setExpandedRows(newExpanded);
  };

  const handleRetry = (merchant, report) => {
    setRetryItem({ merchant, report });
    setShowRetryModal(true);
  };

  const executeRetry = () => {
    // Implement retry logic here
    console.log('Retrying report:', retryItem);
    setShowRetryModal(false);
    setRetryItem(null);
  };

  const formatTime = timestamp => {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleString();
  };

  const calculateProgress = (scheduledTime: string, sla: number): number => {
    const now = new Date();
    const scheduled = new Date(scheduledTime);
    const elapsed = (now.getTime() - scheduled.getTime()) / (1000 * 60); // minutes
    return Math.min((elapsed / sla) * 100, 100);
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Merchant Reporting Dashboard</h1>
        <p className="text-gray-600">Monitor report lifecycle, SLA compliance, and manage merchant reporting operations</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <Users className="w-8 h-8 text-blue-500" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Merchants</p>
              <p className="text-2xl font-bold text-gray-900">{merchants.length}</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <CheckCircle className="w-8 h-8 text-green-500" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Successful Reports</p>
              <p className="text-2xl font-bold text-gray-900">{merchants.reduce((acc, m) => acc + m.successful, 0)}</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <XCircle className="w-8 h-8 text-red-500" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Failed Reports</p>
              <p className="text-2xl font-bold text-gray-900">{merchants.reduce((acc, m) => acc + m.failed, 0)}</p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <Clock className="w-8 h-8 text-yellow-500" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Pending Reports</p>
              <p className="text-2xl font-bold text-gray-900">{merchants.reduce((acc, m) => acc + m.pending, 0)}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow mb-6 p-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
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
          <select
            className="rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            value={filters.status}
            onChange={e => setFilters(prev => ({ ...prev, status: e.target.value }))}
          >
            <option value="all">All Status</option>
            <option value="success">Success Only</option>
            <option value="failed">Has Failures</option>
            <option value="pending">Has Pending</option>
          </select>
          <select
            className="rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            value={filters.reportType}
            onChange={e => setFilters(prev => ({ ...prev, reportType: e.target.value }))}
          >
            <option value="all">All Report Types</option>
            <option value="financial">Financial Reports</option>
            <option value="transaction">Transaction Reports</option>
            <option value="daily">Daily Reports</option>
          </select>
          <select
            className="rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            value={filters.dateRange}
            onChange={e => setFilters(prev => ({ ...prev, dateRange: e.target.value }))}
          >
            <option value="today">Today</option>
            <option value="week">This Week</option>
            <option value="month">This Month</option>
          </select>
        </div>
      </div>

      {/* Main Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant Details</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Report Status</th>
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
                          <div className="text-sm font-medium text-gray-900">{merchant.merchantName}</div>
                          <div className="text-sm text-gray-500">MAID: {merchant.maid}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex space-x-2">
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                          {merchant.successful} Success
                        </span>
                        {merchant.failed > 0 && (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                            {merchant.failed} Failed
                          </span>
                        )}
                        {merchant.pending > 0 && (
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                            {merchant.pending} Pending
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          merchant.slaStatus === 'within' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {merchant.slaStatus === 'within' ? 'Within SLA' : 'SLA Breach'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formatTime(merchant.lastUpdate)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button className="text-blue-600 hover:text-blue-900 mr-3">
                        <Eye className="w-4 h-4" />
                      </button>
                      <button className="text-green-600 hover:text-green-900 mr-3">
                        <RefreshCw className="w-4 h-4" />
                      </button>
                      <button className="text-gray-600 hover:text-gray-900">
                        <Settings className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>

                  {/* Expanded Row Content */}
                  {expandedRows.has(merchant.id) && (
                    <tr>
                      <td colSpan={5} className="px-6 py-4 bg-gray-50">
                        <div className="space-y-4">
                          <h4 className="font-medium text-gray-900">Report Lifecycle Details</h4>
                          <div className="grid gap-4">
                            {merchant.reports.map(report => (
                              <div key={report.id} className="bg-white rounded-lg p-4 border border-gray-200">
                                <div className="flex items-center justify-between mb-3">
                                  <div className="flex items-center space-x-3">
                                    <span
                                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(report.status)}`}
                                    >
                                      {getStatusIcon(report.status)}
                                      <span className="ml-1">{report.status.toUpperCase()}</span>
                                    </span>
                                    <span className="font-medium">{report.type}</span>
                                    <span className="text-sm text-gray-500">ID: {report.id}</span>
                                  </div>
                                  <div className="flex space-x-2">
                                    {report.status === 'failed' && (
                                      <button
                                        onClick={() => handleRetry(merchant, report)}
                                        className="inline-flex items-center px-3 py-1 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                                      >
                                        <Play className="w-3 h-3 mr-1" />
                                        Retry
                                      </button>
                                    )}
                                    <button className="inline-flex items-center px-3 py-1 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                                      <Download className="w-3 h-3 mr-1" />
                                      Download
                                    </button>
                                  </div>
                                </div>

                                {/* Pipeline Status */}
                                <div className="mb-3">
                                  <div className="flex items-center space-x-4 text-sm">
                                    <div className="flex items-center">
                                      <span
                                        className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${getStatusColor(report.rdmStatus)}`}
                                      >
                                        RDM: {report.rdmStatus}
                                      </span>
                                    </div>
                                    <div className="flex items-center">
                                      <span
                                        className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${getStatusColor(report.reportBuilderStatus)}`}
                                      >
                                        Report Builder: {report.reportBuilderStatus}
                                      </span>
                                    </div>
                                  </div>
                                </div>

                                {/* Progress Bar for Processing Reports */}
                                {report.status === 'processing' && (
                                  <div className="mb-3">
                                    <div className="flex justify-between text-sm text-gray-600 mb-1">
                                      <span>Progress</span>
                                      <span>{Math.round(calculateProgress(report.scheduledTime, report.sla))}%</span>
                                    </div>
                                    <div className="w-full bg-gray-200 rounded-full h-2">
                                      <div
                                        className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                                        style={{ width: `${calculateProgress(report.scheduledTime, report.sla)}%` }}
                                      ></div>
                                    </div>
                                  </div>
                                )}

                                {/* Timing Information */}
                                <div className="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm text-gray-600 mb-3">
                                  <div>
                                    <span className="font-medium">Scheduled:</span>
                                    <div>{formatTime(report.scheduledTime)}</div>
                                  </div>
                                  <div>
                                    <span className="font-medium">Completed:</span>
                                    <div>{formatTime(report.completedTime)}</div>
                                  </div>
                                  <div>
                                    <span className="font-medium">SLA:</span>
                                    <div>{report.sla} minutes</div>
                                  </div>
                                </div>

                                {/* Error Details */}
                                {report.errors.length > 0 && (
                                  <div>
                                    <span className="font-medium text-red-600 text-sm">Errors:</span>
                                    <ul className="mt-1 text-sm text-red-600">
                                      {report.errors.map((error, index) => (
                                        <li key={index} className="flex items-center">
                                          <AlertCircle className="w-3 h-3 mr-1" />
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

      {/* Retry Modal */}
      {showRetryModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <div className="flex items-center">
                <Play className="w-6 h-6 text-blue-600 mr-2" />
                <h3 className="text-lg font-medium text-gray-900">Retry Report</h3>
              </div>
              <div className="mt-4">
                <p className="text-sm text-gray-500">
                  Are you sure you want to retry the report "{retryItem?.report?.type}" for merchant "{retryItem?.merchant?.merchantName}"?
                </p>
                <div className="mt-4 bg-yellow-50 border border-yellow-200 rounded-md p-3">
                  <div className="flex">
                    <AlertCircle className="w-5 h-5 text-yellow-600 mr-2" />
                    <div className="text-sm text-yellow-700">
                      This will restart the entire report generation process from RDM data production.
                    </div>
                  </div>
                </div>
              </div>
              <div className="flex justify-end space-x-3 mt-6">
                <button
                  onClick={() => setShowRetryModal(false)}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  onClick={executeRetry}
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700"
                >
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

export default MerchantReportingDashboard;
