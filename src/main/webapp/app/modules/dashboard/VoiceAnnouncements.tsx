import React, { useState, useEffect, useCallback } from 'react';

interface VoiceSettings {
  enabled: boolean;
  voice: string;
  rate: number; // 0.5 - 2.0
  pitch: number; // 0 - 2
  volume: number; // 0 - 1
  customTemplate: string;
}

interface SignalData {
  id: number;
  symbol: string;
  direction: 'BUY' | 'SELL' | 'HOLD';
  indicatorName: string;
  price: number;
  interval: string;
  message?: string;
  announce?: boolean; // 🗣️ Optional: Only announce when true
}

interface VoiceAnnouncementsProps {
  onNotification?: (message: string, type: 'success' | 'info' | 'warning' | 'error') => void;
  className?: string;
  style?: React.CSSProperties;
}

interface VoiceAnnouncementsRef {
  announceSignal: (signal: SignalData) => void;
  announceCustomMessage: (message: string) => void;
  testVoice: () => void;
  isEnabled: () => boolean;
}

const VoiceAnnouncements = React.forwardRef<VoiceAnnouncementsRef, VoiceAnnouncementsProps>(
  ({ onNotification, className = '', style = {} }, ref) => {
    // Voice settings state
    const [voiceSettings, setVoiceSettings] = useState<VoiceSettings>({
      enabled: true,
      voice: '',
      rate: 1.0,
      pitch: 1.0,
      volume: 0.8,
      customTemplate: 'New {direction} signal for {symbol} using {indicator}',
    });

    const [availableVoices, setAvailableVoices] = useState<SpeechSynthesisVoice[]>([]);
    const [voiceEnabled, setVoiceEnabled] = useState(false);
    const [isInitialized, setIsInitialized] = useState(false);

    // Voice announcement function
    const speakAnnouncement = useCallback(
      (text: string) => {
        if (!voiceSettings.enabled || !voiceEnabled) {
          console.log('🔇 Voice announcements disabled');
          return;
        }

        try {
          // Cancel any ongoing speech
          window.speechSynthesis.cancel();

          const utterance = new SpeechSynthesisUtterance(text);

          // Apply voice settings
          if (voiceSettings.voice) {
            const selectedVoice = availableVoices.find(voice => voice.name === voiceSettings.voice);
            if (selectedVoice) {
              utterance.voice = selectedVoice;
            }
          }

          utterance.rate = voiceSettings.rate;
          utterance.pitch = voiceSettings.pitch;
          utterance.volume = voiceSettings.volume;

          // Event listeners for debugging
          utterance.onstart = () => console.log('🗣️ Voice announcement started:', text);
          utterance.onend = () => console.log('✅ Voice announcement completed');
          utterance.onerror = event => console.error('❌ Voice announcement error:', event.error);

          window.speechSynthesis.speak(utterance);
        } catch (error) {
          console.error('❌ Speech synthesis error:', error);
          onNotification?.('Voice announcement failed', 'error');
        }
      },
      [voiceSettings, voiceEnabled, availableVoices, onNotification],
    );

    // Generate announcement text from signal data
    const generateAnnouncementText = useCallback(
      (signal: SignalData): string => {
        const template = voiceSettings.customTemplate;

        return template
          .replace('{direction}', signal.direction.toLowerCase())
          .replace('{symbol}', signal.symbol)
          .replace('{indicator}', signal.indicatorName)
          .replace('{price}', `$${signal.price.toFixed(2)}`)
          .replace('{interval}', signal.interval)
          .replace('{message}', signal.message || '');
      },
      [voiceSettings.customTemplate],
    );

    // Initialize voice functionality
    const initializeVoice = useCallback(() => {
      try {
        if (!('speechSynthesis' in window)) {
          throw new Error('Speech Synthesis not supported');
        }

        // Load available voices
        const loadVoices = () => {
          const voices = window.speechSynthesis.getVoices();
          setAvailableVoices(voices);

          // Set default voice (prefer English voices)
          if (voices.length > 0 && !voiceSettings.voice) {
            const englishVoice = voices.find(voice => voice.lang.startsWith('en')) || voices[0];
            setVoiceSettings(prev => ({ ...prev, voice: englishVoice.name }));
          }
        };

        // Load voices immediately
        loadVoices();

        // Also load when voices change (some browsers load asynchronously)
        window.speechSynthesis.onvoiceschanged = loadVoices;

        setVoiceEnabled(true);
        setIsInitialized(true);
        console.log('✅ Voice announcements enabled');

        onNotification?.('🗣️ Voice announcements initialized successfully!', 'success');

        // Test announcement
        setTimeout(() => {
          speakAnnouncement('Voice announcements are now enabled for trading signals');
        }, 500);

        return true;
      } catch (error) {
        console.log('❌ Voice initialization failed:', error);
        onNotification?.('⚠️ Voice announcements not supported in this browser', 'warning');
        return false;
      }
    }, [voiceSettings.voice, onNotification, speakAnnouncement]);

    // Test voice announcement function
    const testVoiceAnnouncement = useCallback(() => {
      const testSignal: SignalData = {
        id: 999,
        symbol: 'BTCUSDT',
        direction: 'BUY',
        indicatorName: 'Luxalgo',
        price: 45250.5,
        interval: '15m',
        message: 'Bullish contrarian pattern detected',
      };

      const announcementText = generateAnnouncementText(testSignal);
      speakAnnouncement(announcementText);
    }, [generateAnnouncementText, speakAnnouncement]);

    // Expose methods via ref
    React.useImperativeHandle(
      ref,
      () => ({
        announceSignal: (signal: SignalData) => {
          // 🗣️ CHECK: Only announce if isAnnounce is explicitly true

          if (signal.announce !== true) {
            console.log(`🔇 Signal ${signal.id} (${signal.symbol}) - announce=${signal.announce}, skipping voice announcement`);
            return;
          }

          if (voiceEnabled && voiceSettings.enabled) {
            // Check for specific patterns like Luxalgo contrarian
            if (signal.indicatorName.toLowerCase().includes('luxalgo') && signal.message?.toLowerCase().includes('contrarian')) {
              const customAnnouncement = `${signal.direction === 'BUY' ? 'Bullish' : 'Bearish'} contrarian in ${signal.indicatorName} signals for ${signal.symbol}`;
              console.log(`🗣️ Announcing Luxalgo contrarian: ${customAnnouncement}`);
              speakAnnouncement(customAnnouncement);
            } else {
              const standardAnnouncement = generateAnnouncementText(signal);
              console.log(`🗣️ Announcing standard signal: ${standardAnnouncement}`);
              speakAnnouncement(standardAnnouncement);
            }
          } else {
            console.log('🔇 Voice disabled, not announcing signal');
          }
        },
        announceCustomMessage: (message: string) => {
          if (voiceEnabled && voiceSettings.enabled) {
            speakAnnouncement(message);
          }
        },
        testVoice: testVoiceAnnouncement,
        isEnabled: () => voiceEnabled && voiceSettings.enabled,
      }),
      [voiceEnabled, voiceSettings.enabled, speakAnnouncement, generateAnnouncementText, testVoiceAnnouncement],
    );

    // Load voices when component mounts
    useEffect(() => {
      const handleVoicesChanged = () => {
        const voices = window.speechSynthesis.getVoices();
        setAvailableVoices(voices);

        if (voices.length > 0 && !voiceSettings.voice) {
          const englishVoice = voices.find(voice => voice.lang.startsWith('en')) || voices[0];
          setVoiceSettings(prev => ({ ...prev, voice: englishVoice.name }));
        }
      };

      if ('speechSynthesis' in window) {
        window.speechSynthesis.addEventListener('voiceschanged', handleVoicesChanged);
        handleVoicesChanged(); // Load immediately if available

        return () => {
          window.speechSynthesis.removeEventListener('voiceschanged', handleVoicesChanged);
        };
      }
    }, [voiceSettings.voice]);

    return (
      <div
        className={`voice-customization-panel ${className}`}
        style={{
          padding: '15px',
          background: '#e8f5e8',
          border: '1px solid #c3e6c3',
          marginBottom: '10px',
          borderRadius: '5px',
          ...style,
        }}
      >
        <strong>🗣️ Voice Announcement Settings</strong>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginTop: '10px' }}>
          {/* Voice Selection */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>Voice Selection</label>
            <select
              value={voiceSettings.voice}
              onChange={e => setVoiceSettings(prev => ({ ...prev, voice: e.target.value }))}
              style={{ width: '100%', padding: '5px' }}
              disabled={!voiceEnabled}
            >
              <option value="">Select English Voice</option>
              {availableVoices
                .filter(voice => voice.lang.startsWith('en'))
                .map(voice => (
                  <option key={voice.name} value={voice.name}>
                    {voice.name}
                  </option>
                ))}
            </select>
          </div>

          {/* Speaking Rate */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>
              Speaking Rate: {voiceSettings.rate.toFixed(1)}x
            </label>
            <input
              type="range"
              min="0.5"
              max="2.0"
              step="0.1"
              value={voiceSettings.rate}
              onChange={e => setVoiceSettings(prev => ({ ...prev, rate: parseFloat(e.target.value) }))}
              style={{ width: '100%' }}
              disabled={!voiceEnabled}
            />
          </div>

          {/* Pitch */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>
              Pitch: {voiceSettings.pitch.toFixed(1)}
            </label>
            <input
              type="range"
              min="0.0"
              max="2.0"
              step="0.1"
              value={voiceSettings.pitch}
              onChange={e => setVoiceSettings(prev => ({ ...prev, pitch: parseFloat(e.target.value) }))}
              style={{ width: '100%' }}
              disabled={!voiceEnabled}
            />
          </div>

          {/* Voice Volume */}
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>
              Voice Volume: {Math.round(voiceSettings.volume * 100)}%
            </label>
            <input
              type="range"
              min="0.1"
              max="1.0"
              step="0.05"
              value={voiceSettings.volume}
              onChange={e => setVoiceSettings(prev => ({ ...prev, volume: parseFloat(e.target.value) }))}
              style={{ width: '100%' }}
              disabled={!voiceEnabled}
            />
          </div>
        </div>

        {/* Custom Announcement Template */}
        <div style={{ marginTop: '15px' }}>
          <label style={{ display: 'block', fontSize: '12px', fontWeight: 'bold', marginBottom: '5px' }}>
            Custom Announcement Template
          </label>
          <input
            type="text"
            value={voiceSettings.customTemplate}
            onChange={e => setVoiceSettings(prev => ({ ...prev, customTemplate: e.target.value }))}
            placeholder="New {direction} signal for {symbol} using {indicator}"
            style={{ width: '100%', padding: '8px', fontSize: '14px' }}
            disabled={!voiceEnabled}
          />
          <small style={{ fontSize: '10px', color: '#666', marginTop: '2px', display: 'block' }}>
            Variables: &#123;direction&#125;, &#123;symbol&#125;, &#123;indicator&#125;, &#123;price&#125;, &#123;interval&#125;,
            &#123;message&#125;
          </small>
        </div>

        {/* Voice Control Buttons */}
        <div style={{ marginTop: '15px', display: 'flex', gap: '10px', flexWrap: 'wrap', alignItems: 'center' }}>
          <button
            onClick={() => {
              if (!voiceEnabled) {
                initializeVoice();
              } else {
                setVoiceEnabled(false);
                window.speechSynthesis.cancel();
                setVoiceSettings(prev => ({ ...prev, enabled: false }));
                onNotification?.('Voice announcements disabled', 'info');
              }
            }}
            style={{
              padding: '8px 16px',
              background: voiceEnabled ? '#dc3545' : '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            {voiceEnabled ? '🔇 Disable Voice' : '🗣️ Enable Voice'}
          </button>

          {voiceEnabled && (
            <>
              <button
                onClick={testVoiceAnnouncement}
                style={{
                  padding: '8px 16px',
                  background: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '14px',
                }}
              >
                🎤 Test Voice
              </button>

              <button
                onClick={() => {
                  window.speechSynthesis.cancel();
                  onNotification?.('Voice stopped', 'info');
                }}
                style={{
                  padding: '6px 12px',
                  background: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '12px',
                }}
              >
                ⏹️ Stop Speaking
              </button>

              {/* Voice Toggle Checkbox */}
              <label style={{ display: 'flex', alignItems: 'center', gap: '5px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={voiceSettings.enabled}
                  onChange={e => setVoiceSettings(prev => ({ ...prev, enabled: e.target.checked }))}
                />
                <span style={{ fontSize: '12px' }}>Enable for new signals</span>
              </label>
            </>
          )}

          {/* Voice Status Indicator */}
          <span
            style={{
              fontSize: '12px',
              padding: '4px 8px',
              borderRadius: '12px',
              background: voiceEnabled && voiceSettings.enabled ? '#d4edda' : '#f8d7da',
              color: voiceEnabled && voiceSettings.enabled ? '#155724' : '#721c24',
            }}
          >
            {voiceEnabled && voiceSettings.enabled ? '✅ Voice ON' : '❌ Voice OFF'}
          </span>
        </div>

        {/* Voice Presets - Clean English-focused options */}
        <div style={{ marginTop: '10px', display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          {/* Auto-Select Best English Voice */}
          <button
            onClick={() => {
              // Find the clearest available English voice
              const topChoices = ['Alex', 'Samantha', 'Google', 'David Desktop', 'Zira Desktop', 'Daniel'];
              let bestVoice = null;

              for (const choice of topChoices) {
                bestVoice = availableVoices.find(v => v.name.includes(choice));
                if (bestVoice) break;
              }

              // Fallback to first English voice
              if (!bestVoice && availableVoices.length > 0) {
                bestVoice = availableVoices[0];
              }

              setVoiceSettings(prev => ({
                ...prev,
                voice: bestVoice?.name || prev.voice,
                rate: 1.0,
                pitch: 1.0,
                volume: 0.8,
                customTemplate: 'New {direction} signal for {symbol} using {indicator}',
              }));

              // Test the selected voice
              setTimeout(() => {
                speakAnnouncement(`Selected ${bestVoice?.name || 'voice'} for trading alerts`);
              }, 300);
            }}
            style={{
              padding: '6px 12px',
              background: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
              fontWeight: 'bold',
            }}
            disabled={!voiceEnabled}
          >
            ⭐ Auto-Select Best Voice
          </button>

          {/* Standard Settings */}
          <button
            onClick={() =>
              setVoiceSettings(prev => ({
                ...prev,
                rate: 1.0,
                pitch: 1.0,
                volume: 0.8,
                customTemplate: 'New {direction} signal for {symbol} using {indicator}',
              }))
            }
            style={{
              padding: '6px 12px',
              background: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
            disabled={!voiceEnabled}
          >
            Standard
          </button>

          {/* Alert Mode - Clearer & Louder */}
          <button
            onClick={() =>
              setVoiceSettings(prev => ({
                ...prev,
                rate: 0.9,
                pitch: 1.1,
                volume: 0.9,
                customTemplate: 'Alert! {direction} signal for {symbol} at {price}',
              }))
            }
            style={{
              padding: '6px 12px',
              background: '#fd7e14',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
            disabled={!voiceEnabled}
          >
            Alert Mode
          </button>

          {/* Luxalgo Contrarian Mode */}
          <button
            onClick={() => {
              setVoiceSettings(prev => ({
                ...prev,
                rate: 0.9,
                pitch: 1.0,
                volume: 0.85,
                customTemplate: '{direction} contrarian signal in {indicator} for {symbol}',
              }));
              // Test with Luxalgo example
              setTimeout(() => {
                speakAnnouncement('Bullish contrarian in Luxalgo signals for BTCUSDT');
              }, 300);
            }}
            style={{
              padding: '6px 12px',
              background: '#6f42c1',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px',
            }}
            disabled={!voiceEnabled}
          >
            Luxalgo Mode
          </button>
        </div>

        <small style={{ display: 'block', marginTop: '10px', color: '#6c757d', fontSize: '11px' }}>
          💡 Tip: Click &quot;⭐ Auto-Select Best Voice&quot; to automatically choose the clearest English voice available. Use
          &quot;Luxalgo Mode&quot; for contrarian signal announcements.
        </small>
      </div>
    );
  },
);

VoiceAnnouncements.displayName = 'VoiceAnnouncements';

export default VoiceAnnouncements;
export type { VoiceAnnouncementsRef, SignalData };
