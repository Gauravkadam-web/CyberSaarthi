import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Shield, BookOpen, Award, FileText, ShieldAlert, ShieldCheck, User, LogOut, LogIn, BarChart3 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar" role="navigation" aria-label="Main Navigation">
      <div className="container nav-container">
        <Link to="/" className="nav-brand">
          <Shield size={28} color="#1E3A8A" />
          <span>CyberSaarthi</span>
        </Link>

        <ul className="nav-links">
          <li>
            <NavLink to="/awareness" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <BookOpen size={16} style={{ display: 'inline', marginRight: '4px' }} />
              Awareness Hub
            </NavLink>
          </li>

          <li>
            <NavLink to="/reports/new" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} style={{ color: '#DC2626', fontWeight: 600 }}>
              <ShieldAlert size={16} style={{ display: 'inline', marginRight: '4px' }} />
              Report Incident
            </NavLink>
          </li>

          {isAuthenticated && (
            <>
              <li>
                <NavLink to="/reports" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <FileText size={16} style={{ display: 'inline', marginRight: '4px' }} />
                  My Reports
                </NavLink>
              </li>

              <li>
                <NavLink to="/my-attempts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <Award size={16} style={{ display: 'inline', marginRight: '4px' }} />
                  My Assessments
                </NavLink>
              </li>
            </>
          )}

          {isAdmin && (
            <>
              <li>
                <NavLink to="/admin/reports" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} style={{ color: '#1E3A8A', fontWeight: 700 }}>
                  <ShieldCheck size={16} style={{ display: 'inline', marginRight: '4px' }} />
                  Review Queue
                </NavLink>
              </li>
              <li>
                <NavLink to="/admin/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} style={{ color: '#0D9488', fontWeight: 700 }}>
                  <BarChart3 size={16} style={{ display: 'inline', marginRight: '4px' }} />
                  Analytics
                </NavLink>
              </li>
            </>
          )}

          {isAuthenticated ? (
            <li style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                <User size={16} />
                {user?.displayName}
              </span>
              <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.825rem' }}>
                <LogOut size={14} />
                Logout
              </button>
            </li>
          ) : (
            <li>
              <Link to="/login" className="nav-auth-btn">
                <LogIn size={16} />
                Login / Register
              </Link>
            </li>
          )}
        </ul>
      </div>
    </nav>
  );
}
