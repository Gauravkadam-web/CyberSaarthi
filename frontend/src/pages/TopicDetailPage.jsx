import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Award, ArrowLeft, Calendar } from 'lucide-react';
import { awarenessApi } from '../api/awarenessApi';
import CategoryBadge from '../components/CategoryBadge';

export default function TopicDetailPage() {
  const { slug } = useParams();
  const [topic, setTopic] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    awarenessApi.getTopicBySlug(slug)
      .then((res) => {
        if (res.success && res.data) {
          setTopic(res.data);
        } else {
          setError('Topic not found');
        }
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load topic details');
      })
      .finally(() => setLoading(false));
  }, [slug]);

  if (loading) {
    return <div className="container page-wrapper"><p>Loading educational guide...</p></div>;
  }

  if (error || !topic) {
    return (
      <div className="container page-wrapper">
        <p style={{ color: 'var(--danger-color)', marginBottom: '1rem' }}>{error || 'Topic not found'}</p>
        <Link to="/awareness" className="btn btn-secondary"><ArrowLeft size={16} /> Back to Catalog</Link>
      </div>
    );
  }

  return (
    <div className="container page-wrapper" style={{ maxWidth: '860px' }}>
      <Link to="/awareness" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', marginBottom: '1.5rem', fontWeight: 600 }}>
        <ArrowLeft size={16} /> Back to Catalog
      </Link>

      <article style={{ background: '#FFFFFF', padding: '2.5rem', borderRadius: '1rem', border: '1px solid var(--border-color)', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)' }}>
        <header style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '1.5rem', marginBottom: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
            <CategoryBadge categoryCode={topic.topicCategory} />
            {topic.publishedAt && (
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <Calendar size={14} />
                {new Date(topic.publishedAt).toLocaleDateString()}
              </span>
            )}
          </div>
          <h1 style={{ fontFamily: 'var(--font-heading)', fontSize: '2.25rem', fontWeight: 800, color: 'var(--text-main)', lineHeight: 1.3 }}>
            {topic.title}
          </h1>
          <p style={{ fontSize: '1.1rem', color: 'var(--text-muted)', marginTop: '0.75rem' }}>
            {topic.summary}
          </p>
        </header>

        {/* Formatted Guide Body */}
        <div style={{ fontSize: '1.05rem', lineHeight: 1.8, color: '#334155', whiteSpace: 'pre-line' }}>
          {topic.bodyMarkdown}
        </div>

        {/* Assessment CTA */}
        {topic.hasQuiz && (
          <div style={{ marginTop: '3rem', padding: '2rem', background: '#F0FDFA', border: '1.5px solid #0D9488', borderRadius: '0.75rem', textAlign: 'center' }}>
            <Award size={36} color="#0D9488" style={{ margin: '0 auto 0.75rem' }} />
            <h3 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.35rem', fontWeight: 700, color: '#134E4A' }}>
              Test What You've Learned!
            </h3>
            <p style={{ color: '#0F766E', marginBottom: '1.25rem', fontSize: '0.95rem' }}>
              Take our quick, interactive scenario quiz to test your vigilance against this threat vector.
            </p>
            <Link to={`/quizzes/${topic.id}`} className="btn btn-primary" style={{ backgroundColor: '#0D9488' }}>
              Start Scenario Quiz
            </Link>
          </div>
        )}
      </article>
    </div>
  );
}
