import React, { useRef } from 'react';
import SignalActionsTable from './SignalActionsTable';
import VoiceAnnouncements, { VoiceAnnouncementsRef } from './VoiceAnnouncements';
import './SignalActionsPage.scss';

const SignalActionsPage = () => {
  const voiceRef = useRef<VoiceAnnouncementsRef>(null);

  // Example function to demonstrate manual voice announcements
  const handleCustomAnnouncement = (message: string) => {
    voiceRef.current?.announceCustomMessage(message);
  };

  // You can also expose this to child components via props
  const handleNotification = (message: string, type: 'success' | 'info' | 'warning' | 'error') => {
    console.log(`[${type.toUpperCase()}] ${message}`);
    // Add your notification logic here
  };

  return (
    <div className="signal-actions-page">
      <div className="page-header">
        <h1 className="page-title">Signal Actions</h1>
        <p className="page-description">Monitor and manage trading signals with real-time updates and notifications</p>

        {/* Quick Action Buttons */}
        <div style={{ marginTop: '10px', display: 'flex', gap: '10px' }}>
          <button
            onClick={() => handleCustomAnnouncement('Bullish contrarian in Luxalgo signals')}
            style={{
              padding: '8px 16px',
              background: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            🗣️ Test Luxalgo Announcement
          </button>

          <button
            onClick={() => voiceRef.current?.testVoice()}
            style={{
              padding: '8px 16px',
              background: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            🎤 Test Voice Settings
          </button>
        </div>
      </div>

      {/* Voice Announcements Component */}
      <div className="page-content">
        <VoiceAnnouncements ref={voiceRef} onNotification={handleNotification} style={{ marginBottom: '20px' }} />

        {/* Pass voice ref to table if needed */}
        <SignalActionsTable voiceRef={voiceRef} />
      </div>
    </div>
  );
};

export default SignalActionsPage;
