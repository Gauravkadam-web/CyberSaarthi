import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Award, CheckCircle2, XCircle, ArrowLeft, RefreshCw, LogIn } from 'lucide-react';
import { quizApi } from '../api/quizApi';
import { useAuth } from '../context/AuthContext';

export default function QuizPage() {
  const { topicId } = useParams();
  const { isAuthenticated } = useAuth();

  const [quiz, setQuiz] = useState(null);
  const [selectedAnswers, setSelectedAnswers] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    quizApi.getQuizForTopic(topicId)
      .then((res) => {
        if (res.success && res.data) {
          setQuiz(res.data);
        } else {
          setError('Quiz not found for this topic');
        }
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load quiz');
      })
      .finally(() => setLoading(false));
  }, [topicId]);

  const handleSelectOption = (questionId, optionKey) => {
    if (result) return; // Locked once submitted
    setSelectedAnswers((prev) => ({
      ...prev,
      [questionId]: optionKey
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      alert('Please log in first to submit your quiz attempt and record your score.');
      return;
    }

    const answersPayload = Object.entries(selectedAnswers).map(([qId, optKey]) => ({
      questionId: qId,
      selectedOptionKey: optKey
    }));

    if (answersPayload.length < quiz.questions.length) {
      alert('Please answer all questions before submitting.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await quizApi.submitAttempt(topicId, answersPayload);
      if (res.success && res.data) {
        setResult(res.data);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit quiz attempt');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRetake = () => {
    setSelectedAnswers({});
    setResult(null);
  };

  if (loading) {
    return <div className="container page-wrapper"><p>Loading knowledge quiz...</p></div>;
  }

  if (error || !quiz) {
    return (
      <div className="container page-wrapper">
        <p style={{ color: 'var(--danger-color)', marginBottom: '1rem' }}>{error || 'Quiz not available'}</p>
        <Link to="/awareness" className="btn btn-secondary"><ArrowLeft size={16} /> Back to Catalog</Link>
      </div>
    );
  }

  return (
    <div className="container page-wrapper" style={{ maxWidth: '820px' }}>
      <Link to="/awareness" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', marginBottom: '1.5rem', fontWeight: 600 }}>
        <ArrowLeft size={16} /> Back to Catalog
      </Link>

      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontFamily: 'var(--font-heading)', fontSize: '2rem', fontWeight: 800 }}>
          {quiz.topicTitle} — Knowledge Assessment
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Read each scenario carefully and select the safest course of action.
        </p>
      </header>

      {!isAuthenticated && !result && (
        <div style={{ background: '#FEF3C7', border: '1px solid #F59E0B', borderRadius: '0.5rem', padding: '1rem 1.25rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ color: '#92400E', fontSize: '0.9rem' }}>
            <strong>Note:</strong> You can practice questions freely, but you must log in to submit and track your score history.
          </span>
          <Link to="/login" className="btn btn-primary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.825rem', backgroundColor: '#B45309' }}>
            <LogIn size={14} /> Login
          </Link>
        </div>
      )}

      {/* Result Scorecard Banner */}
      {result && (
        <div className="scorecard" role="region" aria-label="Quiz Results">
          <Award size={48} color="#38BDF8" style={{ margin: '0 auto 1rem' }} />
          <h2>Assessment Complete!</h2>
          <div className="score-number">
            {result.score} / {result.maxScore}
          </div>
          <p style={{ fontSize: '1.15rem', color: '#BAE6FD', marginBottom: '1.5rem' }}>
            Overall Score: <strong>{result.percentage}%</strong> — {result.percentage >= 80 ? 'Outstanding Vigilance!' : 'Review the explanations below to strengthen your digital defenses.'}
          </p>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <button onClick={handleRetake} className="btn btn-secondary" style={{ backgroundColor: '#FFFFFF', color: '#0F172A' }}>
              <RefreshCw size={16} /> Retake Quiz
            </button>
            <Link to="/my-attempts" className="btn btn-primary">
              View My History
            </Link>
          </div>
        </div>
      )}

      {/* Questions Form */}
      <form onSubmit={handleSubmit}>
        {quiz.questions.map((q, idx) => {
          const feedback = result?.feedback?.find((f) => f.questionId === q.id);

          return (
            <fieldset key={q.id} className="quiz-card" style={{ border: 'none' }}>
              <legend className="quiz-question-title">
                {idx + 1}. {q.questionText}
              </legend>

              <div style={{ display: 'flex', flexDirection: 'column' }}>
                {q.options.map((opt) => {
                  const isSelected = selectedAnswers[q.id] === opt.optionKey;
                  let optionClass = 'quiz-option';
                  if (isSelected) optionClass += ' selected';

                  return (
                    <label
                      key={opt.optionKey}
                      className={optionClass}
                      onClick={() => handleSelectOption(q.id, opt.optionKey)}
                    >
                      <input
                        type="radio"
                        name={`q_${q.id}`}
                        value={opt.optionKey}
                        checked={isSelected}
                        onChange={() => handleSelectOption(q.id, opt.optionKey)}
                        disabled={!!result}
                        style={{ marginTop: '0.25rem' }}
                      />
                      <span>
                        <strong>{opt.optionKey}.</strong> {opt.optionText}
                      </span>
                    </label>
                  );
                })}
              </div>

              {/* Post-Submission Explanatory Feedback */}
              {feedback && (
                <div className={feedback.isCorrect ? 'feedback-correct' : 'feedback-incorrect'} style={{ marginTop: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                    {feedback.isCorrect ? (
                      <CheckCircle2 size={18} color="var(--success-color)" />
                    ) : (
                      <XCircle size={18} color="var(--danger-color)" />
                    )}
                    <strong style={{ color: feedback.isCorrect ? 'var(--success-color)' : 'var(--danger-color)' }}>
                      {feedback.isCorrect ? 'Correct Answer!' : `Incorrect (Correct was ${feedback.correctOptionKey})`}
                    </strong>
                  </div>
                  <p style={{ fontSize: '0.925rem', color: '#334155' }}>
                    {feedback.explanation}
                  </p>
                </div>
              )}
            </fieldset>
          );
        })}

        {!result && (
          <div style={{ textAlign: 'center', marginTop: '2rem' }}>
            <button
              type="submit"
              disabled={submitting || !isAuthenticated}
              className="btn btn-primary"
              style={{ padding: '0.85rem 2.5rem', fontSize: '1.05rem', opacity: !isAuthenticated ? 0.6 : 1 }}
            >
              {submitting ? 'Evaluating...' : 'Submit Assessment'}
            </button>
          </div>
        )}
      </form>
    </div>
  );
}
