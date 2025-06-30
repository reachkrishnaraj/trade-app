import React, { useState, useEffect } from 'react';
import { Storage } from 'react-jhipster';
import { Subscription } from 'rxjs';
import { connectWebSocket, disconnectWebSocket, subscribeToTopic, unsubscribeFromTopic } from 'app/utils/websocket-utils';
import { VoiceAnnouncementsRef, SignalData } from './VoiceAnnouncements'; // Import both types
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
  announce?: boolean; // 🗣️ Optional: Voice announcement flag
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

interface SignalActionWithReceived extends SignalAction {
  receivedAt?: number; // Timestamp when received by frontend
  // Inherits isAnnounce: boolean from SignalAction
}

// 🎯 ADD PROPS INTERFACE
interface SignalActionsTableProps {
  voiceRef?: React.MutableRefObject<VoiceAnnouncementsRef | null>;
}

// 🎯 UPDATE COMPONENT TO ACCEPT PROPS
const SignalActionsTable: React.FC<SignalActionsTableProps> = ({ voiceRef }) => {
  const [data, setData] = useState<SignalActionWithReceived[]>([]);
  const [filteredData, setFilteredData] = useState<SignalActionWithReceived[]>([]);
  const [loading, setLoading] = useState(true);
  const [updated, setUpdated] = useState(false);
  const [minimized, setMinimized] = useState(false);
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [notifications, setNotifications] = useState<Notification[]>([]);

  // Banner notification state
  const [bannerVisible, setBannerVisible] = useState(false);
  const [newEventCount, setNewEventCount] = useState(0);

  // Keep track of received timestamps to persist across data updates
  const [receivedTimestamps, setReceivedTimestamps] = useState<Map<number, number>>(new Map());

  // Audio customization settings
  const [audioSettings, setAudioSettings] = useState({
    duration: 2.5, // 2.5 seconds
    frequency: 800, // 800Hz
    volume: 0.15, // 15% volume
    pattern: 'ding', // 'ding', 'chime', 'bell', 'melody'
  });

  // Simple audio state
  const [audioEnabled, setAudioEnabled] = useState(true);

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

  // 🗣️ Helper function to convert signal for voice announcements
  const convertSignalForVoice = (signal: SignalActionWithReceived): SignalData => {
    console.log(`🗣️ Converting signal JSON for voice: ${JSON.stringify(signal)}`);
    return {
      id: signal.id,
      symbol: signal.symbol,
      direction: signal.direction,
      indicatorName: signal.indicatorName,
      price: signal.price,
      interval: signal.interval,
      message: signal.message,
      announce: signal.announce,
    };
  };

  // Notification management
  const addNotification = (message: string, type: 'success' | 'info' | 'warning' | 'error' = 'info') => {
    const notification: Notification = {
      id: Date.now(),
      message,
      type,
      timestamp: Date.now(),
    };

    setNotifications(prev => [notification, ...prev]);

    // Removed auto-dismiss - notifications stay until manually closed
  };

  const removeNotification = (id: number) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  // Simple function to enable audio
  const initializeAudio = () => {
    try {
      // Test if Web Audio API is available
      if (!window.AudioContext && !(window as any).webkitAudioContext) {
        throw new Error('Web Audio API not supported');
      }

      setAudioEnabled(true);

      // Show success notification
      addNotification('🔊 Audio notifications enabled! Testing with current settings...', 'success');

      // Play immediate test sound with current settings
      setTimeout(() => {
        playNotificationSound();
      }, 500);
    } catch (error) {
      addNotification('⚠️ Audio not supported in this browser', 'warning');
    }
  };

  // Enhanced notification sound with customizable patterns
  const playNotificationSound = async () => {
    if (!audioEnabled) {
      return;
    }

    try {
      // Create fresh AudioContext each time
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();

      // Resume if suspended
      if (audioContext.state === 'suspended') {
        await audioContext.resume();
      }

      // Create different sound patterns based on user preference
      switch (audioSettings.pattern) {
        case 'ding':
          playDingPattern(audioContext);
          break;
        case 'chime':
          playChimePattern(audioContext);
          break;
        case 'bell':
          playBellPattern(audioContext);
          break;
        case 'melody':
          playMelodyPattern(audioContext);
          break;
        default:
          playDingPattern(audioContext);
      }

      // Close context after sound finishes
      setTimeout(
        () => {
          audioContext.close();
        },
        audioSettings.duration * 1000 + 100,
      );
    } catch (error) {
      // Silently handle audio errors
    }
  };

  // Sustained ding pattern (2-3 seconds)
  const playDingPattern = async (audioContext: AudioContext) => {
    const oscillator = audioContext.createOscillator();
    const gainNode = audioContext.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(audioContext.destination);

    // Configuration
    oscillator.frequency.value = audioSettings.frequency;
    oscillator.type = 'sine';

    // Volume envelope: fade in, sustain, fade out
    const now = audioContext.currentTime;
    const fadeIn = 0.1;
    const fadeOut = 0.3;
    const sustainTime = audioSettings.duration - fadeIn - fadeOut;

    gainNode.gain.value = 0;
    gainNode.gain.linearRampToValueAtTime(audioSettings.volume, now + fadeIn);
    gainNode.gain.setValueAtTime(audioSettings.volume, now + fadeIn + sustainTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, now + audioSettings.duration);

    oscillator.start(now);
    oscillator.stop(now + audioSettings.duration);
  };

  // Gentle chime pattern (multiple tones)
  const playChimePattern = async (audioContext: AudioContext) => {
    const frequencies = [audioSettings.frequency, audioSettings.frequency * 1.25, audioSettings.frequency * 1.5];
    const now = audioContext.currentTime;

    frequencies.forEach((freq, index) => {
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      oscillator.frequency.value = freq;
      oscillator.type = 'sine';

      const startTime = now + index * 0.3;
      const duration = audioSettings.duration / 2;

      gainNode.gain.value = 0;
      gainNode.gain.linearRampToValueAtTime(audioSettings.volume * 0.7, startTime + 0.1);
      gainNode.gain.exponentialRampToValueAtTime(0.001, startTime + duration);

      oscillator.start(startTime);
      oscillator.stop(startTime + duration);
    });
  };

  // Bell-like pattern with harmonics
  const playBellPattern = async (audioContext: AudioContext) => {
    const fundamentalFreq = audioSettings.frequency;
    const harmonics = [1, 2, 3, 4];
    const now = audioContext.currentTime;

    harmonics.forEach((harmonic, index) => {
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      oscillator.frequency.value = fundamentalFreq * harmonic;
      oscillator.type = 'sine';

      const volume = audioSettings.volume / (harmonic * 2); // Decreasing volume for higher harmonics

      gainNode.gain.value = 0;
      gainNode.gain.linearRampToValueAtTime(volume, now + 0.05);
      gainNode.gain.exponentialRampToValueAtTime(0.001, now + audioSettings.duration);

      oscillator.start(now);
      oscillator.stop(now + audioSettings.duration);
    });
  };

  // Musical melody pattern
  const playMelodyPattern = async (audioContext: AudioContext) => {
    // Simple ascending melody: C, E, G, C (octave)
    const baseFreq = audioSettings.frequency;
    const notes = [
      baseFreq, // C
      baseFreq * 1.25, // E
      baseFreq * 1.5, // G
      baseFreq * 2, // C (octave)
    ];

    const noteLength = audioSettings.duration / notes.length;
    const now = audioContext.currentTime;

    notes.forEach((freq, index) => {
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      oscillator.frequency.value = freq;
      oscillator.type = 'triangle'; // Warmer sound for melody

      const startTime = now + index * noteLength;
      const fadeTime = noteLength * 0.1;

      gainNode.gain.value = 0;
      gainNode.gain.linearRampToValueAtTime(audioSettings.volume, startTime + fadeTime);
      gainNode.gain.linearRampToValueAtTime(0, startTime + noteLength - fadeTime);

      oscillator.start(startTime);
      oscillator.stop(startTime + noteLength);
    });
  };

  // Function to dismiss banner
  const dismissBanner = () => {
    setBannerVisible(false);
    setNewEventCount(0);
  };

  // Function to check if a row should be highlighted (received within last 10 minutes)
  const isRowHighlighted = (signal: SignalActionWithReceived): boolean => {
    const receivedAt = receivedTimestamps.get(signal.id);
    if (!receivedAt) return false;
    const tenMinutesAgo = Date.now() - 10 * 60 * 1000; // 10 minutes in milliseconds
    return receivedAt > tenMinutesAgo;
  };

  // 🗣️ UPDATED: Check for new signals and show notifications + voice announcements
  const checkForNewSignals = (newData: SignalActionWithReceived[], oldData: SignalActionWithReceived[]) => {
    if (oldData.length === 0) return; // Skip on initial load

    const newSignals = newData.filter(newSignal => !oldData.some(oldSignal => oldSignal.id === newSignal.id));

    if (newSignals.length > 0) {
      console.log(`🆕 ${newSignals.length} new signal(s) detected!`);

      // Play notification sound for new signals
      playNotificationSound();

      // Mark new signals with receivedAt timestamp
      const now = Date.now();
      const newTimestamps = new Map(receivedTimestamps);

      newSignals.forEach(signal => {
        newTimestamps.set(signal.id, now);

        // 🗣️ VOICE ANNOUNCEMENT - Only if isAnnounce is true
        if (voiceRef?.current) {
          const voiceSignalData = convertSignalForVoice(signal);

          if (signal.announce === true) {
            console.log(`🗣️ Signal ${signal.id} (${signal.symbol}) - announce=true, announcing`);
            voiceRef.current.announceSignal(voiceSignalData);
          } else {
            console.log(`🔇 Signal ${signal.id} (${signal.symbol}) - announce=${signal.announce}, skipping voice`);
          }
        }
      });

      setReceivedTimestamps(newTimestamps);

      // Update banner
      setNewEventCount(prev => prev + newSignals.length);
      setBannerVisible(true);

      // Show individual notifications
      newSignals.forEach(signal => {
        const directionColor = signal.direction === 'BUY' ? '🟢' : signal.direction === 'SELL' ? '🔴' : '🟡';
        const voiceIndicator = signal.announce ? '🗣️' : '🔇';
        const message = `${directionColor}${voiceIndicator} New ${signal.direction} signal: ${signal.symbol} (${signal.indicatorName}) - ${signal.price}`;
        addNotification(message, 'info');

        // Browser notification if permission granted
        if (Notification.permission === 'granted') {
          new Notification('New Trading Signal', {
            body: `${signal.direction} signal for ${signal.symbol} at ${signal.price}`,
            icon: '/favicon.ico',
          });
        }
      });
    }
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
        const result: SignalActionWithReceived[] = await response.json();

        // Sort by datetime (latest first) by default
        const sortedData = result.sort(
          (a: SignalActionWithReceived, b: SignalActionWithReceived) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime(),
        );

        setData(sortedData);
        setFilteredData(sortedData);

        // Extract unique values for filters
        setUniqueSymbols([...new Set(result.map((item: SignalActionWithReceived) => item.symbol))]);
        setUniqueIntervals([...new Set(result.map((item: SignalActionWithReceived) => item.interval))]);
        setUniqueIndicators([...new Set(result.map((item: SignalActionWithReceived) => item.indicatorName))]);

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
      const sortedEvent: SignalActionWithReceived[] = (event as SignalActionWithReceived[]).sort(
        (a: SignalActionWithReceived, b: SignalActionWithReceived) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime(),
      );

      // Use callback to get current data for comparison
      setData(currentData => {
        // Check for new signals using current data
        checkForNewSignals(sortedEvent, currentData);
        return sortedEvent;
      });

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

  // Refresh highlighting every minute to update the 10-minute window
  useEffect(() => {
    const interval = setInterval(() => {
      // Clean up old timestamps (beyond 10 minutes)
      const tenMinutesAgo = Date.now() - 10 * 60 * 1000;
      setReceivedTimestamps(current => {
        const cleaned = new Map();
        current.forEach((timestamp, id) => {
          if (timestamp > tenMinutesAgo) {
            cleaned.set(id, timestamp);
          }
        });
        return cleaned;
      });

      // Force re-render to update highlighting
      setUpdated(true);
      setTimeout(() => setUpdated(false), 100);
    }, 60000); // Every minute

    return () => clearInterval(interval);
  }, []);

  const applyFilters = (dataToFilter: SignalActionWithReceived[], currentFilters: FilterState) => {
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

  const handleExecute = (signalId: number) => {
    const executeSignal = async () => {
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

          // 🗣️ Voice announcement for execution
          voiceRef?.current?.announceCustomMessage(`Signal executed for ${signal?.symbol}`);
        } else {
          addNotification('❌ Failed to execute signal', 'error');
        }
      } catch (error) {
        addNotification('❌ Error executing signal', 'error');
      }
    };

    executeSignal().catch(console.error);
  };

  const handleCancel = (signalId: number) => {
    const cancelSignal = async () => {
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

          // 🗣️ Voice announcement for cancellation
          voiceRef?.current?.announceCustomMessage(`Signal cancelled for ${signal?.symbol}`);
        } else {
          addNotification('❌ Failed to cancel signal', 'error');
        }
      } catch (error) {
        addNotification('❌ Error cancelling signal', 'error');
      }
    };

    cancelSignal().catch(console.error);
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

      {/* Banner Notification */}
      {bannerVisible && (
        <div className="banner-notification">
          <div className="banner-content">
            <div className="banner-icon">🚨</div>
            <div className="banner-text">
              <strong>New Signals Received!</strong>
              <span>
                {newEventCount} new trading signal{newEventCount !== 1 ? 's' : ''} received
              </span>
            </div>
            <button className="banner-close" onClick={dismissBanner}>
              ×
            </button>
          </div>
        </div>
      )}

      {/* Audio Customization Panel */}
      <div
        className="audio-customization-panel"
        style={{
          padding: '15px',
          background: '#f8f9fa',
          border: '1px solid #dee2e6',
          marginBottom: '10px',
          borderRadius: '5px',
        }}
      >
        <strong>🎵 Audio Notification Settings</strong>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginTop: '10px' }}>
          {/* Duration Setting */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>
              Duration: {audioSettings.duration}s
            </label>
            <input
              type="range"
              min="0.5"
              max="5"
              step="0.1"
              value={audioSettings.duration}
              onChange={e => setAudioSettings(prev => ({ ...prev, duration: parseFloat(e.target.value) }))}
              style={{ width: '100%' }}
            />
          </div>

          {/* Frequency Setting */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>
              Frequency: {audioSettings.frequency}Hz
            </label>
            <input
              type="range"
              min="200"
              max="2000"
              step="50"
              value={audioSettings.frequency}
              onChange={e => setAudioSettings(prev => ({ ...prev, frequency: parseInt(e.target.value, 10) }))}
              style={{ width: '100%' }}
            />
          </div>

          {/* Volume Setting */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>
              Volume: {Math.round(audioSettings.volume * 100)}%
            </label>
            <input
              type="range"
              min="0.05"
              max="0.5"
              step="0.01"
              value={audioSettings.volume}
              onChange={e => setAudioSettings(prev => ({ ...prev, volume: parseFloat(e.target.value) }))}
              style={{ width: '100%' }}
            />
          </div>

          {/* Pattern Setting */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>Sound Pattern</label>
            <select
              value={audioSettings.pattern}
              onChange={e => setAudioSettings(prev => ({ ...prev, pattern: e.target.value }))}
              style={{ width: '100%', padding: '5px' }}
            >
              <option value="ding">🔔 Sustained Ding</option>
              <option value="chime">🎐 Gentle Chime</option>
              <option value="bell">🔔 Bell Harmonics</option>
              <option value="melody">🎵 Musical Melody</option>
            </select>
          </div>
        </div>

        {/* Test and Preset Buttons */}
        <div style={{ marginTop: '15px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          <button
            onClick={() => playNotificationSound()}
            style={{
              padding: '8px 16px',
              background: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            🎵 Test Current Settings
          </button>

          {/* Preset Buttons */}
          <button
            onClick={() => setAudioSettings({ duration: 2.0, frequency: 800, volume: 0.15, pattern: 'ding' })}
            style={{
              padding: '6px 12px',
              background: '#17a2b8',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
          >
            Gentle (2s)
          </button>

          <button
            onClick={() => setAudioSettings({ duration: 3.0, frequency: 600, volume: 0.2, pattern: 'chime' })}
            style={{
              padding: '6px 12px',
              background: '#6f42c1',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
          >
            Soothing (3s)
          </button>

          <button
            onClick={() => setAudioSettings({ duration: 2.5, frequency: 1000, volume: 0.25, pattern: 'bell' })}
            style={{
              padding: '6px 12px',
              background: '#fd7e14',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
          >
            Alert (2.5s)
          </button>

          <button
            onClick={() => setAudioSettings({ duration: 3.5, frequency: 523, volume: 0.18, pattern: 'melody' })}
            style={{
              padding: '6px 12px',
              background: '#e83e8c',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
          >
            Musical (3.5s)
          </button>
        </div>

        <small style={{ display: 'block', marginTop: '10px', color: '#6c757d', fontSize: '11px' }}>
          💡 Tip: Choose longer durations (2-3s) for sustained notifications. Frequency: Lower = deeper, Higher = brighter.
        </small>
      </div>

      <div className="table-header">
        <h3>Signal Actions</h3>
        <div className="header-actions">
          {/* Audio Status and Test Button */}
          <div className="audio-controls">
            <span className={`audio-status ${audioEnabled ? 'audio-enabled' : 'audio-disabled'}`}>{audioEnabled ? '🔊' : '🔇'}</span>
            <button
              onClick={() => {
                if (!audioEnabled) {
                  initializeAudio();
                } else {
                  playNotificationSound().catch(console.error);
                }
              }}
              className={`btn ${audioEnabled ? 'btn-test-audio' : 'btn-enable-audio'}`}
              title={audioEnabled ? 'Test notification sound' : 'Click to enable sound notifications (CSP-safe Web Audio API)'}
            >
              {audioEnabled ? 'Test Sound' : 'Enable Sound'}
            </button>

            {/* 🗣️ Voice Test Button */}
            <button
              onClick={() => voiceRef?.current?.testVoice()}
              className="btn btn-test-voice"
              title="Test voice announcement"
              style={{
                padding: '6px 12px',
                background: '#6f42c1',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '12px',
                marginLeft: '5px',
              }}
            >
              🎤 Test Voice
            </button>

            {!audioEnabled && <span className="audio-hint">👈 Click to hear notifications!</span>}
            {audioEnabled && <span className="audio-enabled-text">✅ Audio ON</span>}
          </div>

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
                    <th>Annotation</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredData.length === 0 ? (
                    <tr>
                      <td colSpan={10} className="no-data">
                        No signal actions available
                      </td>
                    </tr>
                  ) : (
                    filteredData.map(signal => (
                      <tr key={signal.id} className={isRowHighlighted(signal) ? 'row-highlighted' : ''}>
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
                        <td className="annotation">
                          {isRowHighlighted(signal) && <span className="new-signal-badge">🆕</span>}
                          {signal.announce === true && (
                            <span className="voice-enabled-badge" title="Voice announcement enabled">
                              🗣️
                            </span>
                          )}
                          {signal.announce === false && (
                            <span className="voice-disabled-badge" title="Voice announcement disabled">
                              🔇
                            </span>
                          )}
                          {signal.announce === undefined && (
                            <span className="voice-unknown-badge" title="Voice setting not configured">
                              ❓
                            </span>
                          )}
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
