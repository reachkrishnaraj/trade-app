import React from 'react';
import CurrentTradesTable from './CurrentTradesTable';
import SignalsTable from './SignalsTable';
import EventsByTimeframeTable from './EventsByTimeframeTable';
import './dashboard.scss';

const Dashboard = () => {
  return (
    <div className="dashboard">
      <h2 className="dashboard-title">Trading Dashboard</h2>
      <div className="dashboard-section">
        <CurrentTradesTable />
      </div>
      <div className="dashboard-section">
        <SignalsTable />
      </div>
      <div className="dashboard-section">
        <EventsByTimeframeTable />
      </div>
    </div>
  );
};

export default Dashboard;
