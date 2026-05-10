import { useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const IDLE_TIMEOUT_MS = 15 * 60 * 1000;
const WARNING_BEFORE_MS = 2 * 60 * 1000;

export function useIdleTimeout(onWarning) {
  const timer = useRef(null);
  const warningTimer = useRef(null);
  const navigate = useNavigate();

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      // Déconnexion locale même si l'API échoue
    } finally {
      localStorage.removeItem('user');
      navigate('/login?reason=timeout');
    }
  }, [navigate]);

  const resetTimer = useCallback(() => {
    clearTimeout(timer.current);
    clearTimeout(warningTimer.current);

    warningTimer.current = setTimeout(() => {
      if (onWarning) onWarning();
    }, IDLE_TIMEOUT_MS - WARNING_BEFORE_MS);

    timer.current = setTimeout(() => {
      logout();
    }, IDLE_TIMEOUT_MS);
  }, [logout, onWarning]);

  useEffect(() => {
    const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];
    events.forEach((e) => window.addEventListener(e, resetTimer, { passive: true }));
    resetTimer();

    return () => {
      events.forEach((e) => window.removeEventListener(e, resetTimer));
      clearTimeout(timer.current);
      clearTimeout(warningTimer.current);
    };
  }, [resetTimer]);
}
