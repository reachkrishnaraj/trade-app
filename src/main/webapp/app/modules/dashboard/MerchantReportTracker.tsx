import React, { useState, useEffect } from 'react';
import { Search, Filter, Download, RefreshCw, AlertCircle, CheckCircle, Clock, XCircle } from 'lucide-react';

const MerchantReportTracker = () => {
  const [reports, setReports] = useState([]);
  const [filteredReports, setFilteredReports] = useState([]);
  const [filters, setFilters] = useState({
    merchantName: '',
    merchantId: '',
    merchantSubAccId: '',
    status: '',
    outOfSla: '',
    fromDateTime: '',
    toDateTime: '',
  });
  const [lastUpdated, setLastUpdated] = useState(new Date());

  // Sample data - in real app this would come from API
  const sampleReports = [
    {
      id: 1,
      merchantName: 'TechCorp Solutions',
      merchantId: 'TECH001',
      merchantSubAccId: 'SUB001',
      reportType: 'Daily Transaction Report',
      expectedDateTime: '2025-06-02T09:00:00',
      status: 'Delivered',
      failures: null,
      delayReason: null,
      delayedByDuration: null,
      lastUpdateDateTime: '2025-06-02T09:15:00',
      deliveredDateTime: '2025-06-02T09:15:00',
      validationFailures: null,
      outOfSla: false,
    },
    {
      id: 2,
      merchantName: 'Global Retail Inc',
      merchantId: 'GLOB002',
      merchantSubAccId: 'SUB002',
      reportType: 'Weekly Summary',
      expectedDateTime: '2025-06-02T10:00:00',
      status: 'Delayed',
      failures: ['Network timeout'],
      delayReason: 'System maintenance',
      delayedByDuration: '2 hours 30 minutes',
      lastUpdateDateTime: '2025-06-02T12:30:00',
      deliveredDateTime: null,
      validationFailures: null,
      outOfSla: true,
    },
    {
      id: 3,
      merchantName: 'FastPay Services',
      merchantId: 'FAST003',
      merchantSubAccId: 'SUB003',
      reportType: 'Monthly Analytics',
      expectedDateTime: '2025-06-02T08:00:00',
      status: 'Failed',
      failures: ['Data validation error', 'Missing required fields'],
      delayReason: 'Data integrity issues',
      delayedByDuration: '4 hours 15 minutes',
      lastUpdateDateTime: '2025-06-02T12:15:00',
      deliveredDateTime: null,
      validationFailures: ['Invalid merchant ID format', 'Missing transaction dates'],
      outOfSla: true,
    },
    {
      id: 4,
      merchantName: 'E-Commerce Hub',
      merchantId: 'ECOM004',
      merchantSubAccId: 'SUB004',
      reportType: 'Daily Transaction Report',
      expectedDateTime: '2025-06-02T11:00:00',
      status: 'Processing',
      failures: null,
      delayReason: null,
      delayedByDuration: null,
      lastUpdateDateTime: '2025-06-02T11:30:00',
      deliveredDateTime: null,
      validationFailures: null,
      outOfSla: false,
    },
    {
      id: 5,
      merchantName: 'Digital Payments Co',
      merchantId: 'DIGI005',
      merchantSubAccId: 'SUB005',
      reportType: 'Risk Assessment Report',
      expectedDateTime: '2025-06-02T07:30:00',
      status: 'Delivered',
      failures: null,
      delayReason: null,
      delayedByDuration: null,
      lastUpdateDateTime: '2025-06-02T07:25:00',
      deliveredDateTime: '2025-06-02T07:25:00',
      validationFailures: null,
      outOfSla: false,
    },
  ];

  useEffect(() => {
    setReports(sampleReports);
    setFilteredReports(sampleReports);

    // Simulate real-time updates
    const interval = setInterval(() => {
      setLastUpdated(new Date());
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    applyFilters();
  }, [filters, reports]);

  const applyFilters = () => {
    let filtered = reports.filter(report => {
      const matchesMerchantName = !filters.merchantName || report.merchantName.toLowerCase().includes(filters.merchantName.toLowerCase());
      const matchesMerchantId = !filters.merchantId || report.merchantId.toLowerCase().includes(filters.merchantId.toLowerCase());
      const matchesSubAccId =
        !filters.merchantSubAccId || report.merchantSubAccId.toLowerCase().includes(filters.merchantSubAccId.toLowerCase());
      const matchesStatus = !filters.status || report.status === filters.status;
      const matchesOutOfSla =
        filters.outOfSla === '' || (filters.outOfSla === 'true' && report.outOfSla) || (filters.outOfSla === 'false' && !report.outOfSla);

      let matchesDateRange = true;
      if (filters.fromDateTime || filters.toDateTime) {
        const reportDate = new Date(report.expectedDateTime);
        if (filters.fromDateTime) {
          matchesDateRange = matchesDateRange && reportDate >= new Date(filters.fromDateTime);
        }
        if (filters.toDateTime) {
          matchesDateRange = matchesDateRange && reportDate <= new Date(filters.toDateTime);
        }
      }

      return matchesMerchantName && matchesMerchantId && matchesSubAccId && matchesStatus && matchesOutOfSla && matchesDateRange;
    });

    setFilteredReports(filtered);
  };

  const handleFilterChange = (field, value) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  const clearFilters = () => {
    setFilters({
      merchantName: '',
      merchantId: '',
      merchantSubAccId: '',
      status: '',
      outOfSla: '',
      fromDateTime: '',
      toDateTime: '',
    });
  };

  const getStatusIcon = status => {
    switch (status) {
      case 'Delivered':
        return <CheckCircle className="w-4 h-4 text-green-600" />;
      case 'Processing':
        return <Clock className="w-4 h-4 text-blue-600" />;
      case 'Delayed':
        return <AlertCircle className="w-4 h-4 text-yellow-600" />;
      case 'Failed':
        return <XCircle className="w-4 h-4 text-red-600" />;
      default:
        return <Clock className="w-4 h-4 text-gray-600" />;
    }
  };

  const getStatusBadge = (status, outOfSla) => {
    const baseClasses = 'px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1';
    let statusClasses = '';

    switch (status) {
      case 'Delivered':
        statusClasses = 'bg-green-100 text-green-800';
        break;
      case 'Processing':
        statusClasses = 'bg-blue-100 text-blue-800';
        break;
      case 'Delayed':
        statusClasses = 'bg-yellow-100 text-yellow-800';
        break;
      case 'Failed':
        statusClasses = 'bg-red-100 text-red-800';
        break;
      default:
        statusClasses = 'bg-gray-100 text-gray-800';
    }

    return (
      <div className="flex items-center gap-2">
        <span className={`${baseClasses} ${statusClasses}`}>
          {getStatusIcon(status)}
          {status}
        </span>
        {outOfSla && <span className="px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">OUT OF SLA</span>}
      </div>
    );
  };

  const formatDateTime = dateTime => {
    if (!dateTime) return 'N/A';
    return new Date(dateTime).toLocaleString();
  };

  const getStats = () => {
    const total = filteredReports.length;
    const delivered = filteredReports.filter(r => r.status === 'Delivered').length;
    const processing = filteredReports.filter(r => r.status === 'Processing').length;
    const delayed = filteredReports.filter(r => r.status === 'Delayed').length;
    const failed = filteredReports.filter(r => r.status === 'Failed').length;
    const outOfSla = filteredReports.filter(r => r.outOfSla).length;

    return { total, delivered, processing, delayed, failed, outOfSla };
  };

  const stats = getStats();

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Merchant Report Tracker</h1>
              <p className="text-gray-600 mt-1">Real-time monitoring of merchant reporting schedules</p>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-sm text-gray-500">Last updated: {lastUpdated.toLocaleTimeString()}</div>
              <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                <RefreshCw className="w-4 h-4" />
                Refresh
              </button>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-6 gap-4 mb-6">
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <div className="text-2xl font-bold text-gray-900">{stats.total}</div>
            <div className="text-sm text-gray-600">Total Reports</div>
          </div>
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <div className="text-2xl font-bold text-green-600">{stats.delivered}</div>
            <div className="text-sm text-gray-600">Delivered</div>
          </div>
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <div className="text-2xl font-bold text-blue-600">{stats.processing}</div>
            <div className="text-sm text-gray-600">Processing</div>
          </div>
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <div className="text-2xl font-bold text-yellow-600">{stats.delayed}</div>
            <div className="text-sm text-gray-600">Delayed</div>
          </div>
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <div className="text-2xl font-bold text-red-600">{stats.failed}</div>
            <div className="text-sm text-gray-600">Failed</div>
          </div>
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <div className="text-2xl font-bold text-red-600">{stats.outOfSla}</div>
            <div className="text-sm text-gray-600">Out of SLA</div>
          </div>
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center gap-2 mb-4">
            <Filter className="w-5 h-5 text-gray-600" />
            <h3 className="text-lg font-semibold text-gray-900">Filters</h3>
          </div>

          <div className="grid grid-cols-4 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Merchant Name</label>
              <input
                type="text"
                value={filters.merchantName}
                onChange={e => handleFilterChange('merchantName', e.target.value)}
                placeholder="Search merchant name..."
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Merchant ID</label>
              <input
                type="text"
                value={filters.merchantId}
                onChange={e => handleFilterChange('merchantId', e.target.value)}
                placeholder="Search merchant ID..."
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Sub Account ID</label>
              <input
                type="text"
                value={filters.merchantSubAccId}
                onChange={e => handleFilterChange('merchantSubAccId', e.target.value)}
                placeholder="Search sub account ID..."
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
              <select
                value={filters.status}
                onChange={e => handleFilterChange('status', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">All Statuses</option>
                <option value="Delivered">Delivered</option>
                <option value="Processing">Processing</option>
                <option value="Delayed">Delayed</option>
                <option value="Failed">Failed</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Out of SLA</label>
              <select
                value={filters.outOfSla}
                onChange={e => handleFilterChange('outOfSla', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">All</option>
                <option value="true">Out of SLA</option>
                <option value="false">Within SLA</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">From Date & Time</label>
              <input
                type="datetime-local"
                value={filters.fromDateTime}
                onChange={e => handleFilterChange('fromDateTime', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">To Date & Time</label>
              <input
                type="datetime-local"
                value={filters.toDateTime}
                onChange={e => handleFilterChange('toDateTime', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>

          <div className="mt-4">
            <button
              onClick={clearFilters}
              className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-md transition-colors"
            >
              Clear All Filters
            </button>
          </div>
        </div>

        {/* Reports Table */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant Info</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Report Details</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Timeline</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Issues</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredReports.map(report => (
                  <tr key={report.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">{report.merchantName}</div>
                        <div className="text-sm text-gray-500">ID: {report.merchantId}</div>
                        <div className="text-sm text-gray-500">Sub: {report.merchantSubAccId}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">{report.reportType}</div>
                        <div className="text-sm text-gray-500">Expected: {formatDateTime(report.expectedDateTime)}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(report.status, report.outOfSla)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <div>
                        <div>Last Update: {formatDateTime(report.lastUpdateDateTime)}</div>
                        {report.deliveredDateTime && <div>Delivered: {formatDateTime(report.deliveredDateTime)}</div>}
                        {report.delayedByDuration && <div className="text-red-600">Delayed by: {report.delayedByDuration}</div>}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm">
                        {report.failures && report.failures.length > 0 && (
                          <div className="mb-2">
                            <div className="font-medium text-red-600">Failures:</div>
                            {report.failures.map((failure, idx) => (
                              <div key={idx} className="text-red-600 text-xs">
                                • {failure}
                              </div>
                            ))}
                          </div>
                        )}
                        {report.validationFailures && report.validationFailures.length > 0 && (
                          <div className="mb-2">
                            <div className="font-medium text-orange-600">Validation Issues:</div>
                            {report.validationFailures.map((failure, idx) => (
                              <div key={idx} className="text-orange-600 text-xs">
                                • {failure}
                              </div>
                            ))}
                          </div>
                        )}
                        {report.delayReason && (
                          <div>
                            <div className="font-medium text-yellow-600">Delay Reason:</div>
                            <div className="text-yellow-600 text-xs">{report.delayReason}</div>
                          </div>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button
                        disabled={report.status !== 'Delivered'}
                        className={`inline-flex items-center gap-1 px-3 py-1 rounded-md text-sm ${
                          report.status === 'Delivered'
                            ? 'bg-blue-600 text-white hover:bg-blue-700'
                            : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                        } transition-colors`}
                      >
                        <Download className="w-4 h-4" />
                        Download
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredReports.length === 0 && (
            <div className="text-center py-12">
              <div className="text-gray-500 text-lg">No reports found matching your filters</div>
              <div className="text-gray-400 text-sm mt-2">Try adjusting your search criteria</div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MerchantReportTracker;
