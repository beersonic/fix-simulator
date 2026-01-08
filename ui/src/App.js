import React, { useState, useEffect } from 'react';
import axios from 'axios';
import SessionSidebar from './SessionSidebar';
import SessionDetails from './SessionDetails';

function BackendIndicator({ status }) {
  const s = (status || '').toString().toLowerCase();
  let color = '#888';
  let label = 'Service: Unknown';
  if (s.includes('ready') || s.includes('ok') || s.includes('listening') || s.includes('200')) {
    color = '#4caf50';
    label = 'Service: OK';
  } else if (s.includes('start') || s.includes('loading') || s.includes('waiting') || s.includes('starting')) {
    color = '#ff9800';
    label = 'Service: Starting';
  } else if (s.includes('timeout') || s.includes('error') || s.includes('failed') || s.includes('not')) {
    color = '#f44336';
    label = 'Service: Unavailable';
  } else if (s && s !== 'unknown') {
    label = `Service: ${status}`;
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <span style={{ width: 12, height: 12, borderRadius: 6, background: color, display: 'inline-block' }} />
      <span style={{ color: 'white', fontSize: 12 }}>{label}</span>
    </div>
  );
}

function App() {
  const [backendStatus, setBackendStatus] = useState('unknown');
  const [selected, setSelected] = useState('Sessions');
  const [selectedSessionId, setSelectedSessionId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [form, setForm] = useState({
    type: 'initiator',
    senderCompID: '',
    targetCompID: 'BeerFIXServer',
    host: '127.0.0.1',
    port: '9878',
    heartBtInt: '30',
    defaultApplVerID: 'FIX.5.0SP2'
  });

  // Sessions data from backend
  const [sessions, setSessions] = useState([]);
  const [sessionsFetchOk, setSessionsFetchOk] = useState(false);
  useEffect(() => {
    let mounted = true;
    const fetchSessions = () => {
      axios.get('/fix/sessions')
        .then(res => { if (mounted) { setSessions(res.data); setSessionsFetchOk(true); } })
        .catch(() => { if (mounted) { setSessions([]); setSessionsFetchOk(false); } });
    };
    fetchSessions();
    const interval = setInterval(fetchSessions, 3000);
    return () => { mounted = false; clearInterval(interval); };
  }, []);

  // Track whether sessions fetch succeeded so we can show service health
  useEffect(() => {
    let mounted = true;
    const fetchOnce = async () => {
      try {
        const res = await axios.get('/fix/sessions');
        if (mounted) {
          setSessions(res.data);
          setSessionsFetchOk(true);
        }
      } catch (e) {
        if (mounted) setSessionsFetchOk(false);
      }
    };
    fetchOnce();
    return () => { mounted = false; };
  }, []);

  // Listen for backend status sent from Electron main (polling stage)
  useEffect(() => {
    if (window && window.electronAPI && typeof window.electronAPI.onBackendStatus === 'function') {
      try {
        window.electronAPI.onBackendStatus((msg) => {
          if (!msg) return;
          setBackendStatus(String(msg));
        });
      } catch (e) {
        // ignore if bridge not available
      }
    }
  }, []);

  // Prefer sessions-based health when available; fall back to backendStatus from Electron
  const effectiveStatus = sessionsFetchOk ? 'ok' : backendStatus;

  // Handler for adding a session via REST API
  const [addError, setAddError] = useState(null);
  const handleAddSession = async e => {
    e.preventDefault();
    setAddError(null);
    try {
      await axios.post('/fix/session', form);
      // Refetch sessions after adding
      const res = await axios.get('/fix/sessions');
      setSessions(res.data);
      setShowAddForm(false);
      setForm({ type: 'initiator', senderCompID: '', targetCompID: 'BeerFIXServer', host: '127.0.0.1', port: '9878', heartBtInt: '30', defaultApplVerID: 'FIX.5.0SP2' });
    } catch (err) {
      setAddError(err.response?.data?.message || err.message || 'Failed to add session');
    }
  };

  return (
    <div className="App" style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      <header style={{ background: '#282c34', color: 'white', padding: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <button style={{ padding: '0.5rem 1rem', fontWeight: 'bold' }} onClick={() => setSelected('Sessions')}>Sessions</button>
        </div>
        <h1 style={{ margin: 0, flex: 1, textAlign: 'center' }}>FIX Simulator</h1>
        <div style={{ marginLeft: '1rem', display: 'flex', alignItems: 'center' }}>
          <BackendIndicator status={effectiveStatus} />
        </div>
      </header>
      <div style={{ display: 'flex', flex: 1 }}>
        {selected === 'Sessions' && (
          <SessionSidebar
            sessions={sessions}
            selectedSessionId={selectedSessionId}
            setSelectedSessionId={setSelectedSessionId}
            showAddForm={showAddForm}
            setShowAddForm={setShowAddForm}
            form={form}
            setForm={setForm}
            handleAddSession={handleAddSession}
            addError={addError}
          />
        )}
        <main style={{ flex: 1, padding: '2rem' }}>
          <h2>Sessions</h2>
          <SessionDetails session={sessions.find(s => s.id === selectedSessionId)} />
        </main>
      </div>
    </div>
  );
}

export default App;
