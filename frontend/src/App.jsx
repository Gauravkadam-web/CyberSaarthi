import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import EmergencyBanner from './components/EmergencyBanner';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import HomePage from './pages/HomePage';
import AwarenessCatalogPage from './pages/AwarenessCatalogPage';
import TopicDetailPage from './pages/TopicDetailPage';
import QuizPage from './pages/QuizPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import MyAttemptsPage from './pages/MyAttemptsPage';
import ReportSubmissionPage from './pages/ReportSubmissionPage';
import MyReportsPage from './pages/MyReportsPage';
import ReportDetailPage from './pages/ReportDetailPage';
import AdminReviewQueuePage from './pages/AdminReviewQueuePage';
import AdminDashboardPage from './pages/AdminDashboardPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
          <a href="#main-content" className="skip-link">
            Skip to main content
          </a>
          <EmergencyBanner />
          <Navbar />
          <main id="main-content" style={{ flex: 1 }} tabIndex={-1}>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/awareness" element={<AwarenessCatalogPage />} />
              <Route path="/awareness/:slug" element={<TopicDetailPage />} />
              <Route path="/quizzes/:topicId" element={<QuizPage />} />
              <Route path="/reports/new" element={<ReportSubmissionPage />} />
              <Route path="/reports" element={<MyReportsPage />} />
              <Route path="/reports/:id" element={<ReportDetailPage />} />
              <Route path="/admin/reports" element={<AdminReviewQueuePage />} />
              <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/my-attempts" element={<MyAttemptsPage />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
