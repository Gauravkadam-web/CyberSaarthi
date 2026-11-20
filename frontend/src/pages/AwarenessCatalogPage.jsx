import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Award, BookOpen } from 'lucide-react';
import { awarenessApi } from '../api/awarenessApi';
import CategoryBadge from '../components/CategoryBadge';

export default function AwarenessCatalogPage() {
  const [topics, setTopics] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    awarenessApi.getCategories()
      .then((res) => {
        if (res.success) setCategories(res.data);
      })
      .catch(console.error);
  }, []);

  useEffect(() => {
    setLoading(true);
    awarenessApi.getTopics(selectedCategory, searchQuery)
      .then((res) => {
        if (res.success) setTopics(res.data);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [selectedCategory, searchQuery]);

  return (
    <div className="container page-wrapper">
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontFamily: 'var(--font-heading)', fontSize: '2.25rem', fontWeight: 800, marginBottom: '0.5rem' }}>
          Cybercrime Awareness Hub
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem' }}>
          Explore research-backed educational guides categorized by attack vector to recognise red flags and safeguard digital accounts.
        </p>
      </header>

      {/* Filter and Search Bar */}
      <div className="filter-bar">
        <div style={{ position: 'relative', flex: '1', minWidth: '280px' }}>
          <input
            type="text"
            className="search-input"
            placeholder="Search guides: e.g., 'UPI', 'QR Code', 'Phishing'..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="category-pills">
          <button
            className={`pill-btn ${selectedCategory === '' ? 'active' : ''}`}
            onClick={() => setSelectedCategory('')}
          >
            All Categories
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              className={`pill-btn ${selectedCategory === cat.code ? 'active' : ''}`}
              onClick={() => setSelectedCategory(cat.code)}
            >
              {cat.displayName}
            </button>
          ))}
        </div>
      </div>

      {/* Topics Grid */}
      {loading ? (
        <p>Loading threat guides...</p>
      ) : topics.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '3rem', background: '#FFFFFF', borderRadius: '0.75rem', border: '1px solid var(--border-color)' }}>
          <BookOpen size={40} color="#94A3B8" style={{ marginBottom: '1rem' }} />
          <h3>No awareness topics found matching your criteria.</h3>
          <p style={{ color: 'var(--text-muted)' }}>Try broadening your search keyword or clearing the category filter.</p>
        </div>
      ) : (
        <div className="card-grid">
          {topics.map((topic) => (
            <article key={topic.id} className="topic-card">
              <CategoryBadge categoryCode={topic.topicCategory} />
              <h2 className="topic-title">{topic.title}</h2>
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
    </div>
  );
}
