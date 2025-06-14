import React from 'react';
import SignalActionsTable from './SignalActionsTable';
import './SignalActionsPage.scss';

const SignalActionsPage = () => {
  return (
    <div className="signal-actions-page">
      <div className="page-header">
        <h1 className="page-title">Signal Actions</h1>
        <p className="page-description">Monitor and manage trading signals with real-time updates and notifications</p>
      </div>
      <div className="page-content">
        <SignalActionsTable />
      </div>
    </div>
  );
};

export default SignalActionsPage;
