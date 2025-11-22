import React, { useState, useEffect } from 'react';
import axios from 'axios';
import SessionSidebar from './SessionSidebar';
import SessionDetails from './SessionDetails';

function App() {
  const [selected, setSelected] = useState('Sessions');
  const [selectedSessionId, setSelectedSessionId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [form, setForm] = useState({
    type: 'initiator',
    senderCompID: '',
    targetCompID: 'BeerFIXServer',
    host: '127.0.0.1',
    port: '9878',
    heartBtInt: '30'
  });

  // Sessions data from backend
  const [sessions, setSessions] = useState([]);
  useEffect(() => {
    let mounted = true;
    const fetchSessions = () => {
      axios.get('/fix/sessions')
        .then(res => { if (mounted) setSessions(res.data); })
        .catch(() => { if (mounted) setSessions([]); });
    };
    fetchSessions();
    const interval = setInterval(fetchSessions, 3000);
    return () => { mounted = false; clearInterval(interval); };
  }, []);

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
      setForm({ type: 'initiator', senderCompID: '', targetCompID: 'BeerFIXServer', host: '127.0.0.1', port: '9878', heartBtInt: '30' });
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
