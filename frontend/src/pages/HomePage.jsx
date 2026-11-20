import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Shield, BookOpen, AlertOctagon, Award, ArrowRight } from 'lucide-react';
import { awarenessApi } from '../api/awarenessApi';
import CategoryBadge from '../components/CategoryBadge';

export default function HomePage() {
  const [featuredTopics, setFeaturedTopics] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    awarenessApi.getTopics()
      .then((res) => {
        if (res.success && res.data) {
          setFeaturedTopics(res.data.slice(0, 3));
        }
      })
      .catch((err) => console.error('Failed to load topics', err))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="container page-wrapper">
      <section className="hero-section">
        <h1 className="hero-title">Empowering You Against Digital Threats</h1>
        <p className="hero-subtitle">
          CyberSaarthi provides verified cyber safety guides, interactive awareness assessments, and guided reporting assistance to help citizens navigate online security with confidence.
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link to="/awareness" className="btn btn-primary" style={{ backgroundColor: '#0D9488' }}>
            <BookOpen size={18} />
            Explore Threat Guides
          </Link>
          <a href="https://cybercrime.gov.in" target="_blank" rel="noopener noreferrer" className="btn btn-secondary">
            <AlertOctagon size={18} />
            Official Portal (1930)
          </a>
        </div>
      </section>

      {/* Featured Awareness Modules */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div>
            <h2 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.75rem', fontWeight: 800 }}>
              Essential Threat Prevention Guides
            </h2>
            <p style={{ color: 'var(--text-muted)' }}>Learn how modern cyber scams operate and how to protect your assets.</p>
          </div>
          <Link to="/awareness" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', fontWeight: 600 }}>
            View All <ArrowRight size={16} />
          </Link>
        </div>

        {loading ? (
          <p>Loading guides...</p>
        ) : (
          <div className="card-grid">
            {featuredTopics.map((topic) => (
              <article key={topic.id} className="topic-card">
                <CategoryBadge categoryCode={topic.topicCategory} />
                <h3 className="topic-title">{topic.title}</h3>
                <p className="topic-summary">{topic.summary}</p>
                <div style={{ marginTop: 'auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Link to={`/awareness/${topic.slug}`} className="btn btn-primary" style={{ padding: '0.45rem 1rem', fontSize: '0.85rem' }}>
                    Read Guide
                  </Link>
                  <Link to={`/quizzes/${topic.id}`} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.85rem', fontWeight: 600 }}>
                    <Award size={15} /> Take Quiz
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
