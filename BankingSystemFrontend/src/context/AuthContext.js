// --- src/context/AuthContext.js ---
import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('authToken'));
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const validateToken = async () => {
      if (token) {
        try {
          // /user/details should validate the token and return user info
          const response = await api.get('/user/details'); 
          setUser(response.data);
          localStorage.setItem('authToken', token);
        } catch (error) {
          console.error("Session invalid, logging out");
          handleLogout();
        }
      }
      setIsLoading(false);
    };
    validateToken();
  }, [token]);

  const handleLogout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('authToken');
    navigate('/login');
  };

  const login = async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    const { token, user } = response.data;
    setToken(token);
    setUser(user);
    localStorage.setItem('authToken', token);
    
    if (user.role === 'Admin') {
      navigate('/admin');
    } else {
      navigate('/dashboard');
    }
  };

  const register = async (userData) => {
    const response = await api.post('/auth/register', userData);
    const { token, user } = response.data;
    setToken(token);
    setUser(user);
    localStorage.setItem('authToken', token);
    navigate('/dashboard');
  };

  const logout = () => {
    handleLogout();
  };

  const value = {
    user,
    token,
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Custom hook to use the AuthContext
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};