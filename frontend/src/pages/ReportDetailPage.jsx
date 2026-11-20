import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, Clock, Shield, Calendar, DollarSign, Tag, MessageSquare, AlertTriangle, ExternalLink, CheckCircle2, Copy } from 'lucide-react';
import { reportsApi } from '../api/reportsApi';
import { STATUS_COLORS } from './MyReportsPage';
import { useAuth } from '../context/AuthContext';

export default function ReportDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, loading: authLoading } = useAuth();

  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      navigate(`/login?redirect=/reports/${id}`);
      return;
    }

    if (isAuthenticated && id) {
      loadReport();
    }
  }, [id, isAuthenticated, authLoading, navigate]);

  const loadReport = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await reportsApi.getReportById(id);
      if (res.success && res.data) {
        setReport(res.data);
      } else {
        setError(res.message || 'Report not found or access denied.');
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load report details.');
    } finally {
      setLoading(false);
    }
  };

  const copyRefCode = () => {
    if (report?.referenceCode) {
      navigator.clipboard.writeText(report.referenceCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  if (authLoading || loading) {
    return (
      <div className="container" style={{ padding: '3rem 1rem', textAlign: 'center' }}>
        <div style={{ display: 'inline-block', width: '36px', height: '36px', border: '3px solid #E2E8F0', borderTopColor: '#1E3A8A', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading incident overview...</p>
      </div>
    );
  }

  if (error || !report) {
    return (
      <div className="container" style={{ padding: '3rem 1rem', maxWidth: '680px', textAlign: 'center' }}>
        <div className="card" style={{ padding: '2.5rem' }}>
          <AlertTriangle size={42} color="#DC2626" style={{ margin: '0 auto 1rem auto' }} />
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem' }}>Unable to Access Report</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>{error || 'Incident report was not found or you lack permission to view it.'}</p>
          <Link to="/reports" className="btn btn-primary">Return to My Reports</Link>
        </div>
      </div>
    );
  }

  const statusConfig = STATUS_COLORS[report.currentStatus] || STATUS_COLORS.SUBMITTED;

  return (
    <div className="container" style={{ padding: '2.5rem 1rem', maxWidth: '860px' }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/reports" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.875rem', fontWeight: 600, textDecoration: 'none' }}>
          <ArrowLeft size={16} /> Back to My Reports
        </Link>
      </div>

      {/* Header Card */}
      <div className="card" style={{ padding: '1.75rem', marginBottom: '1.75rem', borderLeft: `5px solid ${statusConfig.border}` }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '1.5rem', fontWeight: 800, fontFamily: 'monospace', color: '#1E3A8A' }}>
                {report.referenceCode}
              </span>
              <button
                type="button"
                onClick={copyRefCode}
                className="btn btn-secondary"
                style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
                title="Copy Reference Code"
              >
                <Copy size={13} />
                {copied ? 'Copied' : 'Copy'}
              </button>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', margin: 0 }}>
              Submitted on {new Date(report.submittedAt).toLocaleDateString()} at {new Date(report.submittedAt).toLocaleTimeString()}
            </p>
          </div>

          <div style={{ textAlign: 'right' }}>
            <span style={{
              display: 'inline-block',
              padding: '0.35rem 0.85rem',
              borderRadius: '9999px',
              fontSize: '0.85rem',
              fontWeight: 700,
              background: statusConfig.bg,
              color: statusConfig.text,
              border: `1px solid ${statusConfig.border}`
            }}>
              {statusConfig.label}
            </span>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid #F1F5F9' }}>
          <div>
            <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748B', fontWeight: 700, display: 'block', marginBottom: '0.2rem' }}>Incident Date</span>
            <span style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Calendar size={15} color="#64748B" /> {report.incidentDate}
            </span>
          </div>

          <div>
            <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748B', fontWeight: 700, display: 'block', marginBottom: '0.2rem' }}>Channel</span>
            <span style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)' }}>
              {report.incidentChannel}
            </span>
          </div>

          <div>
            <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748B', fontWeight: 700, display: 'block', marginBottom: '0.2rem' }}>Financial Loss</span>
            <span style={{ fontSize: '0.95rem', fontWeight: 700, color: report.financialLossAmount ? '#DC2626' : '#64748B', display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
              <DollarSign size={15} />
              {report.financialLossAmount != null ? `₹${Number(report.financialLossAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : 'None / Not reported'}
            </span>
          </div>

          <div>
            <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748B', fontWeight: 700, display: 'block', marginBottom: '0.2rem' }}>Categorisation</span>
            <span style={{ fontSize: '0.95rem', fontWeight: 600, color: report.reviewedCategory ? '#166534' : 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Tag size={15} color="#64748B" />
              {report.reviewedCategory ? `${report.reviewedCategory} (Confirmed)` : (report.userSelectedCategory || 'Unclassified')}
            </span>
          </div>
        </div>
      </div>

      {/* Official Emergency Advisory Notice */}
      <div style={{ background: '#FEF2F2', border: '1px solid #FECACA', borderRadius: '10px', padding: '1.25rem', marginBottom: '1.75rem' }}>
        <h4 style={{ color: '#991B1B', fontWeight: 700, fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.4rem', margin: '0 0 0.35rem 0' }}>
          <AlertTriangle size={16} /> Official Police & Emergency Guidance
        </h4>
        <p style={{ fontSize: '0.85rem', color: '#7F1D1D', lineHeight: 1.5, margin: 0 }}>
          CyberSaarthi provides educational classification and report structuring support. Submitting here does <strong>not</strong> notify local police authorities.
          For official investigation, asset freeze, or financial recovery, immediately call <strong>1930</strong> or register on{' '}
          <a href="https://cybercrime.gov.in" target="_blank" rel="noopener noreferrer" style={{ color: '#B91C1C', fontWeight: 700, textDecoration: 'underline' }}>
            cybercrime.gov.in <ExternalLink size={12} style={{ display: 'inline' }} />
          </a>.
        </p>
      </div>

      {/* Incident Narrative */}
      <div className="card" style={{ padding: '1.75rem', marginBottom: '1.75rem' }}>
        <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--text-main)' }}>
          Incident Narrative
        </h3>
        <p style={{ fontSize: '0.95rem', lineHeight: 1.6, color: '#334155', whiteSpace: 'pre-line', margin: 0, background: '#F8FAFC', padding: '1.25rem', borderRadius: '8px', border: '1px solid #E2E8F0' }}>
          {report.description}
        </p>
      </div>

      {/* Review Notes (Citizen Guidance) */}
      <div className="card" style={{ padding: '1.75rem', marginBottom: '1.75rem' }}>
        <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <MessageSquare size={18} />
          Educational Guidance & Review Feedback
        </h3>
        {report.reviewNotes && report.reviewNotes.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {report.reviewNotes.map((note) => (
              <div key={note.id} style={{ background: '#F0FDF4', border: '1px solid #BBF7D0', borderRadius: '8px', padding: '1rem' }}>
                <p style={{ fontSize: '0.9rem', color: '#166534', margin: '0 0 0.5rem 0', lineHeight: 1.5 }}>
                  {note.noteText}
                </p>
                <span style={{ fontSize: '0.75rem', color: '#4ADE80' }}>
                  {new Date(note.createdAt).toLocaleString()}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', margin: 0 }}>
            No review guidance notes added yet. Triage is in progress.
          </p>
        )}
      </div>

      {/* Status History Timeline */}
      <div className="card" style={{ padding: '1.75rem' }}>
        <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1rem', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Clock size={18} />
          Review & Triage Timeline
        </h3>
        <div style={{ position: 'relative', paddingLeft: '1.5rem', borderLeft: '2px solid #E2E8F0', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {report.statusHistory && report.statusHistory.map((item, idx) => {
            const itemConfig = STATUS_COLORS[item.toStatus] || STATUS_COLORS.SUBMITTED;
            return (
              <div key={item.id || idx} style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '-1.85rem', top: '2px', width: '12px', height: '12px', borderRadius: '50%', background: itemConfig.border, border: '2px solid #FFF' }} />
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.875rem', color: itemConfig.text }}>
                    {item.fromStatus ? `${item.fromStatus} → ${item.toStatus}` : item.toStatus}
                  </span>
                  <span style={{ fontSize: '0.75rem', color: '#64748B' }}>
                    {new Date(item.changedAt).toLocaleString()}
                  </span>
                </div>
                {item.changeReason && (
                  <p style={{ fontSize: '0.85rem', color: '#475569', margin: 0, fontStyle: 'italic' }}>
                    "{item.changeReason}"
                  </p>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
