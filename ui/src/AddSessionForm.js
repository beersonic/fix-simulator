import React from 'react';

export default function AddSessionForm({ form, setForm, onSubmit, onCancel }) {
  const handleSubmit = e => {
    console.log('AddSessionForm submit event fired');
    onSubmit(e);
  };
  return (
    <form
      style={{
        background: '#fff',
        border: '1px solid #1976d2',
        borderRadius: '8px',
        padding: '1rem',
        marginBottom: '1rem',
      }}
      onSubmit={handleSubmit}
    >
      <h4>Add FIX Session</h4>
      <div style={{ marginBottom: '0.5rem' }}>
        <label>Type: </label>
        <select value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value }))}>
          <option value="initiator">Initiator</option>
          <option value="acceptor">Acceptor</option>
        </select>
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <label>SenderCompID: </label>
        <input value={form.senderCompID} onChange={e => setForm(f => ({ ...f, senderCompID: e.target.value }))} required />
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <label>TargetCompID: </label>
        <input value={form.targetCompID} onChange={e => setForm(f => ({ ...f, targetCompID: e.target.value }))} required />
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <label>Host: </label>
        <input value={form.host} onChange={e => setForm(f => ({ ...f, host: e.target.value }))} required />
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <label>Port: </label>
        <input value={form.port} onChange={e => setForm(f => ({ ...f, port: e.target.value }))} required />
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <label>HeartBtInt: </label>
        <input value={form.heartBtInt} onChange={e => setForm(f => ({ ...f, heartBtInt: e.target.value }))} required />
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <label>DefaultApplVerID: </label>
        <input value={form.defaultApplVerID ?? 'FIX.5.0SP2'} onChange={e => setForm(f => ({ ...f, defaultApplVerID: e.target.value }))} required />
      </div>
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <button type="submit" style={{ background: '#1976d2', color: 'white', border: 'none', borderRadius: '4px', padding: '0.3rem 1rem', fontWeight: 'bold' }}>Add</button>
        <button type="button" style={{ background: '#eee', border: 'none', borderRadius: '4px', padding: '0.3rem 1rem' }} onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}
