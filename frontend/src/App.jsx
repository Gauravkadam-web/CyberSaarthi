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

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
          <EmergencyBanner />
          <Navbar />
          <main style={{ flex: 1 }}>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/awareness" element={<AwarenessCatalogPage />} />
              <Route path="/awareness/:slug" element={<TopicDetailPage />} />
              <Route path="/quizzes/:topicId" element={<QuizPage />} />
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
