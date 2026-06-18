import React, { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import TikTokAccounts from '../TikTokAccounts/TikTokAccounts';
import Reports from '../Reports/Reports';
import TopUnfollowers from '../TopUnfollowers/TopUnfollowers';
import './Dashboard.css';

function Dashboard() {
  const { user, logout } = useAuth();
  const [activeTab, setActiveTab] = useState('accounts');
  const [selectedAccount, setSelectedAccount] = useState(null);

  const handleSelectAccount = ({ id, username }) => {
    setSelectedAccount({ id, username });
    setActiveTab('reports');
  };

  const initials = user?.name
    ? user.name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
    : '?';

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-content">
          <div className="header-logo">
            <span className="header-logo-icon">📱</span>
            <h1>Follower Tracker</h1>
          </div>
          <div className="header-right">
            <div className="user-avatar">{initials}</div>
            <span className="user-name">{user?.name}</span>
            <button onClick={logout} className="logout-btn">Sign out</button>
          </div>
        </div>
      </header>

      <nav className="dashboard-nav">
        <div className="dashboard-nav-inner">
          <button
            className={`nav-btn ${activeTab === 'accounts' ? 'active' : ''}`}
            onClick={() => setActiveTab('accounts')}
          >
            <span className="nav-icon">👤</span>
            Accounts
          </button>
          <button
            className={`nav-btn ${activeTab === 'reports' ? 'active' : ''}`}
            onClick={() => setActiveTab('reports')}
            disabled={!selectedAccount}
          >
            <span className="nav-icon">📊</span>
            Reports
            {selectedAccount && (
              <span className="nav-account-badge">@{selectedAccount.username}</span>
            )}
          </button>
          <button
            className={`nav-btn ${activeTab === 'unfollowers' ? 'active' : ''}`}
            onClick={() => setActiveTab('unfollowers')}
          >
            <span className="nav-icon">🚨</span>
            Top Unfollowers
          </button>
        </div>
      </nav>

      <main className="dashboard-content">
        {activeTab === 'accounts' && (
          <TikTokAccounts onSelectAccount={handleSelectAccount} />
        )}
        {activeTab === 'reports' && selectedAccount && (
          <Reports accountId={selectedAccount.id} username={selectedAccount.username} />
        )}
        {activeTab === 'unfollowers' && <TopUnfollowers />}
      </main>
    </div>
  );
}

export default Dashboard;
