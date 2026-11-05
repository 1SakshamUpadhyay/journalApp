
import { useEffect, useState } from 'react';
import { adminApi } from '../api/client';
import { errMsg } from '../utils';
import { Alert, Field, Button, Spinner } from '../components/ui';

export default function Admin() {
  const [users, setUsers] = useState(null);
  const [error, setError] = useState('');
  const [ok, setOk] = useState('');
  const [f, setF] = useState({ username: '', email: '' });
  const [errs, setErrs] = useState({});
  const [busy, setBusy] = useState(false);

  const load = () => {
    adminApi.users()
      .then(setUsers)
      .catch((e) => {
        setError(
          e.response?.status === 403
            ? 'Admin access required.'
            : errMsg(e)
        );
        setUsers([]);
      });
  };

  useEffect(() => {
    load();
  }, []);

  const promote = async (e) => {
    e.preventDefault();

    const v = {};

    if (f.username.trim().length < 3) {
      v.username = 'Enter a valid username';
    }

    if (!f.email.trim()) {
      v.email = 'Email is required';
    }

    setErrs(v);
    setError('');
    setOk('');

    if (Object.keys(v).length) return;

    setBusy(true);

    try {
      await adminApi.createAdmin({
        username: f.username.trim(),
        email: f.email.trim()
      });

      setOk('User successfully promoted to ADMIN.');
      setF({ username: '', email: '' });
      load();

    } catch (err) {
      setError(errMsg(err));
    } finally {
      setBusy(false);
    }
  };

  const clear = async () => {
    setError('');
    setOk('');

    try {
      await adminApi.clearCache();
      setOk('App cache cleared.');
    } catch (e) {
      setError(errMsg(e));
    }
  };

  return (
    <>
      <div className="bar">
        <h2>Admin</h2>
        <button className="btn ghost" onClick={clear}>
          Clear app cache
        </button>
      </div>

      <Alert>{error}</Alert>
      <Alert type="ok">{ok}</Alert>

      <div className="card">
        <h3>Users</h3>

        {users === null ? (
          <Spinner />
        ) : users.length === 0 ? (
          <p className="muted">No users to show.</p>
        ) : (
          <div className="scroll">
            <table>
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Roles</th>
                  <th>Sentiment</th>
                </tr>
              </thead>

              <tbody>
                {users.map((u) => (
                  <tr key={u.username}>
                    <td>{u.username}</td>
                    <td>{u.email}</td>
                    <td>{(u.roles || []).join(', ')}</td>
                    <td>{u.sentimentAnalysis ? 'Yes' : 'No'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="card">
        <h3>Promote existing user to admin</h3>

        <form onSubmit={promote} noValidate className="grid">
          <Field
            label="Username"
            value={f.username}
            onChange={(e) =>
              setF({ ...f, username: e.target.value })
            }
            error={errs.username}
          />

          <Field
            label="Email"
            type="email"
            value={f.email}
            onChange={(e) =>
              setF({ ...f, email: e.target.value })
            }
            error={errs.email}
          />

          <div className="row end">
            <Button loading={busy} variant="primary">
              Make admin
            </Button>
          </div>
        </form>
      </div>
    </>
  );
}