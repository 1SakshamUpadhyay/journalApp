import { useEffect, useState } from 'react';
import { userApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { errMsg } from '../utils';
import { Alert, Field, Button } from '../components/ui';
export default function Profile() {
  const { logout, username } = useAuth();
  const [greeting, setGreeting] = useState('');
  const [f, setF] = useState({ username, password: '' });
  const [errs, setErrs] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => { userApi.greeting().then(setGreeting).catch(() => {}); }, []);
  const save = async (e) => {
    e.preventDefault();
    const v = {};
    if (f.username.trim().length < 3) v.username = 'At least 3 characters';
    if (f.password.length < 6) v.password = 'At least 6 characters';
    setErrs(v); setError('');
    if (Object.keys(v).length) return;
    setBusy(true);
    try { await userApi.update({ username: f.username.trim(), password: f.password }); logout(); } // token is tied to old username
    catch (err) { setError(errMsg(err)); setBusy(false); }
  };
  const del = async () => {
    if (!window.confirm('Permanently delete your account?')) return;
    try { await userApi.remove(); logout(); } catch (err) { setError(errMsg(err)); }
  };
  return (
    <div className="card narrow">
      <h2>Profile</h2>
      {greeting && <p className="muted">{greeting}</p>}
      <form onSubmit={save} noValidate>
        <Alert>{error}</Alert>
        <Field label="Username" value={f.username} onChange={(e) => setF({ ...f, username: e.target.value })} error={errs.username} />
        <Field label="New password" type="password" value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} error={errs.password} autoComplete="new-password" />
        <small className="muted">You will be logged out after saving.</small>
        <div className="row end"><Button loading={busy} variant="primary">Update</Button></div>
      </form>
      <hr /><div className="row"><span>Delete account and all data</span><button className="btn danger" onClick={del}>Delete account</button></div>
    </div>
  );
}
