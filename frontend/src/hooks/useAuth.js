import { useState, useCallback } from 'react';
import api from '../services/api';

export default function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    try {
      const user = localStorage.getItem('user');
      return !!user;
    } catch {
      return false;
    }
  });

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      // Ignore errors on logout
    } finally {
      localStorage.removeItem('user');
      setIsAuthenticated(false);
      window.location.href = '/login';
    }
  }, []);

  const login = useCallback(() => {
    setIsAuthenticated(true);
  }, []);

  return { isAuthenticated, logout, login };
}
