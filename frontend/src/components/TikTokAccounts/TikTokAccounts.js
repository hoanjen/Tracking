import React, { useState, useEffect } from 'react';
import { tiktokService, trackingService } from '../../services/api';
import './TikTokAccounts.css';

function TikTokAccounts({ onSelectAccount }) {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newUsername, setNewUsername] = useState('');
  const [trackingId, setTrackingId] = useState(null);

  useEffect(() => { fetchAccounts(); }, []);

  const fetchAccounts = async () => {
    try {
      const res = await tiktokService.getUserAccounts();
      setAccounts(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleAddAccount = async (e) => {
    e.preventDefault();
    if (!newUsername.trim()) return;
    try {
      await tiktokService.createAccount(newUsername.trim());
      setNewUsername('');
      fetchAccounts();
    } catch {
      alert('Failed to add account');
    }
  };

  const handleCreateTracking = async (accountId) => {
    setTrackingId(accountId);
    try {
      const acct = accounts.find(a => a.id === accountId);
      await trackingService.createTracking(accountId);
      await fetchAccounts();
      onSelectAccount({ id: accountId, username: acct?.username || '' });
    } catch (e) {
      alert(e.response?.data?.error || 'Failed to create tracking.');
    } finally {
      setTrackingId(null);
    }
  };

  const handleDelete = async (accountId) => {
    if (!window.confirm('Delete this account and all its data?')) return;
    try {
      await tiktokService.deleteAccount(accountId);
      fetchAccounts();
    } catch {
      alert('Failed to delete account');
    }
  };

  if (loading) return <div className="loading">Loading accounts</div>;

  return (
    <div className="tiktok-accounts">
      {/* Add account */}
      <div className="section-card">
        <p className="section-title">Add TikTok Account</p>
        <form className="add-account-form" onSubmit={handleAddAccount}>
          <input
            type="text"
            placeholder="Enter TikTok username…"
            value={newUsername}
            onChange={e => setNewUsername(e.target.value)}
            maxLength={100}
          />
          <button type="submit" className="btn-primary" style={{ whiteSpace: 'nowrap', width: 'auto' }}>
            + Add
          </button>
        </form>
      </div>

      {/* Account list */}
      <div className="section-card">
        <p className="section-title">Your Accounts — {accounts.length}</p>

        {accounts.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">📭</span>
            No accounts yet. Add your first TikTok account above.
          </div>
        ) : (
          <>
            <div className="accounts-grid">
              {accounts.map(account => {
                const isFetching = trackingId === account.id;
                return (
                  <div key={account.id} className="account-card">
                    <div className="account-avatar">🎵</div>
                    <div className="account-info">
                      <div className="account-username">@{account.username}</div>
                      <div className="account-meta">{account.trackingCount} snapshot{account.trackingCount !== 1 ? 's' : ''}</div>
                    </div>
                    <div className="account-actions">
                      {isFetching ? (
                        <div className="fetching-indicator">
                          <div className="fetching-dot" />
                          <div className="fetching-dot" />
                          <div className="fetching-dot" />
                        </div>
                      ) : (
                        <>
                          <button
                            className="btn-track"
                            onClick={() => handleCreateTracking(account.id)}
                            disabled={trackingId !== null}
                            title="Fetch current followers and create snapshot"
                          >
                            📸 Track
                          </button>
                          <button
                            className="btn-reports"
                            onClick={() => onSelectAccount({ id: account.id, username: account.username })}
                            disabled={trackingId !== null}
                          >
                            Reports
                          </button>
                          <button
                            className="btn-delete"
                            onClick={() => handleDelete(account.id)}
                            disabled={trackingId !== null}
                          >
                            Delete
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
            <p className="limit-hint">Up to 5 snapshots per account per day</p>
          </>
        )}
      </div>
    </div>
  );
}

export default TikTokAccounts;
