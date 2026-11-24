import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BarChart3,
  TrendingUp,
  FileSpreadsheet,
  Cpu,
  GraduationCap,
  ShieldAlert,
  Download,
  AlertCircle,
  CheckCircle,
  HelpCircle,
  RefreshCw,
  Layers,
  IndianRupee,
  Users,
  ShieldCheck,
  Lock
} from 'lucide-react';
import { analyticsApi } from '../api/analyticsApi';
import { useAuth } from '../context/AuthContext';

export default function AdminDashboardPage() {
  const { isAuthenticated, isAdmin, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('operational'); // 'operational' | 'educational' | 'ml' | 'export'
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  // Analytics states
  const [overview, setOverview] = useState(null);
  const [categories, setCategories] = useState([]);
  const [channels, setChannels] = useState([]);
  const [assessments, setAssessments] = useState(null);
  const [mlDiagnostics, setMlDiagnostics] = useState(null);

  // Export states
  const [exportingIncidents, setExportingIncidents] = useState(false);
  const [exportingAssessments, setExportingAssessments] = useState(false);
  const [exportSuccessMessage, setExportSuccessMessage] = useState(null);

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !isAdmin)) {
      navigate('/login?redirect=/admin/dashboard');
      return;
    }

    if (isAuthenticated && isAdmin) {
      loadAllAnalytics();
    }
  }, [isAuthenticated, isAdmin, authLoading]);

  const loadAllAnalytics = async () => {
    try {
      setLoading(true);
      setError(null);
      const [ov, cat, ch, ass, ml] = await Promise.all([
        analyticsApi.getOverview(),
        analyticsApi.getCategoryBreakdown(),
        analyticsApi.getChannelBreakdown(),
        analyticsApi.getAssessmentStats(),
        analyticsApi.getMlDiagnostics()
      ]);
      setOverview(ov);
      setCategories(cat);
      setChannels(ch);
      setAssessments(ass);
      setMlDiagnostics(ml);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
      setError('Failed to load administrative analytics data. Please verify backend service availability.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadAllAnalytics();
  };

  const handleExportIncidents = async () => {
    try {
      setExportingIncidents(true);
      setExportSuccessMessage(null);
      await analyticsApi.downloadIncidentsCsv();
      setExportSuccessMessage('De-identified incident dataset CSV exported successfully. Audit event recorded.');
    } catch (err) {
      console.error('Failed to export incidents:', err);
      alert('Export failed. Please check permissions.');
    } finally {
      setExportingIncidents(false);
    }
  };

  const handleExportAssessments = async () => {
    try {
      setExportingAssessments(true);
      setExportSuccessMessage(null);
      await analyticsApi.downloadAssessmentsCsv();
      setExportSuccessMessage('De-identified assessment dataset CSV exported successfully. Audit event recorded.');
    } catch (err) {
      console.error('Failed to export assessments:', err);
      alert('Export failed. Please check permissions.');
    } finally {
      setExportingAssessments(false);
    }
  };

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return '₹0';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(val);
  };

  if (authLoading || loading) {
    return (
      <div className="container page-wrapper" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <div style={{ textAlign: 'center' }}>
          <RefreshCw size={36} className="spin" color="#1E3A8A" style={{ marginBottom: '1rem', animation: 'spin 1s linear infinite' }} />
          <h2 style={{ fontSize: '1.25rem', color: '#1E3A8A' }}>Loading Intelligence & Analytics...</h2>
          <p style={{ color: 'var(--text-muted)' }}>Aggregating operational incident statistics, educational telemetry, and ML diagnostics.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container page-wrapper">
      {/* Header & Controls */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
            <h1 style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--primary-color)' }}>
              Operational Intelligence & Analytics
            </h1>
            <span style={{ backgroundColor: '#DBEAFE', color: '#1E40AF', fontSize: '0.75rem', fontWeight: 700, padding: '0.25rem 0.65rem', borderRadius: '9999px', textTransform: 'uppercase' }}>
              Admin Exclusive
            </span>
          </div>
          <p style={{ color: 'var(--text-muted)', maxWidth: '750px', fontSize: '0.95rem' }}>
            Comprehensive reporting analytics, educational track assessment metrics, ML model diagnostics, and privacy-preserving de-identified research data exports.
          </p>
        </div>

        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="btn"
          style={{ backgroundColor: '#FFFFFF', border: '1px solid var(--border-color)', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
        >
          <RefreshCw size={16} className={refreshing ? 'spin' : ''} />
          {refreshing ? 'Refreshing...' : 'Refresh Metrics'}
        </button>
      </div>

      {/* Error Alert */}
      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', borderRadius: '8px', padding: '1rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#991B1B' }}>
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Academic Boundary & k-Anonymity Notice */}
      <div style={{ backgroundColor: '#F0FDF4', border: '1px solid #BBF7D0', borderRadius: '8px', padding: '0.85rem 1.25rem', marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#166534', fontSize: '0.875rem' }}>
        <ShieldCheck size={18} style={{ flexShrink: 0 }} />
        <div>
          <strong>Privacy & Research Standard:</strong> Small-cell counts below 5 are suppressed (&lt;5) across analytical breakdowns in compliance with <em>k</em>-anonymity privacy safeguards. All research exports exclude names, contact details, user IDs, and raw sensitive narratives.
        </div>
      </div>

      {/* KPI Top Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '1rem', marginBottom: '2.5rem' }}>
        <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #1E3A8A' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            <span>Total Reports</span>
            <ShieldAlert size={18} color="#1E3A8A" />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--text-main)' }}>
            {overview?.totalIncidents ?? 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Documented incident cases
          </div>
        </div>

        <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #DC2626' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            <span>Documented Loss</span>
            <IndianRupee size={18} color="#DC2626" />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: '#DC2626' }}>
            {formatCurrency(overview?.totalDocumentedFinancialLoss)}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Across all financial categories
          </div>
        </div>

        <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #0D9488' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            <span>Quiz Assessments</span>
            <GraduationCap size={18} color="#0D9488" />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--text-main)' }}>
            {overview?.totalAssessmentsCompleted ?? 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: '#16A34A', fontWeight: 600, marginTop: '0.25rem' }}>
            {overview?.overallAssessmentPassRate != null ? `${overview.overallAssessmentPassRate.toFixed(1)}% Pass Rate` : 'No attempts yet'}
          </div>
        </div>

        <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #7C3AED' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            <span>ML Diagnostic Rate</span>
            <Cpu size={18} color="#7C3AED" />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--text-main)' }}>
            {overview?.mlPredictionCoverageRate != null ? `${overview.mlPredictionCoverageRate.toFixed(1)}%` : '0%'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Advisory inference coverage
          </div>
        </div>

        <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #16A34A' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            <span>High Conf. Predictions</span>
            <TrendingUp size={18} color="#16A34A" />
          </div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, color: '#16A34A' }}>
            {mlDiagnostics?.highConfidencePredictions ?? 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Confidence score &ge; 80%
          </div>
        </div>
      </div>

      {/* Tabs Navigation */}
      <div style={{ display: 'flex', borderBottom: '2px solid var(--border-color)', marginBottom: '2rem', gap: '0.5rem', overflowX: 'auto' }}>
        <button
          onClick={() => setActiveTab('operational')}
          style={{
            padding: '0.75rem 1.25rem',
            border: 'none',
            background: 'none',
            borderBottom: activeTab === 'operational' ? '3px solid #1E3A8A' : '3px solid transparent',
            color: activeTab === 'operational' ? '#1E3A8A' : 'var(--text-muted)',
            fontWeight: activeTab === 'operational' ? 700 : 500,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontSize: '0.95rem'
          }}
        >
          <Layers size={18} />
          Operational & Incident Analytics
        </button>

        <button
          onClick={() => setActiveTab('educational')}
          style={{
            padding: '0.75rem 1.25rem',
            border: 'none',
            background: 'none',
            borderBottom: activeTab === 'educational' ? '3px solid #1E3A8A' : '3px solid transparent',
            color: activeTab === 'educational' ? '#1E3A8A' : 'var(--text-muted)',
            fontWeight: activeTab === 'educational' ? 700 : 500,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontSize: '0.95rem'
          }}
        >
          <GraduationCap size={18} />
          Awareness & Quiz Assessments
        </button>

        <button
          onClick={() => setActiveTab('ml')}
          style={{
            padding: '0.75rem 1.25rem',
            border: 'none',
            background: 'none',
            borderBottom: activeTab === 'ml' ? '3px solid #1E3A8A' : '3px solid transparent',
            color: activeTab === 'ml' ? '#1E3A8A' : 'var(--text-muted)',
            fontWeight: activeTab === 'ml' ? 700 : 500,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontSize: '0.95rem'
          }}
        >
          <Cpu size={18} />
          ML Telemetry & Diagnostics
        </button>

        <button
          onClick={() => setActiveTab('export')}
          style={{
            padding: '0.75rem 1.25rem',
            border: 'none',
            background: 'none',
            borderBottom: activeTab === 'export' ? '3px solid #1E3A8A' : '3px solid transparent',
            color: activeTab === 'export' ? '#1E3A8A' : 'var(--text-muted)',
            fontWeight: activeTab === 'export' ? 700 : 500,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontSize: '0.95rem'
          }}
        >
          <FileSpreadsheet size={18} />
          Research Data Export Center
        </button>
      </div>

      {/* TAB 1: OPERATIONAL & INCIDENT ANALYTICS */}
      {activeTab === 'operational' && (
        <div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
            {/* Category Breakdown */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-main)' }}>
                  Incidents by Category (8 Taxonomy Classes)
                </h3>
                <span style={{ fontSize: '0.75rem', backgroundColor: '#F3F4F6', color: '#4B5563', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>
                  k=5 Suppressed
                </span>
              </div>

              {categories.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No incident category data available.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {categories.map((cat) => {
                    const pct = cat.percentage ?? 0;
                    return (
                      <div key={cat.categoryCode}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.35rem' }}>
                          <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                            {cat.categoryName} <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 400 }}>({cat.categoryCode})</span>
                          </span>
                          <span style={{ fontWeight: 700, color: '#1E3A8A' }}>
                            {cat.displayCount} ({pct.toFixed(1)}%)
                          </span>
                        </div>
                        <div style={{ width: '100%', height: '8px', backgroundColor: '#F1F5F9', borderRadius: '4px', overflow: 'hidden' }}>
                          <div
                            style={{
                              width: `${Math.min(pct, 100)}%`,
                              height: '100%',
                              backgroundColor: cat.isMasked ? '#94A3B8' : '#1E3A8A',
                              borderRadius: '4px',
                              transition: 'width 0.4s ease'
                            }}
                          />
                        </div>
                        {cat.isMasked && (
                          <div style={{ fontSize: '0.7rem', color: '#64748B', marginTop: '0.2rem', fontStyle: 'italic' }}>
                            * Count masked to &lt;5 for privacy protection
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Delivery Channel Breakdown */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-main)' }}>
                  Incidents by Delivery Channel
                </h3>
                <span style={{ fontSize: '0.75rem', backgroundColor: '#F3F4F6', color: '#4B5563', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>
                  Digital Vectors
                </span>
              </div>

              {channels.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No channel data available.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {channels.map((ch) => {
                    const pct = ch.percentage ?? 0;
                    return (
                      <div key={ch.channelCode}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.35rem' }}>
                          <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                            {ch.channelDisplayName}
                          </span>
                          <span style={{ fontWeight: 700, color: '#0D9488' }}>
                            {ch.displayCount} ({pct.toFixed(1)}%)
                          </span>
                        </div>
                        <div style={{ width: '100%', height: '8px', backgroundColor: '#F1F5F9', borderRadius: '4px', overflow: 'hidden' }}>
                          <div
                            style={{
                              width: `${Math.min(pct, 100)}%`,
                              height: '100%',
                              backgroundColor: ch.isMasked ? '#94A3B8' : '#0D9488',
                              borderRadius: '4px',
                              transition: 'width 0.4s ease'
                            }}
                          />
                        </div>
                        {ch.isMasked && (
                          <div style={{ fontSize: '0.7rem', color: '#64748B', marginTop: '0.2rem', fontStyle: 'italic' }}>
                            * Count masked to &lt;5
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: EDUCATIONAL AWARENESS & ASSESSMENTS */}
      {activeTab === 'educational' && (
        <div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
            <div className="card" style={{ padding: '1.5rem', textAlign: 'center' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Total Quiz Attempts</div>
              <div style={{ fontSize: '2.25rem', fontWeight: 800, color: 'var(--primary-color)' }}>
                {assessments?.totalAttempts ?? 0}
              </div>
            </div>

            <div className="card" style={{ padding: '1.5rem', textAlign: 'center' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Successful Passes (&ge; 70%)</div>
              <div style={{ fontSize: '2.25rem', fontWeight: 800, color: '#16A34A' }}>
                {assessments?.passedAttempts ?? 0}
              </div>
              <div style={{ fontSize: '0.8rem', color: '#16A34A', fontWeight: 600 }}>
                {assessments?.overallPassRate != null ? `${assessments.overallPassRate.toFixed(1)}% Success Rate` : '0%'}
              </div>
            </div>

            <div className="card" style={{ padding: '1.5rem', textAlign: 'center' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Average Learner Score</div>
              <div style={{ fontSize: '2.25rem', fontWeight: 800, color: '#0D9488' }}>
                {assessments?.averageScorePercentage != null ? `${assessments.averageScorePercentage.toFixed(1)}%` : '0%'}
              </div>
            </div>
          </div>

          {/* Topic-by-Topic Performance Table */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-main)', marginBottom: '1.25rem' }}>
              Assessment Performance by Awareness Topic
            </h3>

            {(!assessments?.topicAggregates || assessments.topicAggregates.length === 0) ? (
              <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No quiz assessment data recorded yet.</p>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)' }}>
                      <th style={{ padding: '0.75rem' }}>Awareness Topic</th>
                      <th style={{ padding: '0.75rem', textAlign: 'center' }}>Total Attempts</th>
                      <th style={{ padding: '0.75rem', textAlign: 'center' }}>Passed</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right' }}>Pass Rate</th>
                      <th style={{ padding: '0.75rem', width: '200px' }}>Mastery Progress</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assessments.topicAggregates.map((topic) => {
                      const passRate = topic.passRate ?? 0;
                      const isHigh = passRate >= 70;
                      return (
                        <tr key={topic.topicId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                          <td style={{ padding: '0.75rem', fontWeight: 600 }}>{topic.topicTitle}</td>
                          <td style={{ padding: '0.75rem', textAlign: 'center' }}>{topic.totalAttempts}</td>
                          <td style={{ padding: '0.75rem', textAlign: 'center', color: '#16A34A', fontWeight: 600 }}>{topic.passedAttempts}</td>
                          <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 700, color: isHigh ? '#16A34A' : '#D97706' }}>
                            {passRate.toFixed(1)}%
                          </td>
                          <td style={{ padding: '0.75rem' }}>
                            <div style={{ width: '100%', height: '8px', backgroundColor: '#F1F5F9', borderRadius: '4px', overflow: 'hidden' }}>
                              <div
                                style={{
                                  width: `${Math.min(passRate, 100)}%`,
                                  height: '100%',
                                  backgroundColor: isHigh ? '#16A34A' : '#D97706',
                                  borderRadius: '4px'
                                }}
                              />
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 3: ML TELEMETRY & DIAGNOSTICS */}
      {activeTab === 'ml' && (
        <div>
          {/* Model Card Metadata */}
          <div className="card" style={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1rem' }}>
              <div>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#1E3A8A' }}>
                  Model Specification & Architecture
                </h3>
                <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                  Baseline Multi-Class Text Classifier for Cyber Incident Categorisation
                </div>
              </div>
              <span style={{ backgroundColor: '#DCFCE7', color: '#166534', padding: '0.35rem 0.75rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 700 }}>
                STATUS: {mlDiagnostics?.primaryModelStatus ?? 'ONLINE'}
              </span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', backgroundColor: '#F8FAFC', padding: '1rem', borderRadius: '8px', fontSize: '0.85rem' }}>
              <div>
                <div style={{ color: 'var(--text-muted)' }}>Active Model:</div>
                <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{mlDiagnostics?.modelName ?? 'Logistic Regression'}</div>
              </div>
              <div>
                <div style={{ color: 'var(--text-muted)' }}>Model Version:</div>
                <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{mlDiagnostics?.modelVersion ?? '1.0.0'}</div>
              </div>
              <div>
                <div style={{ color: 'var(--text-muted)' }}>Fallback Engine:</div>
                <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{mlDiagnostics?.fallbackEngine ?? 'Keyword Rule Matcher'}</div>
              </div>
              <div>
                <div style={{ color: 'var(--text-muted)' }}>Confidence Threshold:</div>
                <div style={{ fontWeight: 600, color: '#D97706' }}>60.0% (Routing to Manual Review)</div>
              </div>
            </div>
          </div>

          {/* Diagnostic Metrics Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Total Predictions Logged</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-main)' }}>
                {mlDiagnostics?.totalPredictions ?? 0}
              </div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Successful Live Inferences</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#16A34A' }}>
                {mlDiagnostics?.successfulInferences ?? 0}
              </div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Fallback Invocations</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#D97706' }}>
                {mlDiagnostics?.fallbackInferences ?? 0}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>During service downtime</div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Admin Reclassifications</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#7C3AED' }}>
                {mlDiagnostics?.reclassifiedCount ?? 0}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Manual category corrections</div>
            </div>
          </div>

          {/* Confidence Bands & Advisory Note */}
          <div className="card" style={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
            <h4 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-main)', marginBottom: '1rem' }}>
              Confidence Score Distribution Bands
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
              <div style={{ backgroundColor: '#F0FDF4', padding: '1rem', borderRadius: '6px', border: '1px solid #DCFCE7' }}>
                <div style={{ fontSize: '0.8rem', color: '#166534', fontWeight: 600 }}>High Confidence (&ge; 80%)</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#166534' }}>
                  {mlDiagnostics?.highConfidencePredictions ?? 0}
                </div>
              </div>

              <div style={{ backgroundColor: '#FFFBEB', padding: '1rem', borderRadius: '6px', border: '1px solid #FEF3C7' }}>
                <div style={{ fontSize: '0.8rem', color: '#92400E', fontWeight: 600 }}>Moderate Confidence (60% - 79%)</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#92400E' }}>
                  {mlDiagnostics?.moderateConfidencePredictions ?? 0}
                </div>
              </div>

              <div style={{ backgroundColor: '#FEF2F2', padding: '1rem', borderRadius: '6px', border: '1px solid #FEE2E2' }}>
                <div style={{ fontSize: '0.8rem', color: '#991B1B', fontWeight: 600 }}>Low Confidence (&lt; 60%)</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#991B1B' }}>
                  {mlDiagnostics?.lowConfidencePredictions ?? 0}
                </div>
              </div>
            </div>
          </div>

          {/* Advisory Notice */}
          <div style={{ backgroundColor: '#EFF6FF', border: '1px solid #BFDBFE', borderRadius: '8px', padding: '1rem', display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#1E40AF', fontSize: '0.875rem' }}>
            <HelpCircle size={20} style={{ flexShrink: 0 }} />
            <div>
              <strong>Academic Integrity Protocol (AGENTS.md Sec 6):</strong> Machine learning inferences are decision-support suggestions for human triage. They are never treated as judicial facts or definitive conclusions.
            </div>
          </div>
        </div>
      )}

      {/* TAB 4: RESEARCH DATA EXPORT CENTER */}
      {activeTab === 'export' && (
        <div>
          {exportSuccessMessage && (
            <div style={{ backgroundColor: '#DCFCE7', border: '1px solid #86EFAC', borderRadius: '8px', padding: '1rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#166534' }}>
              <CheckCircle size={20} />
              <span>{exportSuccessMessage}</span>
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
            {/* Export Card 1: Incidents */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
                <FileSpreadsheet size={24} color="#1E3A8A" />
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-main)' }}>
                  De-Identified Incidents Dataset (CSV)
                </h3>
              </div>

              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.25rem' }}>
                Export structured, privacy-cleansed incident reports for quantitative cybercrime research, temporal analysis, and classification benchmarking.
              </p>

              {/* Data Schema Box */}
              <div style={{ backgroundColor: '#F8FAFC', border: '1px solid var(--border-color)', borderRadius: '6px', padding: '0.85rem', marginBottom: '1.25rem', fontSize: '0.8rem' }}>
                <div style={{ fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.35rem' }}>Included Fields:</div>
                <code style={{ color: '#1E3A8A', display: 'block', wordBreak: 'break-all' }}>
                  Pseudo_ID, Delivery_Channel, Year_Month, Loss_Bracket, Category_Code, Review_Status, ML_Category, ML_Inference_Status, ML_Confidence_Band
                </code>
              </div>

              {/* Safety Badges */}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1.5rem' }}>
                <span style={{ backgroundColor: '#DCFCE7', color: '#166534', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                  ✓ Zero Direct PII
                </span>
                <span style={{ backgroundColor: '#DCFCE7', color: '#166534', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                  ✓ Coarse Loss Brackets
                </span>
                <span style={{ backgroundColor: '#DCFCE7', color: '#166534', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                  ✓ OWASP Formula Defended
                </span>
              </div>

              <button
                onClick={handleExportIncidents}
                disabled={exportingIncidents}
                className="btn btn-primary"
                style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem' }}
              >
                <Download size={16} />
                {exportingIncidents ? 'Generating Clean Dataset...' : 'Download Incidents CSV'}
              </button>
            </div>

            {/* Export Card 2: Quiz Assessments */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
                <FileSpreadsheet size={24} color="#0D9488" />
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-main)' }}>
                  De-Identified Quiz Assessments Dataset (CSV)
                </h3>
              </div>

              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.25rem' }}>
                Export aggregate learner knowledge checks, mastery pass rates, and topic effectiveness data for educational impact studies.
              </p>

              {/* Data Schema Box */}
              <div style={{ backgroundColor: '#F8FAFC', border: '1px solid var(--border-color)', borderRadius: '6px', padding: '0.85rem', marginBottom: '1.25rem', fontSize: '0.8rem' }}>
                <div style={{ fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.35rem' }}>Included Fields:</div>
                <code style={{ color: '#0D9488', display: 'block', wordBreak: 'break-all' }}>
                  Pseudo_Attempt_ID, Topic_Title, Attempt_YearMonth, Score_Percentage, Passed
                </code>
              </div>

              {/* Safety Badges */}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1.5rem' }}>
                <span style={{ backgroundColor: '#DCFCE7', color: '#166534', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                  ✓ Anonymous Pseudo-IDs
                </span>
                <span style={{ backgroundColor: '#DCFCE7', color: '#166534', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                  ✓ Aggregated Timestamps
                </span>
                <span style={{ backgroundColor: '#DCFCE7', color: '#166534', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}>
                  ✓ OWASP Formula Defended
                </span>
              </div>

              <button
                onClick={handleExportAssessments}
                disabled={exportingAssessments}
                className="btn btn-secondary"
                style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem' }}
              >
                <Download size={16} />
                {exportingAssessments ? 'Generating Clean Dataset...' : 'Download Assessments CSV'}
              </button>
            </div>
          </div>

          {/* Security & Audit Commitment Box */}
          <div style={{ backgroundColor: '#F8FAFC', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '1.25rem', display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
            <Lock size={20} color="#1E3A8A" style={{ marginTop: '0.15rem' }} />
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              <strong style={{ color: 'var(--text-main)' }}>Immutable Audit Trail Logging:</strong> Every data export action is recorded with your administrator identity, export timestamp, file type, and record count in the audit ledger. In accordance with the project data management plan, exported files must be stored securely and used exclusively for academic research.
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
