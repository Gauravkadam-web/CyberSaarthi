import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldAlert, AlertTriangle, CheckCircle, Info, ExternalLink, ArrowRight, Copy } from 'lucide-react';
import { reportsApi } from '../api/reportsApi';
import { awarenessApi } from '../api/awarenessApi';
import { useAuth } from '../context/AuthContext';

const CHANNELS = [
  { value: 'UPI_APP', label: 'UPI / Payment App (Google Pay, PhonePe, Paytm, etc.)' },
  { value: 'WHATSAPP', label: 'WhatsApp / Instant Messaging' },
  { value: 'PHONE_CALL', label: 'Phone Call / Voice Call Impersonation' },
  { value: 'SMS', label: 'SMS / Text Message (Smishing)' },
  { value: 'EMAIL', label: 'Email Phishing / Malicious Attachment' },
  { value: 'WEBSITE_PORTAL', label: 'Fake Website / Investment Portal' },
  { value: 'SOCIAL_MEDIA', label: 'Social Media (Instagram, Facebook, Telegram, etc.)' },
  { value: 'OTHER', label: 'Other Digital Channel' }
];

export default function ReportSubmissionPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    incidentChannel: 'UPI_APP',
    incidentDate: new Date().toISOString().split('T')[0],
    financialLossAmount: '',
    userSelectedCategoryId: '',
    description: '',
    disclaimerAcknowledged: false
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [submittedReport, setSubmittedReport] = useState(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await awarenessApi.getCategories();
        if (res.success && res.data) {
          setCategories(res.data);
        }
      } catch (err) {
        console.error('Failed to load categories', err);
      }
    };
    fetchCategories();
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!isAuthenticated) {
      navigate('/login?redirect=/reports/new');
      return;
    }

    if (!formData.disclaimerAcknowledged) {
      setError('You must acknowledge that CyberSaarthi is an educational support portal and not a police or emergency authority.');
      return;
    }

    // Client-side warning check
    const cardMatch = /\b(?:\d[ -]*?){13,16}\b/.test(formData.description);
    const pinMatch = /(atm\s*pin|cvv\s*code|card\s*cvv|netbanking\s*password|login\s*password)/i.test(formData.description);
    if (cardMatch || pinMatch) {
      setError('Data Minimisation Alert: Do not include full payment card numbers, CVVs, PINs, or banking passwords in the incident description.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const payload = {
        incidentChannel: formData.incidentChannel,
        incidentDate: formData.incidentDate,
        description: formData.description.trim(),
        financialLossAmount: formData.financialLossAmount ? parseFloat(formData.financialLossAmount) : null,
        userSelectedCategoryId: formData.userSelectedCategoryId || null,
        disclaimerAcknowledged: formData.disclaimerAcknowledged
      };

      const res = await reportsApi.submitReport(payload);
      if (res.success && res.data) {
        setSubmittedReport(res.data);
      } else {
        setError(res.message || 'Failed to submit incident report');
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'An error occurred while submitting the report.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const copyRefCode = () => {
    if (submittedReport?.referenceCode) {
      navigator.clipboard.writeText(submittedReport.referenceCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  if (submittedReport) {
    return (
      <div className="container" style={{ padding: '3rem 1rem', maxWidth: '720px' }}>
        <div className="card" style={{ textAlign: 'center', padding: '2.5rem 2rem', borderTop: '4px solid #16A34A' }}>
          <div style={{ display: 'inline-flex', padding: '1rem', background: '#DCFCE7', borderRadius: '50%', marginBottom: '1.25rem' }}>
            <CheckCircle size={48} color="#16A34A" />
          </div>

          <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.5rem', color: 'var(--text-main)' }}>
            Incident Report Documented
          </h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '2rem', fontSize: '1rem' }}>
            Your incident has been securely logged with CyberSaarthi for educational review and tracking.
          </p>

          <div style={{ background: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: '12px', padding: '1.5rem', marginBottom: '2rem' }}>
            <span style={{ fontSize: '0.825rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: '#64748B', fontWeight: 700 }}>
              Your Unique Tracking Code
            </span>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem', marginTop: '0.5rem' }}>
              <span style={{ fontSize: '2rem', fontWeight: 900, fontFamily: 'monospace', color: '#1E3A8A', letterSpacing: '0.08em' }}>
                {submittedReport.referenceCode}
              </span>
              <button
                type="button"
                onClick={copyRefCode}
                className="btn btn-secondary"
                style={{ padding: '0.4rem 0.75rem', fontSize: '0.85rem' }}
                title="Copy reference code"
              >
                <Copy size={16} />
                {copied ? 'Copied!' : 'Copy'}
              </button>
            </div>
            <p style={{ fontSize: '0.825rem', color: '#64748B', marginTop: '0.5rem' }}>
              Save this reference code to track review updates or retrieve your incident documentation anytime.
            </p>
          </div>

          {/* Official Police Notice */}
          <div style={{ background: '#FEF2F2', border: '1px solid #FCA5A5', borderRadius: '8px', padding: '1.25rem', textAlign: 'left', marginBottom: '2rem' }}>
            <h4 style={{ color: '#991B1B', fontWeight: 700, fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
              <AlertTriangle size={18} />
              Important Notice Regarding Official Complaint Filing
            </h4>
            <p style={{ fontSize: '0.875rem', color: '#7F1D1D', lineHeight: 1.5, margin: 0 }}>
              CyberSaarthi is an academic awareness prototype and does <strong>not</strong> file an official police FIR or emergency complaint.
              If you lost money or are facing active cyber extortion, immediately dial <strong>1930</strong> (National Cyber Financial Fraud Helpline)
              and register an official complaint at{' '}
              <a href="https://cybercrime.gov.in" target="_blank" rel="noopener noreferrer" style={{ color: '#B91C1C', fontWeight: 700, textDecoration: 'underline' }}>
                cybercrime.gov.in <ExternalLink size={12} style={{ display: 'inline' }} />
              </a>.
            </p>
          </div>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <Link to={`/reports/${submittedReport.id}`} className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
              View Report Details <ArrowRight size={16} />
            </Link>
            <Link to="/reports" className="btn btn-secondary">
              My Reports
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2.5rem 1rem', maxWidth: '820px' }}>
      {/* Educational Notice Banner */}
      <div style={{ background: '#EFF6FF', border: '1px solid #BFDBFE', borderRadius: '12px', padding: '1.25rem', marginBottom: '2rem', display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
        <ShieldAlert size={26} color="#1D4ED8" style={{ flexShrink: 0, marginTop: '2px' }} />
        <div>
          <h4 style={{ color: '#1E40AF', fontWeight: 700, fontSize: '1rem', marginBottom: '0.25rem' }}>
            Documenting an Incident with CyberSaarthi
          </h4>
          <p style={{ color: '#1E3A8A', fontSize: '0.875rem', lineHeight: 1.5, margin: 0 }}>
            This portal helps citizens structure, categorise, and analyse cyber incidents for educational awareness.
            Submitting here <strong>does not replace an official police complaint</strong>. For emergency financial cyber fraud, immediately call <strong>1930</strong>.
          </p>
        </div>
      </div>

      <div className="card">
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.5rem', color: 'var(--text-main)' }}>
          Report a Cyber Incident
        </h1>
        <p style={{ color: 'var(--text-muted)', marginBottom: '1.75rem', fontSize: '0.95rem' }}>
          Provide factual details of the cyber encounter. Please do not submit bank credentials, PINs, or card CVVs.
        </p>

        {error && (
          <div style={{ background: '#FEF2F2', border: '1px solid #FCA5A5', color: '#991B1B', padding: '0.85rem 1rem', borderRadius: '8px', marginBottom: '1.5rem', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <AlertTriangle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem', marginBottom: '1.25rem' }}>
            {/* Channel Selection */}
            <div>
              <label htmlFor="incidentChannel" style={{ display: 'block', fontWeight: 600, fontSize: '0.875rem', marginBottom: '0.4rem', color: 'var(--text-main)' }}>
                Incident Channel *
              </label>
              <select
                id="incidentChannel"
                name="incidentChannel"
                value={formData.incidentChannel}
                onChange={handleChange}
                required
                style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '8px', border: '1px solid #CBD5E1', background: '#FFF', fontSize: '0.9rem' }}
              >
                {CHANNELS.map(ch => (
                  <option key={ch.value} value={ch.value}>{ch.label}</option>
                ))}
              </select>
            </div>

            {/* Incident Date */}
            <div>
              <label htmlFor="incidentDate" style={{ display: 'block', fontWeight: 600, fontSize: '0.875rem', marginBottom: '0.4rem', color: 'var(--text-main)' }}>
                Date of Incident *
              </label>
              <input
                type="date"
                id="incidentDate"
                name="incidentDate"
                max={new Date().toISOString().split('T')[0]}
                value={formData.incidentDate}
                onChange={handleChange}
                required
                style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '0.9rem' }}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem', marginBottom: '1.25rem' }}>
            {/* Financial Loss */}
            <div>
              <label htmlFor="financialLossAmount" style={{ display: 'block', fontWeight: 600, fontSize: '0.875rem', marginBottom: '0.4rem', color: 'var(--text-main)' }}>
                Financial Loss in INR (if any)
              </label>
              <input
                type="number"
                id="financialLossAmount"
                name="financialLossAmount"
                step="0.01"
                min="0"
                placeholder="e.g. 5000.00"
                value={formData.financialLossAmount}
                onChange={handleChange}
                style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '0.9rem' }}
              />
            </div>

            {/* Category */}
            <div>
              <label htmlFor="userSelectedCategoryId" style={{ display: 'block', fontWeight: 600, fontSize: '0.875rem', marginBottom: '0.4rem', color: 'var(--text-main)' }}>
                Suspected Category (Optional)
              </label>
              <select
                id="userSelectedCategoryId"
                name="userSelectedCategoryId"
                value={formData.userSelectedCategoryId}
                onChange={handleChange}
                style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '8px', border: '1px solid #CBD5E1', background: '#FFF', fontSize: '0.9rem' }}
              >
                <option value="">-- I am unsure / Select category --</option>
                {categories.map(cat => (
                  <option key={cat.id} value={cat.id}>{cat.displayName}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Description */}
          <div style={{ marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
              <label htmlFor="description" style={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--text-main)' }}>
                Incident Narrative / Description *
              </label>
              <span style={{ fontSize: '0.8rem', color: formData.description.length < 20 ? '#EF4444' : '#64748B' }}>
                {formData.description.length} / 5000 characters (min 20)
              </span>
            </div>
            <textarea
              id="description"
              name="description"
              rows={6}
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe what occurred: what message or call was received, what action was requested, any fake domain names or phone numbers involved. Remember: NEVER share bank passwords, PINs, or CVV codes."
              required
              minLength={20}
              maxLength={5000}
              style={{ width: '100%', padding: '0.85rem', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '0.9rem', lineHeight: 1.5, fontFamily: 'inherit' }}
            />
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.35rem', fontSize: '0.8rem', color: '#64748B' }}>
              <Info size={14} />
              <span>Data Minimisation Guard active: card numbers and sensitive credentials will be automatically rejected.</span>
            </div>
          </div>

          {/* Disclaimer Checkbox */}
          <div style={{ background: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: '8px', padding: '1rem', marginBottom: '1.75rem' }}>
            <label style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', cursor: 'pointer', margin: 0 }}>
              <input
                type="checkbox"
                name="disclaimerAcknowledged"
                checked={formData.disclaimerAcknowledged}
                onChange={handleChange}
                required
                style={{ marginTop: '3px', width: '18px', height: '18px' }}
              />
              <span style={{ fontSize: '0.875rem', color: 'var(--text-main)', lineHeight: 1.45 }}>
                I acknowledge that <strong>CyberSaarthi is an academic educational portal</strong> and submitting this report does <strong>NOT</strong> constitute filing an official police FIR or legal complaint. For financial emergencies, I must call <strong>1930</strong> or report on <strong>cybercrime.gov.in</strong>.
              </span>
            </label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', alignItems: 'center' }}>
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="btn btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || formData.description.length < 20 || !formData.disclaimerAcknowledged}
              className="btn btn-primary"
              style={{ padding: '0.65rem 1.5rem', fontWeight: 600 }}
            >
              {loading ? 'Submitting...' : 'Submit Incident Report'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
