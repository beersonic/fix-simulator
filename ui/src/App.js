import React, { useState } from 'react';

function App() {
  const [selected, setSelected] = useState('Sessions');
  const [selectedSessionId, setSelectedSessionId] = useState(null);

  // Fake sessions data
  const sessions = [
    { id: 1, name: 'Session A', status: 'Active' },
    { id: 2, name: 'Session B', status: 'Stopped' },
    { id: 3, name: 'Session C', status: 'Active' }
  ];

  let mainContent;
  mainContent = (
    <>
      <h2>Sessions</h2>
      {selectedSessionId ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', maxWidth: '600px' }}>
          <div style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '1rem', background: '#fafafa' }}>
            <h3>{sessions.find(s => s.id === selectedSessionId).name}</h3>
            <p>Status: <span style={{color: sessions.find(s => s.id === selectedSessionId).status === 'Active' ? 'green' : 'gray'}}>{sessions.find(s => s.id === selectedSessionId).status}</span></p>
            <p>Session details will appear here.</p>
          </div>
          <div style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '1rem', background: '#fff', minHeight: '120px' }}>
            <h3>FIX Messages</h3>
            <p>Messages for this session will appear here.</p>
          </div>
        </div>
      ) : (
        <p>Select a session on the left to view details.</p>
      )}
    </>
  );

  return (
    <div className="App" style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      <header style={{ background: '#282c34', color: 'white', padding: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <button style={{ padding: '0.5rem 1rem', fontWeight: 'bold' }} onClick={() => setSelected('Sessions')}>Sessions</button>
        </div>
        <h1 style={{ margin: 0, flex: 1, textAlign: 'center' }}>FIX Simulator</h1>
      </header>
      <div style={{ display: 'flex', flex: 1 }}>
        {/* Sidebar for sessions only when Sessions tab is selected */}
        {selected === 'Sessions' && (
          <nav style={{ width: '240px', background: '#f0f0f0', padding: '1rem', borderRight: '1px solid #ddd' }}>
            <h3>Sessions</h3>
            <div>
              {sessions.map(session => (
                <div
                  key={session.id}
                  onClick={() => setSelectedSessionId(session.id)}
                  style={{
                    border: selectedSessionId === session.id ? '2px solid #1976d2' : '1px solid #ddd',
                    borderRadius: '8px',
                    padding: '0.75rem',
                    marginBottom: '0.75rem',
                    background: selectedSessionId === session.id ? '#e3f2fd' : '#fff',
                    cursor: 'pointer',
                    boxShadow: selectedSessionId === session.id ? '0 2px 8px rgba(25,118,210,0.08)' : 'none',
                    transition: 'all 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                  }}
                >
                  <div style={{ flex: 1 }}>
                    <strong>{session.name}</strong>
                    <div style={{ fontSize: '0.9em', color: '#666' }}>ID: {session.id}</div>
                  </div>
                  <span style={{color: session.status === 'Active' ? 'green' : 'gray', fontWeight: 'bold'}}>{session.status}</span>
                </div>
              ))}
            </div>
          </nav>
        )}
        <main style={{ flex: 1, padding: '2rem' }}>
          {mainContent}
        </main>
      </div>
    </div>
  );
}

export default App;
