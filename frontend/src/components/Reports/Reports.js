import React, { useState, useEffect, useCallback } from 'react';
import { trackingService } from '../../services/api';
import './Reports.css';

function Reports({ accountId, username }) {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  const fetchReports = useCallback(async () => {
    setLoading(true);
    try {
      const res = await trackingService.getTrackingHistory(accountId);
      setReports(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, [accountId]);

  useEffect(() => {
    if (accountId) { setReports([]); setExpandedId(null); fetchReports(); }
  }, [accountId, fetchReports]);

  const copy = (text) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="reports">
      <div className="reports-page-header">
        <div>
          <h2 className="reports-page-title">Tracking Reports</h2>
          <p className="reports-page-sub">Account: <strong>@{username}</strong></p>
        </div>
        <button className="btn-refresh" onClick={fetchReports} disabled={loading}>
          ↻ Refresh
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading reports</div>
      ) : reports.length === 0 ? (
        <div className="empty-state">
          <span className="empty-icon">📭</span>
          No snapshots yet for <strong>@{username}</strong>.<br />
          Go to Accounts and click 📸 Track.
        </div>
      ) : (
        reports.map((report, index) => {
          const isFirst = report.isFirstTracking;
          const added   = report.comparison?.followersAdded ?? 0;
          const removed = report.comparison?.followersRemoved ?? 0;
          const net     = report.comparison?.netChange ?? 0;
          const addedList   = (report.followerChanges || []).filter(f => f.changeType === 'added');
          const removedList = (report.followerChanges || []).filter(f => f.changeType === 'removed');
          const isOpen = expandedId === report.id;

          return (
            <div key={report.id} className="report-card">
              <div className="report-header" onClick={() => setExpandedId(isOpen ? null : report.id)}>
                <div className="report-index">#{reports.length - index}</div>

                <div className="report-meta">
                  <div className="report-date">
                    {new Date(report.createdAt).toLocaleString('vi-VN')}
                  </div>
                  <div className="report-stats">
                    <span className="pill pill-total">👥 {report.totalFollowers}</span>
                    {isFirst ? (
                      <span className="pill pill-first">⭐ Initial snapshot</span>
                    ) : (
                      <>
                        <span className="pill pill-added">+{added}</span>
                        <span className="pill pill-removed">−{removed}</span>
                        <span className={`pill ${net >= 0 ? 'pill-net-pos' : 'pill-net-neg'}`}>
                          {net >= 0 ? '+' : ''}{net} net
                        </span>
                      </>
                    )}
                  </div>
                </div>

                <span className={`expand-toggle ${isOpen ? 'open' : ''}`}>▼</span>
              </div>

              {isOpen && (
                <div className="report-details">
                  {isFirst ? (
                    <div className="first-snapshot-note">
                      <span>⭐</span>
                      <span>
                        Initial snapshot saved. Changes will appear from the next tracking onwards.
                      </span>
                    </div>
                  ) : (
                    <div className="changes-grid">
                      <div>
                        <p className="changes-section-title added">▲ New Followers ({added})</p>
                        {addedList.length === 0 ? (
                          <p className="no-changes">None this snapshot</p>
                        ) : (
                          <div className="changes-list">
                            {addedList.map(c => (
                              <div key={c.id} className="change-item added">
                                <span className="change-username">@{c.username}</span>
                                <button className="btn-copy" onClick={() => copy(c.username)}>copy</button>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>

                      <div>
                        <p className="changes-section-title removed">▼ Lost Followers ({removed})</p>
                        {removedList.length === 0 ? (
                          <p className="no-changes">None this snapshot</p>
                        ) : (
                          <div className="changes-list">
                            {removedList.map(c => (
                              <div key={c.id} className="change-item removed">
                                <span className="change-username">@{c.username}</span>
                                <button className="btn-copy" onClick={() => copy(c.username)}>copy</button>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })
      )}
    </div>
  );
}

export default Reports;
