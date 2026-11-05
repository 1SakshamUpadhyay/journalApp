
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { errMsg } from '../utils';
import { Alert, Field, Button, AuthCard } from '../components/ui';

export default function Login() {
  const { login } = useAuth();

  const [f, setF] = useState({
    username: '',
    password: ''
  });

  const [errs, setErrs] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const on = (key) => (e) =>
    setF({ ...f, [key]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();

    const validation = {};

    if (!f.username.trim()) {
      validation.username = 'Username is required';
    }

    if (!f.password) {
      validation.password = 'Password is required';
    }

    setErrs(validation);
    setError('');

    if (Object.keys(validation).length) return;

    setBusy(true);

    try {
      const token = await authApi.login(
        f.username.trim(),
        f.password
      );

      login(token);

    } catch (err) {
      setError(
        err.response?.status === 400
          ? 'Invalid username or password'
          : errMsg(err)
      );

      setBusy(false);
    }
  };

  const googleLogin = () => {
    window.location.assign('/api/auth/google/start');
  };

  return (
    <AuthCard title="Welcome back">

      <form onSubmit={submit} noValidate>

        <Alert>{error}</Alert>

        <Field
          label="Username"
          value={f.username}
          onChange={on('username')}
          error={errs.username}
          autoComplete="username"
          autoFocus
        />

        <Field
          label="Password"
          type="password"
          value={f.password}
          onChange={on('password')}
          error={errs.password}
          autoComplete="current-password"
        />

        <Button loading={busy} variant="primary block">
          Log in
        </Button>

      </form>

      <div className="center" style={{ margin: '18px 0' }}>
        <span className="muted">OR</span>
      </div>

      <button
        type="button"
        className="btn ghost"
        style={{ width: '100%' }}
        onClick={googleLogin}
      >
        Continue with Google
      </button>

      <p className="muted center">
        No account? <Link to="/signup">Sign up</Link>
      </p>

    </AuthCard>
  );
}