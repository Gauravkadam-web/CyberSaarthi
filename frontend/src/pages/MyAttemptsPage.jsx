import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Award, ArrowRight, BookOpen } from 'lucide-react';
import { quizApi } from '../api/quizApi';

export default function MyAttemptsPage() {
  const [attempts, setAttempts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    quizApi.getMyAttempts()
      .then((res) => {
        if (res.success && res.data) {
          setAttempts(res.data);
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="container page-wrapper">
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontFamily: 'var(--font-heading)', fontSize: '2.25rem', fontWeight: 800 }}>
          My Assessment History
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Review your completed awareness quizzes, scores, and mastery of threat defense concepts.
        </p>
      </header>

      {loading ? (
        <p>Loading assessment history...</p>
      ) : attempts.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '3.5rem', background: '#FFFFFF', borderRadius: '1rem', border: '1px solid var(--border-color)' }}>
          <Award size={48} color="#94A3B8" style={{ margin: '0 auto 1rem' }} />
          <h3>No Quizzes Taken Yet</h3>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
            Explore our threat guides and test your knowledge against real-world scam tactics.
          </p>
          <Link to="/awareness" className="btn btn-primary">
            <BookOpen size={18} />
            Explore Awareness Hub
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {attempts.map((att) => {
            const isPassing = att.percentage >= 70;
            return (
              <div
                key={att.attemptId}
                style={{
                  background: '#FFFFFF',
                  border: '1px solid var(--border-color)',
                  borderRadius: '0.75rem',
                  padding: '1.5rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: '1rem'
                }}
              >
                <div>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.25rem' }}>
                    {att.topicTitle}
                  </h3>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    Completed on {new Date(att.submittedAt).toLocaleDateString()} at {new Date(att.submittedAt).toLocaleTimeString()}
                  </span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '1.5rem', fontWeight: 800, color: isPassing ? 'var(--success-color)' : 'var(--warning-color)' }}>
                      {att.score} / {att.maxScore}
                    </div>
                    <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                      {att.percentage}% Score
                    </span>
                  </div>

                  <Link
                    to={`/quizzes/${att.topicId}`}
                    className="btn btn-secondary"
                    style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                  >
                    Retake <ArrowRight size={14} />
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
