import React from 'react';
import AddSessionForm from './AddSessionForm';

export default function SessionSidebar({
  sessions,
  selectedSessionId,
  setSelectedSessionId,
  showAddForm,
  setShowAddForm,
  form,
  setForm,
  handleAddSession
}) {
  return (
    <nav style={{ width: '240px', background: '#f0f0f0', padding: '1rem', borderRight: '1px solid #ddd' }}>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
        <h3 style={{ flex: 1, margin: 0 }}>Sessions</h3>
        <button
          style={{ padding: '0.3rem 0.8rem', background: '#1976d2', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
          onClick={() => setShowAddForm(true)}
        >+ Add</button>
      </div>
      <div>
        {showAddForm && (
          <AddSessionForm
            form={form}
            setForm={setForm}
            onSubmit={handleAddSession}
            onCancel={() => setShowAddForm(false)}
          />
        )}
        {sessions.map(session => (
          <div
            key={session.id}
            onClick={() => setSelectedSessionId(session.id)}
            style={{
              border: selectedSessionId === session.id ? '2px solid #1976d2' : '1px solid #ddd',
              borderRadius: '6px',
              padding: '0.4rem 0.5rem',
              marginBottom: '0.4rem',
              background: selectedSessionId === session.id ? '#e3f2fd' : '#fff',
              cursor: 'pointer',
              boxShadow: selectedSessionId === session.id ? '0 1px 4px rgba(25,118,210,0.08)' : 'none',
              transition: 'all 0.2s',
              display: 'flex',
              alignItems: 'center',
              minHeight: '38px',
              fontSize: '0.95em',
            }}
          >
            <div style={{ flex: 1 }}>
              <strong style={{ fontSize: '1em' }}>{session.sessionName}</strong>
              <div style={{ fontSize: '0.8em', color: '#666' }}>ID: {session.id}</div>
            </div>
            <span style={{color: session.loggedOn ? 'green' : 'gray', fontWeight: 'bold', fontSize: '0.9em'}}>{session.loggedOn ? 'Online' : 'Offline'}</span>
          </div>
        ))}
      </div>
    </nav>
  );
}
