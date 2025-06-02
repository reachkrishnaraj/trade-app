import React, { useState, useEffect, CSSProperties } from 'react';
import {
  Clock,
  Play,
  Pause,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Activity,
  Calendar,
  Server,
  RefreshCw,
  Eye,
  TrendingUp,
  Database,
} from 'lucide-react';

const AirflowEMRDashboard = () => {
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [filterStatus, setFilterStatus] = useState('all');
  const [refreshing, setRefreshing] = useState(false);
  const [lastRefresh, setLastRefresh] = useState(new Date());

  // Comprehensive styles that work without Bootstrap
  const styles: { [key: string]: CSSProperties } = {
    container: {
      padding: '24px',
      backgroundColor: '#f8f9fa',
      minHeight: '100vh',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    },
    header: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginBottom: '24px',
    },
    title: {
      fontSize: '2rem',
      fontWeight: 'bold',
      color: '#212529',
      margin: 0,
    },
    refreshButton: {
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      padding: '8px 16px',
      backgroundColor: '#007bff',
      color: 'white',
      border: 'none',
      borderRadius: '4px',
      cursor: 'pointer',
      fontSize: '14px',
      fontWeight: '500',
    },
    refreshButtonDisabled: {
      opacity: 0.6,
      cursor: 'not-allowed',
    },
    lastUpdate: {
      color: '#6c757d',
      fontSize: '14px',
      marginBottom: '24px',
    },
    statsGrid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: '16px',
      marginBottom: '24px',
    },
    statCard: {
      backgroundColor: 'white',
      padding: '20px',
      borderRadius: '8px',
      boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
      border: '1px solid #e9ecef',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
    },
    statNumber: {
      fontSize: '2rem',
      fontWeight: 'bold',
      margin: 0,
    },
    statLabel: {
      fontSize: '14px',
      color: '#6c757d',
      margin: '0 0 8px 0',
    },
    filtersContainer: {
      marginBottom: '24px',
      display: 'flex',
      flexWrap: 'wrap' as const,
      gap: '8px',
    },
    filterButton: {
      padding: '6px 12px',
      border: '1px solid #dee2e6',
      borderRadius: '20px',
      backgroundColor: 'white',
      color: '#495057',
      cursor: 'pointer',
      fontSize: '14px',
      fontWeight: '500',
      textTransform: 'capitalize' as const,
    },
    filterButtonActive: {
      backgroundColor: '#007bff',
      color: 'white',
      borderColor: '#007bff',
    },
    mainGrid: {
      display: 'grid',
      gridTemplateColumns: '1fr 1fr',
      gap: '24px',
    },
    card: {
      backgroundColor: 'white',
      borderRadius: '8px',
      boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
      border: '1px solid #e9ecef',
      overflow: 'hidden',
    },
    cardHeader: {
      padding: '16px 20px',
      borderBottom: '1px solid #e9ecef',
      backgroundColor: '#fff',
    },
    cardTitle: {
      fontSize: '1.1rem',
      fontWeight: '600',
      color: '#212529',
      margin: 0,
    },
    cardBody: {
      padding: '20px',
    },
    jobsList: {
      maxHeight: '600px',
      overflowY: 'auto' as const,
    },
    jobItem: {
      padding: '16px 20px',
      borderBottom: '1px solid #e9ecef',
      cursor: 'pointer',
      transition: 'background-color 0.2s',
    },
    jobItemHover: {
      backgroundColor: '#f8f9fa',
    },
    jobItemSelected: {
      backgroundColor: '#e3f2fd',
    },
    jobHeader: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginBottom: '8px',
    },
    jobName: {
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
    },
    jobTitle: {
      fontSize: '16px',
      fontWeight: '500',
      color: '#212529',
      margin: 0,
    },
    badge: {
      padding: '4px 8px',
      borderRadius: '12px',
      fontSize: '12px',
      fontWeight: '500',
      textTransform: 'capitalize' as const,
    },
    badgeRunning: {
      backgroundColor: '#cce5ff',
      color: '#004085',
    },
    badgeSuccess: {
      backgroundColor: '#d4edda',
      color: '#155724',
    },
    badgeFailed: {
      backgroundColor: '#f8d7da',
      color: '#721c24',
    },
    badgeScheduled: {
      backgroundColor: '#fff3cd',
      color: '#856404',
    },
    badgePending: {
      backgroundColor: '#e2e3e5',
      color: '#383d41',
    },
    jobMeta: {
      display: 'flex',
      alignItems: 'center',
      gap: '16px',
      fontSize: '14px',
      color: '#6c757d',
      marginBottom: '8px',
    },
    progressContainer: {
      marginTop: '8px',
    },
    progressLabel: {
      display: 'flex',
      justifyContent: 'space-between',
      fontSize: '12px',
      color: '#6c757d',
      marginBottom: '4px',
    },
    progressBar: {
      height: '6px',
      backgroundColor: '#e9ecef',
      borderRadius: '3px',
      overflow: 'hidden',
    },
    progressFill: {
      height: '100%',
      backgroundColor: '#007bff',
      transition: 'width 0.3s ease',
    },
    detailsContainer: {
      padding: 0,
    },
    detailsHeader: {
      display: 'flex',
      alignItems: 'center',
      gap: '12px',
      marginBottom: '24px',
    },
    detailsTitle: {
      fontSize: '1.2rem',
      fontWeight: '600',
      color: '#212529',
      margin: 0,
    },
    infoGrid: {
      display: 'grid',
      gridTemplateColumns: '1fr 1fr',
      gap: '16px',
      marginBottom: '24px',
    },
    infoItem: {
      display: 'flex',
      flexDirection: 'column' as const,
    },
    infoLabel: {
      fontSize: '12px',
      color: '#6c757d',
      fontWeight: '500',
      marginBottom: '4px',
      textTransform: 'uppercase' as const,
    },
    infoValue: {
      fontSize: '14px',
      color: '#212529',
      fontWeight: '500',
    },
    alertDanger: {
      padding: '16px',
      backgroundColor: '#f8d7da',
      border: '1px solid #f5c6cb',
      borderRadius: '4px',
      marginBottom: '24px',
    },
    alertTitle: {
      fontSize: '16px',
      fontWeight: '600',
      color: '#721c24',
      marginBottom: '8px',
    },
    alertText: {
      fontSize: '14px',
      color: '#721c24',
      margin: '4px 0',
    },
    tasksList: {
      marginBottom: '24px',
    },
    tasksTitle: {
      fontSize: '16px',
      fontWeight: '600',
      color: '#212529',
      marginBottom: '12px',
    },
    taskItem: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: '12px',
      backgroundColor: '#f8f9fa',
      borderRadius: '4px',
      marginBottom: '8px',
    },
    taskLeft: {
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
    },
    taskName: {
      fontSize: '14px',
      fontWeight: '500',
      color: '#212529',
    },
    taskDuration: {
      fontSize: '12px',
      color: '#6c757d',
    },
    logsContainer: {
      backgroundColor: '#212529',
      color: '#28a745',
      padding: '16px',
      borderRadius: '4px',
      fontFamily: 'Consolas, Monaco, "Courier New", monospace',
      fontSize: '12px',
      maxHeight: '200px',
      overflowY: 'auto' as const,
      whiteSpace: 'pre-wrap' as const,
    },
    emptyState: {
      textAlign: 'center' as const,
      padding: '60px 20px',
      color: '#6c757d',
    },
    emptyIcon: {
      marginBottom: '16px',
    },
  };

  // Mock data - replace with actual API calls
  const mockJobs = [
    {
      id: 'dag_1',
      name: 'Data Processing Pipeline',
      status: 'running',
      progress: 65,
      cluster: 'j-ABC123XYZ',
      schedule: '0 2 * * *',
      nextRun: '2025-05-28T02:00:00Z',
      lastRun: '2025-05-27T02:00:00Z',
      duration: '45m 32s',
      tasks: [
        { id: 'extract_data', name: 'Extract Data', status: 'success', duration: '12m 15s' },
        { id: 'transform_data', name: 'Transform Data', status: 'running', duration: '23m 45s' },
        { id: 'load_data', name: 'Load Data', status: 'pending', duration: '-' },
        { id: 'validate_data', name: 'Validate Data', status: 'pending', duration: '-' },
      ],
      logs: 'INFO: Starting data extraction...\nINFO: Processing 1.2M records...\nINFO: Transform stage initiated...',
      failureDetails: null,
    },
    {
      id: 'dag_2',
      name: 'ML Model Training',
      status: 'failed',
      progress: 30,
      cluster: 'j-DEF456UVW',
      schedule: '0 6 * * 1',
      nextRun: '2025-06-02T06:00:00Z',
      lastRun: '2025-05-26T06:00:00Z',
      duration: '1h 23m',
      tasks: [
        { id: 'data_prep', name: 'Data Preparation', status: 'success', duration: '15m 30s' },
        { id: 'feature_eng', name: 'Feature Engineering', status: 'success', duration: '25m 45s' },
        { id: 'model_train', name: 'Model Training', status: 'failed', duration: '42m 12s' },
        { id: 'model_eval', name: 'Model Evaluation', status: 'upstream_failed', duration: '-' },
      ],
      logs: 'ERROR: OutOfMemoryError during model training\nERROR: Insufficient cluster resources\nINFO: Attempting retry...',
      failureDetails: {
        reason: 'OutOfMemoryError',
        message: 'Java heap space exceeded during model training phase',
        logFile: 's3://your-bucket/logs/model-training-error.log',
      },
    },
    {
      id: 'dag_3',
      name: 'Daily ETL Process',
      status: 'success',
      progress: 100,
      cluster: 'j-GHI789RST',
      schedule: '0 1 * * *',
      nextRun: '2025-05-28T01:00:00Z',
      lastRun: '2025-05-27T01:00:00Z',
      duration: '28m 15s',
      tasks: [
        { id: 'extract_db', name: 'Extract from DB', status: 'success', duration: '8m 22s' },
        { id: 'clean_data', name: 'Clean Data', status: 'success', duration: '12m 35s' },
        { id: 'load_warehouse', name: 'Load to Warehouse', status: 'success', duration: '7m 18s' },
      ],
      logs: 'INFO: ETL process completed successfully\nINFO: Processed 850K records\nINFO: Data validation passed',
      failureDetails: null,
    },
    {
      id: 'dag_4',
      name: 'Report Generation',
      status: 'scheduled',
      progress: 0,
      cluster: 'j-JKL012MNO',
      schedule: '0 8 * * 1-5',
      nextRun: '2025-05-28T08:00:00Z',
      lastRun: '2025-05-27T08:00:00Z',
      duration: '15m 42s',
      tasks: [
        { id: 'gather_metrics', name: 'Gather Metrics', status: 'pending', duration: '-' },
        { id: 'generate_report', name: 'Generate Report', status: 'pending', duration: '-' },
        { id: 'send_email', name: 'Send Email', status: 'pending', duration: '-' },
      ],
      logs: 'INFO: Waiting for scheduled execution time...',
      failureDetails: null,
    },
  ];

  useEffect(() => {
    setJobs(mockJobs);
  }, []);

  const getStatusIcon = status => {
    const iconProps = { size: 16 };
    switch (status) {
      case 'running':
        return <Activity {...iconProps} style={{ color: '#007bff' }} />;
      case 'success':
        return <CheckCircle {...iconProps} style={{ color: '#28a745' }} />;
      case 'failed':
        return <XCircle {...iconProps} style={{ color: '#dc3545' }} />;
      case 'scheduled':
        return <Clock {...iconProps} style={{ color: '#ffc107' }} />;
      case 'pending':
        return <Pause {...iconProps} style={{ color: '#6c757d' }} />;
      case 'upstream_failed':
        return <AlertTriangle {...iconProps} style={{ color: '#fd7e14' }} />;
      default:
        return <Clock {...iconProps} style={{ color: '#6c757d' }} />;
    }
  };

  const getBadgeStyle = status => {
    switch (status) {
      case 'running':
        return { ...styles.badge, ...styles.badgeRunning };
      case 'success':
        return { ...styles.badge, ...styles.badgeSuccess };
      case 'failed':
        return { ...styles.badge, ...styles.badgeFailed };
      case 'scheduled':
        return { ...styles.badge, ...styles.badgeScheduled };
      case 'pending':
        return { ...styles.badge, ...styles.badgePending };
      case 'upstream_failed':
        return { ...styles.badge, ...styles.badgeScheduled };
      default:
        return { ...styles.badge, ...styles.badgePending };
    }
  };

  const filteredJobs = jobs.filter(job => filterStatus === 'all' || job.status === filterStatus);

  const handleRefresh = async () => {
    setRefreshing(true);
    setTimeout(() => {
      setLastRefresh(new Date());
      setRefreshing(false);
    }, 1000);
  };

  const formatTime = dateString => {
    return new Date(dateString).toLocaleString();
  };

  const getJobStats = () => {
    const total = jobs.length;
    const running = jobs.filter(j => j.status === 'running').length;
    const failed = jobs.filter(j => j.status === 'failed').length;
    const success = jobs.filter(j => j.status === 'success').length;

    return { total, running, failed, success };
  };

  const stats = getJobStats();

  return (
    <div style={styles.container}>
      {/* Header */}
      <div style={styles.header}>
        <h1 style={styles.title}>Airflow EMR Dashboard</h1>
        <button
          style={{
            ...styles.refreshButton,
            ...(refreshing ? styles.refreshButtonDisabled : {}),
          }}
          onClick={handleRefresh}
          disabled={refreshing}
        >
          <RefreshCw size={16} />
          {refreshing ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>

      <div style={styles.lastUpdate}>Last updated: {lastRefresh.toLocaleString()}</div>

      {/* Stats Cards */}
      <div style={styles.statsGrid}>
        <div style={styles.statCard}>
          <div>
            <p style={styles.statLabel}>Total Jobs</p>
            <h3 style={styles.statNumber}>{stats.total}</h3>
          </div>
          <Database size={32} style={{ color: '#6c757d' }} />
        </div>

        <div style={styles.statCard}>
          <div>
            <p style={styles.statLabel}>Running</p>
            <h3 style={{ ...styles.statNumber, color: '#007bff' }}>{stats.running}</h3>
          </div>
          <Activity size={32} style={{ color: '#007bff' }} />
        </div>

        <div style={styles.statCard}>
          <div>
            <p style={styles.statLabel}>Failed</p>
            <h3 style={{ ...styles.statNumber, color: '#dc3545' }}>{stats.failed}</h3>
          </div>
          <XCircle size={32} style={{ color: '#dc3545' }} />
        </div>

        <div style={styles.statCard}>
          <div>
            <p style={styles.statLabel}>Successful</p>
            <h3 style={{ ...styles.statNumber, color: '#28a745' }}>{stats.success}</h3>
          </div>
          <CheckCircle size={32} style={{ color: '#28a745' }} />
        </div>
      </div>

      {/* Filters */}
      <div style={styles.filtersContainer}>
        {['all', 'running', 'failed', 'success', 'scheduled'].map(status => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            style={{
              ...styles.filterButton,
              ...(filterStatus === status ? styles.filterButtonActive : {}),
            }}
          >
            {status}
          </button>
        ))}
      </div>

      <div style={styles.mainGrid}>
        {/* Jobs List */}
        <div style={styles.card}>
          <div style={styles.cardHeader}>
            <h2 style={styles.cardTitle}>Jobs Overview</h2>
          </div>

          <div style={styles.jobsList}>
            {filteredJobs.map(job => (
              <div
                key={job.id}
                style={{
                  ...styles.jobItem,
                  ...(selectedJob?.id === job.id ? styles.jobItemSelected : {}),
                }}
                onClick={() => setSelectedJob(job)}
                onMouseEnter={e => {
                  if (selectedJob?.id !== job.id) {
                    (e.target as HTMLElement).style.backgroundColor = '#f8f9fa';
                  }
                }}
                onMouseLeave={e => {
                  if (selectedJob?.id !== job.id) {
                    (e.target as HTMLElement).style.backgroundColor = 'white';
                  }
                }}
              >
                <div style={styles.jobHeader}>
                  <div style={styles.jobName}>
                    {getStatusIcon(job.status)}
                    <h3 style={styles.jobTitle}>{job.name}</h3>
                  </div>
                  <span style={getBadgeStyle(job.status)}>{job.status}</span>
                </div>

                <div style={styles.jobMeta}>
                  <span>
                    <Server size={12} style={{ marginRight: '4px' }} />
                    {job.cluster}
                  </span>
                  <span>
                    <Clock size={12} style={{ marginRight: '4px' }} />
                    {job.duration}
                  </span>
                </div>

                {job.status === 'running' && (
                  <div style={styles.progressContainer}>
                    <div style={styles.progressLabel}>
                      <span>Progress</span>
                      <span>{job.progress}%</span>
                    </div>
                    <div style={styles.progressBar}>
                      <div
                        style={{
                          ...styles.progressFill,
                          width: `${job.progress}%`,
                        }}
                      />
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Job Details */}
        <div style={styles.card}>
          <div style={styles.cardHeader}>
            <h2 style={styles.cardTitle}>Job Details</h2>
          </div>

          {selectedJob ? (
            <div style={styles.cardBody}>
              <div style={styles.detailsHeader}>
                {getStatusIcon(selectedJob.status)}
                <h3 style={styles.detailsTitle}>{selectedJob.name}</h3>
                <span style={getBadgeStyle(selectedJob.status)}>{selectedJob.status}</span>
              </div>

              {/* Job Info */}
              <div style={styles.infoGrid}>
                <div style={styles.infoItem}>
                  <p style={styles.infoLabel}>EMR Cluster</p>
                  <p style={styles.infoValue}>{selectedJob.cluster}</p>
                </div>
                <div style={styles.infoItem}>
                  <p style={styles.infoLabel}>Duration</p>
                  <p style={styles.infoValue}>{selectedJob.duration}</p>
                </div>
                <div style={styles.infoItem}>
                  <p style={styles.infoLabel}>Schedule</p>
                  <p style={styles.infoValue}>{selectedJob.schedule}</p>
                </div>
                <div style={styles.infoItem}>
                  <p style={styles.infoLabel}>Next Run</p>
                  <p style={styles.infoValue}>{formatTime(selectedJob.nextRun)}</p>
                </div>
              </div>

              {/* Progress Bar */}
              {selectedJob.status === 'running' && (
                <div style={{ marginBottom: '24px' }}>
                  <div style={styles.progressLabel}>
                    <span>Overall Progress</span>
                    <span>{selectedJob.progress}%</span>
                  </div>
                  <div style={styles.progressBar}>
                    <div
                      style={{
                        ...styles.progressFill,
                        width: `${selectedJob.progress}%`,
                      }}
                    />
                  </div>
                </div>
              )}

              {/* Failure Details */}
              {selectedJob.failureDetails && (
                <div style={styles.alertDanger}>
                  <h4 style={styles.alertTitle}>Failure Details</h4>
                  <p style={styles.alertText}>
                    <strong>Reason:</strong> {selectedJob.failureDetails.reason}
                  </p>
                  <p style={styles.alertText}>
                    <strong>Message:</strong> {selectedJob.failureDetails.message}
                  </p>
                  <p style={styles.alertText}>
                    <strong>Log File:</strong> {selectedJob.failureDetails.logFile}
                  </p>
                </div>
              )}

              {/* Tasks */}
              <div style={styles.tasksList}>
                <h4 style={styles.tasksTitle}>Tasks</h4>
                {selectedJob.tasks.map(task => (
                  <div key={task.id} style={styles.taskItem}>
                    <div style={styles.taskLeft}>
                      {getStatusIcon(task.status)}
                      <span style={styles.taskName}>{task.name}</span>
                      <span style={getBadgeStyle(task.status)}>{task.status}</span>
                    </div>
                    <span style={styles.taskDuration}>{task.duration}</span>
                  </div>
                ))}
              </div>

              {/* Logs */}
              <div>
                <h4 style={styles.tasksTitle}>Recent Logs</h4>
                <div style={styles.logsContainer}>{selectedJob.logs}</div>
              </div>
            </div>
          ) : (
            <div style={styles.emptyState}>
              <div style={styles.emptyIcon}>
                <Eye size={48} style={{ color: '#dee2e6' }} />
              </div>
              <p>Select a job to view details</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AirflowEMRDashboard;
