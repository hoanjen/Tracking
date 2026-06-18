import React, { useState, useEffect } from 'react';
import { topUnfollowerService } from '../../services/api';
import './TopUnfollowers.css';

function TopUnfollowers() {
  const [unfollowers, setUnfollowers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTopUnfollowers();
  }, []);

  const fetchTopUnfollowers = async () => {
    setLoading(true);
    try {
      const response = await topUnfollowerService.getTopUnfollowers();
      setUnfollowers(response.data);
    } catch (error) {
      console.error('Failed to fetch top unfollowers:', error);
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (username) => {
    navigator.clipboard.writeText(username);
    alert('Copied!');
  };

  return (
    <div className="top-unfollowers">
      <div className="unfollowers-header">
        <div>
          <h2>Top Unfollowers</h2>
          <p className="unfollowers-subtitle">Tổng hợp tất cả TikTok accounts của bạn</p>
        </div>
        <button className="refresh-btn" onClick={fetchTopUnfollowers} disabled={loading}>
          🔄 Refresh
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : unfollowers.length === 0 ? (
        <p className="empty-message">
          Chưa có dữ liệu unfollow. Tạo ít nhất 2 snapshot cho một account để bắt đầu tracking!
        </p>
      ) : (
        <div className="unfollowers-table">
          <div className="table-header">
            <div className="col-rank">Rank</div>
            <div className="col-tiktok">TikTok Account</div>
            <div className="col-username">Unfollower</div>
            <div className="col-count">Số lần unfollow</div>
            <div className="col-action"></div>
          </div>
          {unfollowers.map((u, index) => (
            <div key={u.id} className="table-row">
              <div className="col-rank">#{index + 1}</div>
              <div className="col-tiktok">
                <span className="tiktok-badge">@{u.tikTokUsername}</span>
              </div>
              <div className="col-username">@{u.username}</div>
              <div className="col-count">
                <span className="count-badge">{u.unfollowCount}×</span>
              </div>
              <div className="col-action">
                <button onClick={() => copyToClipboard(u.username)} className="copy-btn">
                  Copy
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default TopUnfollowers;
