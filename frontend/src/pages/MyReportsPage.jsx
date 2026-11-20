import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FileText, Search, PlusCircle, AlertCircle, Clock, ChevronRight, Calendar, DollarSign, Tag } from 'lucide-react';
import { reportsApi } from '../api/reportsApi';
import { useAuth } from '../context/AuthContext';

export const STATUS_COLORS = {
  SUBMITTED: { bg: '#DBEAFE', text: '#1E40AF', border: '#93C5FD', label: 'Submitted' },
  UNDER_REVIEW: { bg: '#FEF3C7', text: '#92400E', border: '#FCD34D', label: 'Under Review' },
  NEEDS_INFORMATION: { bg: '#FFEDD5', text: '#9A3412', border: '#FDBA74', label: 'Needs Info' },
  REVIEWED: { bg: '#DCFCE7', text: '#166534', border: '#86EFAC', label: 'Reviewed' },
  CLOSED: { bg: '#F1F5F9', text: '#475569', border: '#CBD5E1', label: 'Closed' }
};

export default function MyReportsPage() {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchCode, setSearchCode] = useState('');
  const [trackError, setTrackError] = useState(null);
  const [tracking, setTracking] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      navigate('/login?redirect=/reports');
      return;
    }

    if (isAuthenticated) {
      loadReports();
    }
  }, [isAuthenticated, authLoading, navigate]);

  const loadReports = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await reportsApi.getMyReports();
      if (res.success && res.data) {
        setReports(res.data);
      } else {
        setError(res.message || 'Failed to fetch incident reports');
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Error loading reports');
    } finally {
      setLoading(false);
    }
  };

  const handleTrackByCode = async (e) => {
    e.preventDefault();
    if (!searchCode.trim()) return;

    setTracking(true);
    setTrackError(null);
    try {
      const res = await reportsApi.trackReportByReferenceCode(searchCode.trim().toUpperCase());
      if (res.success && res.data) {
        navigate(`/reports/${res.data.id}`);
      } else {
        setTrackError('Report reference code not found or access denied.');
      }
    } catch (err) {
      setTrackError(err.response?.data?.message || 'Report not found or not accessible under your account.');
    } finally {
      setTracking(false);
    }
  };

  if (authLoading || loading) {
    return (
      <div className="container" style={{ padding: '3rem 1rem', textAlign: 'center' }}>
        <div style={{ display: 'inline-block', width: '36px', height: '36px', border: '3px solid #E2E8F0', borderTopColor: '#1E3A8A', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading your incident reports...</p>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2.5rem 1rem', maxWidth: '960px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.75rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-main)', margin: 0 }}>
            My Incident Reports
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem', marginBottom: 0 }}>
            Track review status, triage history, and educational feedback for your documented incidents.
          </p>
        </div>
        <Link to="/reports/new" className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
          <PlusCircle size={18} />
          Report New Incident
        </Link>
      </div>

      {/* Quick Tracking Search Bar */}
      <div className="card" style={{ padding: '1.25rem', marginBottom: '2rem', background: '#F8FAFC' }}>
        <form onSubmit={handleTrackByCode} style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <div style={{ position: 'relative', flex: 1, minWidth: '240px' }}>
            <Search size={18} color="#64748B" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="Track by reference code (e.g. CS-2026-A8K2F)"
              value={searchCode}
              onChange={(e) => setSearchCode(e.target.value.toUpperCase())}
              style={{ width: '100%', padding: '0.65rem 0.75rem 0.65rem 2.5rem', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '0.9rem', fontFamily: 'monospace' }}
            />
          </div>
          <button
            type="submit"
            disabled={tracking || !searchCode.trim()}
            className="btn btn-secondary"
            style={{ padding: '0.65rem 1.25rem', fontWeight: 600 }}
          >
            {tracking ? 'Searching...' : 'Track'}
          </button>
        </form>
        {trackError && (
          <p style={{ color: '#DC2626', fontSize: '0.825rem', marginTop: '0.5rem', marginBottom: 0 }}>
            {trackError}
          </p>
        )}
      </div>

      {error && (
        <div style={{ background: '#FEF2F2', border: '1px solid #FCA5A5', color: '#991B1B', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {reports.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3.5rem 1.5rem' }}>
          <div style={{ display: 'inline-flex', padding: '1rem', background: '#EFF6FF', borderRadius: '50%', marginBottom: '1rem' }}>
            <FileText size={40} color="#1D4ED8" />
          </div>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>
            No incident reports yet
          </h3>
          <p style={{ color: 'var(--text-muted)', maxWidth: '460px', margin: '0 auto 1.5rem auto', fontSize: '0.95rem' }}>
            If you encountered a suspicious online call, phishing message, or fraudulent financial debit, document it here to receive categorized awareness analysis.
          </p>
          <Link to="/reports/new" className="btn btn-primary">
            Report an Incident
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {reports.map((report) => {
            const statusConfig = STATUS_COLORS[report.currentStatus] || STATUS_COLORS.SUBMITTED;
            return (
              <div
                key={report.id}
                className="card"
                style={{ padding: '1.25rem 1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', transition: 'box-shadow 0.2s ease', borderLeft: `4px solid ${statusConfig.border}` }}
              >
                <div style={{ flex: 1, minWidth: '280px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.4rem', flexWrap: 'wrap' }}>
                    <span style={{ fontFamily: 'monospace', fontWeight: 800, fontSize: '1.05rem', color: '#1E3A8A' }}>
                      {report.referenceCode}
                    </span>
                    <span style={{
                      padding: '0.2rem 0.65rem',
                      borderRadius: '9999px',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      background: statusConfig.bg,
                      color: statusConfig.text,
                      border: `1px solid ${statusConfig.border}`
                    }}>
                      {statusConfig.label}
                    </span>
                    {report.reviewedCategory ? (
                      <span style={{ fontSize: '0.8rem', background: '#F1F5F9', padding: '0.2rem 0.5rem', borderRadius: '4px', color: '#334155', fontWeight: 600 }}>
                        <Tag size={12} style={{ display: 'inline', marginRight: '4px' }} />
                        {report.reviewedCategory}
                      </span>
                    ) : report.userSelectedCategory ? (
                      <span style={{ fontSize: '0.8rem', background: '#F8FAFC', padding: '0.2rem 0.5rem', borderRadius: '4px', color: '#64748B' }}>
                        {report.userSelectedCategory}
                      </span>
                    ) : null}
                  </div>

                  <div style={{ display: 'flex', gap: '1.25rem', fontSize: '0.85rem', color: '#64748B', flexWrap: 'wrap' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                      <Calendar size={14} />
                      Incident: {report.incidentDate}
                    </span>
                    <span>Channel: {report.incidentChannel}</span>
                    {report.financialLossAmount != null && (
                      <span style={{ display: 'flex', alignItems: 'center', gap: '0.2rem', color: '#DC2626', fontWeight: 600 }}>
                        <DollarSign size={14} />
                        ₹{Number(report.financialLossAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    )}
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                      <Clock size={14} />
                      Submitted: {new Date(report.submittedAt).toLocaleDateString()}
                    </span>
                  </div>
                </div>

                <div>
                  <Link
                    to={`/reports/${report.id}`}
                    className="btn btn-secondary"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.875rem', padding: '0.5rem 0.9rem' }}
                  >
                    View Details <ChevronRight size={16} />
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
