import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/client';
import { errMsg } from '../utils';
import { Alert, Field, Button, AuthCard } from '../components/ui';
export default function Signup() {
  const nav = useNavigate();
  const [f, setF] = useState({ userName: '', email: '', password: '', sentimentAnalysis: false });
  const [errs, setErrs] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const on = (k) => (e) => setF({ ...f, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });
  const submit = async (e) => {
    e.preventDefault();
    const v = {};
    if (f.userName.trim().length < 3) v.userName = 'At least 3 characters';
    if (!/^\S+@\S+\.\S+$/.test(f.email)) v.email = 'Enter a valid email';
    if (f.password.length < 6) v.password = 'At least 6 characters';
    setErrs(v); setError('');
    if (Object.keys(v).length) return;
    setBusy(true);
    try { await authApi.signup({ ...f, userName: f.userName.trim(), email: f.email.trim() }); nav('/login'); }
    catch (err) { setError(err.response?.status === 500 ? 'Could not create account (username may already be taken)' : errMsg(err)); setBusy(false); }
  };
  return (
    <AuthCard title="Create account">
      <form onSubmit={submit} noValidate>
        <Alert>{error}</Alert>
        <Field label="Username" value={f.userName} onChange={on('userName')} error={errs.userName} autoComplete="username" autoFocus />
        <Field label="Email" type="email" value={f.email} onChange={on('email')} error={errs.email} autoComplete="email" />
        <Field label="Password" type="password" value={f.password} onChange={on('password')} error={errs.password} autoComplete="new-password" />
        <label className="check"><input type="checkbox" checked={f.sentimentAnalysis} onChange={on('sentimentAnalysis')} /> Email me a weekly sentiment summary</label>
        <Button loading={busy} variant="primary block">Sign up</Button>
      </form>
      <p className="muted center">Have an account? <Link to="/login">Log in</Link></p>
    </AuthCard>
  );
}
