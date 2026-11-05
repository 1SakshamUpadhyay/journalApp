
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/client';

export default function GoogleCallback() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [error, setError] = useState('');

  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;

    started.current = true;

    const exchangeTicket = async () => {
      const params = new URLSearchParams(window.location.search);

      const ticket = params.get('ticket');

      if (params.get('error') || !ticket) {
        setError('Google login failed. Please try again.');
        return;
      }

      try {
        const response = await api.post(
          '/auth/google/exchange',
          { ticket }
        );

        const token = response.data?.token;

        if (!token) {
          throw new Error('JWT token was not returned');
        }

        login(token);

        navigate('/', { replace: true });

      } catch (err) {
        setError('Google login failed. Please try again.');
      }
    };

    exchangeTicket();

  }, [login, navigate]);

  return (
    <div className="card narrow center">

      {error ? (
        <>
          <h2>Login failed</h2>

          <p>{error}</p>

          <button
            className="btn primary"
            onClick={() => navigate('/login', { replace: true })}
          >
            Back to login
          </button>
        </>
      ) : (
        <>
          <h2>Signing you in...</h2>

          <p>
            Please wait while we complete Google authentication.
          </p>
        </>
      )}

    </div>
  );
}