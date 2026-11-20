import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('cs_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('cs_token');
      if (storedToken) {
        try {
          const res = await authApi.getCurrentUser();
          if (res.success && res.data) {
            setUser(res.data);
          } else {
            logout();
          }
        } catch {
          logout();
        }
      }
      setLoading(false);
    };
    initAuth();
  }, []);

  const login = async (email, password) => {
    const res = await authApi.login(email, password);
    if (res.success && res.data) {
      localStorage.setItem('cs_token', res.data.accessToken);
      localStorage.setItem('cs_user', JSON.stringify(res.data.user));
      setToken(res.data.accessToken);
      setUser(res.data.user);
      return res.data;
    }
    throw new Error(res.message || 'Login failed');
  };

  const register = async (email, password, displayName) => {
    const res = await authApi.register(email, password, displayName);
    if (res.success && res.data) {
      localStorage.setItem('cs_token', res.data.accessToken);
      localStorage.setItem('cs_user', JSON.stringify(res.data.user));
      setToken(res.data.accessToken);
      setUser(res.data.user);
      return res.data;
    }
    throw new Error(res.message || 'Registration failed');
  };

  const logout = () => {
    localStorage.removeItem('cs_token');
    localStorage.removeItem('cs_user');
    setToken(null);
    setUser(null);
  };

  const isAdmin = user && user.roles && user.roles.includes('ROLE_ADMIN');

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, isAdmin, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
