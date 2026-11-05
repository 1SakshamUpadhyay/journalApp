
import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { journalApi } from '../api/client';
import { errMsg } from '../utils';
import { Alert, Field, Button, Spinner } from '../components/ui';

export default function EntryForm() {
  const { id } = useParams();
  const nav = useNavigate();

  const [f, setF] = useState({
    title: '',
    content: '',
    sentiment: ''
  });

  const [loading, setLoading] = useState(!!id);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [errs, setErrs] = useState({});

  const sentiments = [
    { value: 'HAPPY', emoji: '😊', label: 'Happy' },
    { value: 'SAD', emoji: '😢', label: 'Sad' },
    { value: 'ANGRY', emoji: '😠', label: 'Angry' },
    { value: 'ANXIOUS', emoji: '😟', label: 'Anxious' }
  ];

  useEffect(() => {
    if (!id) return;

    journalApi.get(id)
      .then((e) => setF({
        title: e.title || '',
        content: e.content || '',
        sentiment: e.sentiment || ''
      }))
      .catch((e) => setError(errMsg(e, 'Entry not found')))
      .finally(() => setLoading(false));
  }, [id]);

  const submit = async (e) => {
    e.preventDefault();

    const v = {};

    if (!f.title.trim()) {
      v.title = 'Title is required';
    }

    if (!f.sentiment) {
      v.sentiment = 'Please select your mood';
    }

    setErrs(v);
    setError('');

    if (Object.keys(v).length > 0) return;

    setBusy(true);

    try {
      if (id) {
        await journalApi.update(id, f);
      } else {
        await journalApi.create(f);
      }

      nav('/');
    } catch (err) {
      setError(errMsg(err));
      setBusy(false);
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="card narrow">
      <h2>{id ? 'Edit entry' : 'New entry'}</h2>

      <form onSubmit={submit} noValidate>
        <Alert>{error}</Alert>

        <Field
          label="Title"
          value={f.title}
          onChange={(e) =>
            setF({ ...f, title: e.target.value })
          }
          error={errs.title}
          autoFocus
        />

        <Field
          as="textarea"
          rows={10}
          label="Content"
          value={f.content}
          onChange={(e) =>
            setF({ ...f, content: e.target.value })
          }
        />

        <div style={{ marginTop: '20px', marginBottom: '20px' }}>
          <label
            htmlFor="sentiment"
            style={{
              display: 'block',
              marginBottom: '8px',
              fontWeight: 500
            }}
          >
            How are you feeling today?
          </label>

          <select
            id="sentiment"
            value={f.sentiment}
            onChange={(e) =>
              setF({ ...f, sentiment: e.target.value })
            }
            style={{
              width: '100%',
              padding: '14px',
              border: '1px solid #d1d5db',
              borderRadius: '12px',
              fontSize: '16px',
              backgroundColor: 'white'
            }}
          >
            <option value="">Select your mood</option>

            {sentiments.map((s) => (
              <option key={s.value} value={s.value}>
                {s.emoji} {s.label}
              </option>
            ))}
          </select>

          {errs.sentiment && (
            <p style={{ color: 'red', fontSize: '13px' }}>
              {errs.sentiment}
            </p>
          )}
        </div>

        <div className="row end">
          <Link className="btn ghost" to="/">
            Cancel
          </Link>

          <Button loading={busy} variant="primary">
            {id ? 'Save changes' : 'Create entry'}
          </Button>
        </div>
      </form>
    </div>
  );
}
