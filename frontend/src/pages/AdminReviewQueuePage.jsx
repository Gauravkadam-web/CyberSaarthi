import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, Search, Filter, AlertCircle, X, CheckCircle, MessageSquare, Tag, Eye, Lock, Sparkles, RefreshCw } from 'lucide-react';
import { reportsApi } from '../api/reportsApi';
import { awarenessApi } from '../api/awarenessApi';
import { STATUS_COLORS } from './MyReportsPage';
import { useAuth } from '../context/AuthContext';

const VALID_TRANSITIONS = {
  SUBMITTED: ['UNDER_REVIEW'],
  UNDER_REVIEW: ['NEEDS_INFORMATION', 'REVIEWED', 'CLOSED'],
  NEEDS_INFORMATION: ['UNDER_REVIEW', 'CLOSED'],
  REVIEWED: ['CLOSED'],
  CLOSED: []
};

export default function AdminReviewQueuePage() {
  const { isAuthenticated, isAdmin, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [reports, setReports] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Selected report for review modal
  const [selectedReport, setSelectedReport] = useState(null);
  const [modalLoading, setModalLoading] = useState(false);
  const [statusForm, setStatusForm] = useState({ newStatus: '', reason: '', reviewedCategoryId: '' });
  const [noteForm, setNoteForm] = useState({ noteText: '', visibility: 'INTERNAL' });
  const [actionSuccess, setActionSuccess] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [reclassifying, setReclassifying] = useState(false);

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !isAdmin)) {
      navigate('/login?redirect=/admin/reports');
      return;
    }

    if (isAuthenticated && isAdmin) {
      loadCategories();
      loadQueue();
    }
  }, [isAuthenticated, isAdmin, authLoading, statusFilter, page]);

  const loadCategories = async () => {
    try {
      const res = await awarenessApi.getCategories();
      if (res.success && res.data) {
        setCategories(res.data);
      }
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const loadQueue = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await reportsApi.getAdminQueue({
        status: statusFilter || null,
        query: searchQuery.trim() || null,
        page,
        size: 10
      });
      if (res.success && res.data) {
        setReports(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
      } else {
        setError(res.message || 'Failed to load admin queue');
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Error loading triage queue');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    loadQueue();
  };

  const openReviewModal = async (reportId) => {
    setModalLoading(true);
    setActionSuccess(null);
    setActionError(null);
    try {
      const res = await reportsApi.getAdminReportById(reportId);
      if (res.success && res.data) {
        setSelectedReport(res.data);
        setStatusForm({
          newStatus: VALID_TRANSITIONS[res.data.currentStatus]?.[0] || '',
          reason: '',
          reviewedCategoryId: res.data.reviewedCategoryId || ''
        });
        setNoteForm({ noteText: '', visibility: 'INTERNAL' });
      }
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to fetch report detail');
    } finally {
      setModalLoading(false);
    }
  };

  const handleStatusUpdate = async (e) => {
    e.preventDefault();
    if (!statusForm.newStatus || !statusForm.reason.trim()) {
      setActionError('New status and transition reason are required.');
      return;
    }

    setActionError(null);
    try {
      const res = await reportsApi.updateStatus(selectedReport.id, {
        newStatus: statusForm.newStatus,
        reason: statusForm.reason.trim(),
        reviewedCategoryId: statusForm.reviewedCategoryId || null
      });

      if (res.success && res.data) {
        setSelectedReport(res.data);
        setActionSuccess(`Status transitioned to ${res.data.currentStatus}`);
        setStatusForm(prev => ({ ...prev, reason: '' }));
        loadQueue();
      }
    } catch (err) {
      setActionError(err.response?.data?.message || 'Status transition failed');
    }
  };

  const handleAddNote = async (e) => {
    e.preventDefault();
    if (!noteForm.noteText.trim()) return;

    setActionError(null);
    try {
      const res = await reportsApi.addReviewNote(selectedReport.id, {
        noteText: noteForm.noteText.trim(),
        visibility: noteForm.visibility
      });

      if (res.success && res.data) {
        // Refresh report detail to show newly attached note
        const refreshed = await reportsApi.getAdminReportById(selectedReport.id);
        if (refreshed.success) setSelectedReport(refreshed.data);
        setNoteForm({ noteText: '', visibility: 'INTERNAL' });
        setActionSuccess('Review note recorded successfully');
      }
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to attach note');
    }
  };

  const handleReclassify = async () => {
    if (!selectedReport) return;
    setReclassifying(true);
    setActionSuccess(null);
    setActionError(null);
    try {
      const res = await reportsApi.reclassifyReport(selectedReport.id);
      if (res.success && res.data) {
        const refreshed = await reportsApi.getAdminReportById(selectedReport.id);
        if (refreshed.success) setSelectedReport(refreshed.data);
        setActionSuccess(`ML reclassification complete: ${res.data.predictedCategoryName || res.data.predictedCategoryCode}`);
        loadQueue();
      }
    } catch (err) {
      setActionError(err.response?.data?.message || 'Reclassification failed');
    } finally {
      setReclassifying(false);
    }
  };

  const applyMlCategory = () => {
    if (selectedReport?.latestPrediction?.predictedCategoryId) {
      setStatusForm(prev => ({ ...prev, reviewedCategoryId: selectedReport.latestPrediction.predictedCategoryId }));
      setActionSuccess('Applied ML suggested category to confirmed category selection.');
    }
  };

  if (authLoading || loading && reports.length === 0) {
    return (
      <div className="container" style={{ padding: '3rem 1rem', textAlign: 'center' }}>
        <div style={{ display: 'inline-block', width: '36px', height: '36px', border: '3px solid #E2E8F0', borderTopColor: '#1E3A8A', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
        <p style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading administrator triage queue...</p>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2.5rem 1rem', maxWidth: '1100px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.75rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-main)', margin: 0, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <ShieldCheck size={28} color="#1E3A8A" />
            Admin Incident Review Queue
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem', marginBottom: 0 }}>
            Triage incoming citizen incident reports, update investigation states, and provide advisory notes.
          </p>
        </div>
      </div>

      {/* Filters and Search Bar */}
      <div className="card" style={{ padding: '1.25rem', marginBottom: '1.75rem', background: '#F8FAFC' }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <div style={{ minWidth: '180px' }}>
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              style={{ width: '100%', padding: '0.6rem 0.85rem', borderRadius: '8px', border: '1px solid #CBD5E1', background: '#FFF', fontSize: '0.9rem' }}
            >
              <option value="">All Statuses</option>
              <option value="SUBMITTED">SUBMITTED</option>
              <option value="UNDER_REVIEW">UNDER REVIEW</option>
              <option value="NEEDS_INFORMATION">NEEDS INFORMATION</option>
              <option value="REVIEWED">REVIEWED</option>
              <option value="CLOSED">CLOSED</option>
            </select>
          </div>

          <div style={{ position: 'relative', flex: 1, minWidth: '240px' }}>
            <Search size={18} color="#64748B" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="Search by reference code or description..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ width: '100%', padding: '0.6rem 0.75rem 0.6rem 2.5rem', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '0.9rem' }}
            />
          </div>

          <button type="submit" className="btn btn-secondary" style={{ padding: '0.6rem 1.25rem', fontWeight: 600 }}>
            Apply Filter
          </button>
        </form>
      </div>

      {error && (
        <div style={{ background: '#FEF2F2', border: '1px solid #FCA5A5', color: '#991B1B', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Reports Table */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead style={{ background: '#F1F5F9', borderBottom: '1px solid #E2E8F0', color: '#475569', fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              <tr>
                <th style={{ padding: '0.85rem 1rem' }}>Reference Code</th>
                <th style={{ padding: '0.85rem 1rem' }}>Status</th>
                <th style={{ padding: '0.85rem 1rem' }}>Channel</th>
                <th style={{ padding: '0.85rem 1rem' }}>Loss (INR)</th>
                <th style={{ padding: '0.85rem 1rem' }}>Category</th>
                <th style={{ padding: '0.85rem 1rem' }}>ML Advisory</th>
                <th style={{ padding: '0.85rem 1rem' }}>Submitted At</th>
                <th style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {reports.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ padding: '2.5rem', textAlign: 'center', color: '#64748B' }}>
                    No incident reports found matching current filters.
                  </td>
                </tr>
              ) : (
                reports.map((r) => {
                  const s = STATUS_COLORS[r.currentStatus] || STATUS_COLORS.SUBMITTED;
                  return (
                    <tr key={r.id} style={{ borderBottom: '1px solid #F1F5F9' }}>
                      <td style={{ padding: '0.85rem 1rem', fontFamily: 'monospace', fontWeight: 700, color: '#1E3A8A' }}>
                        {r.referenceCode}
                      </td>
                      <td style={{ padding: '0.85rem 1rem' }}>
                        <span style={{
                          padding: '0.2rem 0.6rem',
                          borderRadius: '9999px',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          background: s.bg,
                          color: s.text,
                          border: `1px solid ${s.border}`
                        }}>
                          {s.label}
                        </span>
                      </td>
                      <td style={{ padding: '0.85rem 1rem', color: '#334155' }}>{r.incidentChannel}</td>
                      <td style={{ padding: '0.85rem 1rem', color: r.financialLossAmount ? '#DC2626' : '#64748B', fontWeight: r.financialLossAmount ? 600 : 400 }}>
                        {r.financialLossAmount != null ? `₹${Number(r.financialLossAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : '-'}
                      </td>
                      <td style={{ padding: '0.85rem 1rem', color: '#334155' }}>
                        {r.reviewedCategory || r.userSelectedCategory || 'Unclassified'}
                      </td>
                      <td style={{ padding: '0.85rem 1rem' }}>
                        {r.predictedCategoryName ? (
                          <span style={{ fontSize: '0.8rem', background: '#F0FDF4', color: '#166534', padding: '0.2rem 0.5rem', borderRadius: '4px', border: '1px solid #BBF7D0', fontWeight: 600 }}>
                            {r.predictedCategoryName}
                          </span>
                        ) : (
                          <span style={{ fontSize: '0.75rem', color: '#94A3B8' }}>Pending</span>
                        )}
                      </td>
                      <td style={{ padding: '0.85rem 1rem', color: '#64748B', fontSize: '0.85rem' }}>
                        {new Date(r.submittedAt).toLocaleDateString()}
                      </td>
                      <td style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>
                        <button
                          onClick={() => openReviewModal(r.id)}
                          className="btn btn-secondary"
                          style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem' }}
                        >
                          Triage & Review
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', borderTop: '1px solid #E2E8F0', background: '#F8FAFC' }}>
            <button
              disabled={page === 0}
              onClick={() => setPage(p => Math.max(0, p - 1))}
              className="btn btn-secondary"
              style={{ padding: '0.35rem 0.8rem', fontSize: '0.8rem' }}
            >
              Previous
            </button>
            <span style={{ fontSize: '0.85rem', color: '#64748B' }}>
              Page {page + 1} of {totalPages}
            </span>
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => p + 1)}
              className="btn btn-secondary"
              style={{ padding: '0.35rem 0.8rem', fontSize: '0.8rem' }}
            >
              Next
            </button>
          </div>
        )}
      </div>

      {/* Triage & Review Modal */}
      {selectedReport && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(15, 23, 42, 0.65)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000,
          padding: '1rem'
        }}>
          <div className="card" style={{
            maxWidth: '820px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '2rem',
            position: 'relative'
          }}>
            <button
              onClick={() => setSelectedReport(null)}
              style={{ position: 'absolute', right: '1.25rem', top: '1.25rem', background: 'none', border: 'none', cursor: 'pointer', color: '#64748B' }}
            >
              <X size={22} />
            </button>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <span style={{ fontSize: '1.5rem', fontWeight: 800, fontFamily: 'monospace', color: '#1E3A8A' }}>
                {selectedReport.referenceCode}
              </span>
              <span style={{
                padding: '0.2rem 0.7rem',
                borderRadius: '9999px',
                fontSize: '0.8rem',
                fontWeight: 700,
                background: (STATUS_COLORS[selectedReport.currentStatus] || STATUS_COLORS.SUBMITTED).bg,
                color: (STATUS_COLORS[selectedReport.currentStatus] || STATUS_COLORS.SUBMITTED).text
              }}>
                {selectedReport.currentStatus}
              </span>
            </div>

            {actionSuccess && (
              <div style={{ background: '#DCFCE7', border: '1px solid #86EFAC', color: '#166534', padding: '0.75rem', borderRadius: '6px', marginBottom: '1rem', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <CheckCircle size={16} />
                <span>{actionSuccess}</span>
              </div>
            )}
            {actionError && (
              <div style={{ background: '#FEF2F2', border: '1px solid #FCA5A5', color: '#991B1B', padding: '0.75rem', borderRadius: '6px', marginBottom: '1rem', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <AlertCircle size={16} />
                <span>{actionError}</span>
              </div>
            )}

            {/* Incident Narrative */}
            <div style={{ background: '#F8FAFC', padding: '1.25rem', borderRadius: '8px', border: '1px solid #E2E8F0', marginBottom: '1.5rem' }}>
              <h4 style={{ fontSize: '0.9rem', fontWeight: 700, margin: '0 0 0.5rem 0', color: '#334155' }}>Citizen Incident Description</h4>
              <p style={{ fontSize: '0.9rem', color: '#1E293B', margin: 0, lineHeight: 1.5, whiteSpace: 'pre-line' }}>{selectedReport.description}</p>
              <div style={{ display: 'flex', gap: '1.5rem', fontSize: '0.8rem', color: '#64748B', marginTop: '0.75rem', borderTop: '1px solid #E2E8F0', paddingTop: '0.5rem' }}>
                <span>Channel: <strong>{selectedReport.incidentChannel}</strong></span>
                <span>Date: <strong>{selectedReport.incidentDate}</strong></span>
                <span>Loss: <strong>{selectedReport.financialLossAmount ? `₹${selectedReport.financialLossAmount}` : 'None'}</strong></span>
              </div>
            </div>

            {/* ML Advisory Analysis Card */}
            <div style={{ background: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                <h4 style={{ fontSize: '0.95rem', fontWeight: 700, margin: 0, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Sparkles size={16} color="#2563EB" /> ML Advisory Classification Analysis
                </h4>
                <button
                  type="button"
                  onClick={handleReclassify}
                  disabled={reclassifying}
                  className="btn btn-secondary"
                  style={{ padding: '0.35rem 0.75rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}
                >
                  <RefreshCw size={13} style={{ animation: reclassifying ? 'spin 1s linear infinite' : 'none' }} />
                  {reclassifying ? 'Re-running...' : 'Re-run ML Inference'}
                </button>
              </div>

              {selectedReport.latestPrediction ? (
                <div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', marginBottom: '0.75rem', background: '#FFF', padding: '0.85rem', borderRadius: '6px', border: '1px solid #E2E8F0' }}>
                    <div>
                      <span style={{ fontSize: '0.75rem', color: '#64748B', display: 'block', textTransform: 'uppercase', fontWeight: 700 }}>Predicted Category</span>
                      <span style={{ fontSize: '0.9rem', fontWeight: 700, color: '#1E40AF' }}>
                        {selectedReport.latestPrediction.predictedCategoryName || selectedReport.latestPrediction.predictedCategoryCode}
                      </span>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.75rem', color: '#64748B', display: 'block', textTransform: 'uppercase', fontWeight: 700 }}>Confidence Score</span>
                      <span style={{ fontSize: '0.9rem', fontWeight: 700, color: (selectedReport.latestPrediction.confidenceScore >= 0.60) ? '#16A34A' : '#D97706' }}>
                        {selectedReport.latestPrediction.confidenceScore != null ? `${(Number(selectedReport.latestPrediction.confidenceScore) * 100).toFixed(1)}%` : 'N/A'} ({selectedReport.latestPrediction.inferenceStatus})
                      </span>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.75rem', color: '#64748B', display: 'block', textTransform: 'uppercase', fontWeight: 700 }}>Model Version</span>
                      <span style={{ fontSize: '0.85rem', color: '#475569' }}>
                        {selectedReport.latestPrediction.modelName} ({selectedReport.latestPrediction.modelVersion})
                      </span>
                    </div>
                  </div>

                  {selectedReport.latestPrediction.requiresManualReview && (
                    <div style={{ background: '#FEF3C7', border: '1px solid #FCD34D', color: '#92400E', padding: '0.5rem 0.75rem', borderRadius: '6px', fontSize: '0.8rem', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                      <AlertCircle size={14} />
                      <span>Flagged for Human Review: Confidence is below threshold (60%) or fallback inference was used.</span>
                    </div>
                  )}

                  {selectedReport.latestPrediction.predictedCategoryId && (
                    <button
                      type="button"
                      onClick={applyMlCategory}
                      className="btn btn-secondary"
                      style={{ padding: '0.35rem 0.8rem', fontSize: '0.8rem', background: '#EFF6FF', color: '#1D4ED8', borderColor: '#BFDBFE' }}
                    >
                      ✓ Apply as Confirmed Category
                    </button>
                  )}
                </div>
              ) : (
                <p style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>
                  No ML prediction recorded yet for this incident report. Click "Re-run ML Inference" to generate an advisory classification.
                </p>
              )}
            </div>

            {/* Status Transition Panel */}
            <div style={{ border: '1px solid #E2E8F0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem', background: '#FFF' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: 700, margin: '0 0 1rem 0', color: 'var(--text-main)' }}>
                Transition Investigation State
              </h4>
              <form onSubmit={handleStatusUpdate}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', fontWeight: 600, fontSize: '0.825rem', marginBottom: '0.35rem' }}>Next Status *</label>
                    <select
                      value={statusForm.newStatus}
                      onChange={(e) => setStatusForm(prev => ({ ...prev, newStatus: e.target.value }))}
                      required
                      style={{ width: '100%', padding: '0.55rem', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '0.875rem' }}
                    >
                      <option value="">Select Next Status</option>
                      {(VALID_TRANSITIONS[selectedReport.currentStatus] || []).map(st => (
                        <option key={st} value={st}>{st}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontWeight: 600, fontSize: '0.825rem', marginBottom: '0.35rem' }}>Confirmed Category</label>
                    <select
                      value={statusForm.reviewedCategoryId}
                      onChange={(e) => setStatusForm(prev => ({ ...prev, reviewedCategoryId: e.target.value }))}
                      style={{ width: '100%', padding: '0.55rem', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '0.875rem' }}
                    >
                      <option value="">Keep current category</option>
                      {categories.map(cat => (
                        <option key={cat.id} value={cat.id}>{cat.displayName}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', fontWeight: 600, fontSize: '0.825rem', marginBottom: '0.35rem' }}>Audit Reason for Transition *</label>
                  <input
                    type="text"
                    required
                    minLength={5}
                    placeholder="e.g. Verified transaction SMS screenshot; initiating review."
                    value={statusForm.reason}
                    onChange={(e) => setStatusForm(prev => ({ ...prev, reason: e.target.value }))}
                    style={{ width: '100%', padding: '0.55rem', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '0.875rem' }}
                  />
                </div>

                <button
                  type="submit"
                  disabled={!statusForm.newStatus || statusForm.reason.length < 5}
                  className="btn btn-primary"
                  style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                >
                  Confirm State Transition
                </button>
              </form>
            </div>

            {/* Review Notes Section */}
            <div style={{ border: '1px solid #E2E8F0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem', background: '#FFF' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: 700, margin: '0 0 1rem 0', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <MessageSquare size={16} /> Review Notes
              </h4>

              {/* Existing Notes List */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1rem' }}>
                {selectedReport.reviewNotes && selectedReport.reviewNotes.length > 0 ? (
                  selectedReport.reviewNotes.map((note) => (
                    <div key={note.id} style={{
                      padding: '0.75rem',
                      borderRadius: '6px',
                      background: note.visibility === 'INTERNAL' ? '#FEF9C3' : '#F0FDF4',
                      border: `1px solid ${note.visibility === 'INTERNAL' ? '#FDE047' : '#BBF7D0'}`
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                        <span style={{ fontSize: '0.75rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.25rem', color: note.visibility === 'INTERNAL' ? '#854D0E' : '#166534' }}>
                          {note.visibility === 'INTERNAL' ? <Lock size={12} /> : <Eye size={12} />}
                          {note.visibility}
                        </span>
                        <span style={{ fontSize: '0.7rem', color: '#64748B' }}>{new Date(note.createdAt).toLocaleString()}</span>
                      </div>
                      <p style={{ fontSize: '0.85rem', margin: 0, color: '#1E293B' }}>{note.noteText}</p>
                    </div>
                  ))
                ) : (
                  <p style={{ color: '#94A3B8', fontSize: '0.825rem', margin: 0 }}>No review notes recorded yet.</p>
                )}
              </div>

              {/* Add Note Form */}
              <form onSubmit={handleAddNote} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                <input
                  type="text"
                  placeholder="Attach review note..."
                  value={noteForm.noteText}
                  onChange={(e) => setNoteForm(prev => ({ ...prev, noteText: e.target.value }))}
                  required
                  style={{ flex: 1, minWidth: '220px', padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '0.875rem' }}
                />
                <select
                  value={noteForm.visibility}
                  onChange={(e) => setNoteForm(prev => ({ ...prev, visibility: e.target.value }))}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '0.875rem', background: '#FFF' }}
                >
                  <option value="INTERNAL">INTERNAL (Admin only)</option>
                  <option value="USER_VISIBLE">USER_VISIBLE (Citizen advisory)</option>
                </select>
                <button
                  type="submit"
                  disabled={!noteForm.noteText.trim()}
                  className="btn btn-secondary"
                  style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                >
                  Add Note
                </button>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
