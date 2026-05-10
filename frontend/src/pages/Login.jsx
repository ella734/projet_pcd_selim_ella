import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../services/api';
import { validateLogin, hasErrors } from '../schemas/validation';

const MAX_ATTEMPTS = 5;
const LOCKOUT_MS = 15 * 60 * 1000;

export default function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [attempts, setAttempts] = useState(0);
  const [lockedUntil, setLockedUntil] = useState(null);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const reason = searchParams.get('reason');
  const isLocked = lockedUntil && Date.now() < lockedUntil;
  const lockRemaining = isLocked ? Math.ceil((lockedUntil - Date.now()) / 60000) : 0;

  const handleLogin = async (e) => {
    e.preventDefault();
    setApiError('');

    if (isLocked) {
      setApiError(`Trop de tentatives. Réessayez dans ${lockRemaining} minute(s).`);
      return;
    }

    const validationErrors = validateLogin({ username, password });
    if (hasErrors(validationErrors)) {
      setErrors(validationErrors);
      return;
    }
    setErrors({});

    setLoading(true);
    try {
      const response = await api.post('/auth/login', { username, password });
      const { role, services } = response.data;

      localStorage.setItem('user', JSON.stringify({
        username: response.data.username,
        role,
        service: services?.[0]?.libelleS || 'Unknown Service',
        hopital: services?.[0]?.hopital?.libelleH || 'Unknown Hospital',
        serviceId: services?.[0]?.identifiantS || null,
        hopitalId: services?.[0]?.hopital?.identifiantH || null,
      }));

      setAttempts(0);
      if (onLoginSuccess) onLoginSuccess();
      navigate('/dashboard');
    } catch (err) {
      const newAttempts = attempts + 1;
      setAttempts(newAttempts);

      if (newAttempts >= MAX_ATTEMPTS) {
        setLockedUntil(Date.now() + LOCKOUT_MS);
        setApiError(`Compte temporairement bloqué après ${MAX_ATTEMPTS} tentatives. Réessayez dans 15 minutes.`);
      } else {
        setApiError(
          err.response?.status === 429
            ? 'Trop de tentatives. Réessayez plus tard.'
            : `Identifiants invalides. (${newAttempts}/${MAX_ATTEMPTS} tentatives)`
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Medical Portal</h1>
          <p>Connectez-vous à votre espace</p>
        </div>

        {reason === 'timeout' && (
          <div className="info-message" style={{ marginBottom: '1rem', padding: '0.75rem', background: '#fff3cd', borderRadius: '6px', fontSize: '0.875rem' }}>
            Votre session a expiré pour inactivité. Veuillez vous reconnecter.
          </div>
        )}
        {reason === 'expired' && (
          <div className="info-message" style={{ marginBottom: '1rem', padding: '0.75rem', background: '#fff3cd', borderRadius: '6px', fontSize: '0.875rem' }}>
            Votre session a expiré. Veuillez vous reconnecter.
          </div>
        )}

        <form onSubmit={handleLogin} noValidate>
          <div className="form-group">
            <label className="form-label">Nom d'utilisateur</label>
            <input
              type="text"
              className={`form-input${errors.username ? ' input-error' : ''}`}
              value={username}
              onChange={(e) => { setUsername(e.target.value); setErrors((p) => ({ ...p, username: undefined })); }}
              placeholder="Votre identifiant"
              autoComplete="username"
              maxLength={50}
              required
              disabled={isLocked || loading}
            />
            {errors.username && <span className="field-error" style={{ color: 'red', fontSize: '0.75rem' }}>{errors.username}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Mot de passe</label>
            <input
              type="password"
              className={`form-input${errors.password ? ' input-error' : ''}`}
              value={password}
              onChange={(e) => { setPassword(e.target.value); setErrors((p) => ({ ...p, password: undefined })); }}
              placeholder="Votre mot de passe"
              autoComplete="current-password"
              maxLength={100}
              required
              disabled={isLocked || loading}
            />
            {errors.password && <span className="field-error" style={{ color: 'red', fontSize: '0.75rem' }}>{errors.password}</span>}
          </div>

          {apiError && (
            <div className="error-message" style={{ color: 'red', fontSize: '0.875rem', marginBottom: '0.75rem' }}>
              {apiError}
            </div>
          )}

          <button
            type="submit"
            className="btn-primary"
            disabled={loading || isLocked}
            style={{ marginTop: '1rem' }}
          >
            {loading ? 'Connexion...' : isLocked ? `Bloqué (${lockRemaining} min)` : 'Se connecter'}
          </button>
        </form>
      </div>
    </div>
  );
}
