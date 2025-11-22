import React from 'react';

export default function SessionDetails({ session }) {
  if (!session) {
    return <p>Select a session on the left to view details.</p>;
  }
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', maxWidth: '600px' }}>
      <div style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '1rem', background: '#fafafa' }}>
        <h3>{session.sessionName}</h3>
        <p><strong>Status:</strong> <span style={{color: session.loggedOn ? 'green' : 'gray'}}>{session.loggedOn ? 'Online' : 'Offline'}</span></p>
        <p><strong>ID:</strong> {session.id}</p>
        <p><strong>Started At:</strong> {session.startedAt}</p>
        <p><strong>Message Count:</strong> {session.messageCount}</p>
      </div>
      <div style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '1rem', background: '#fff', minHeight: '120px' }}>
        <h3>FIX Messages</h3>
        <p>Messages for this session will appear here.</p>
      </div>
    </div>
  );
}
