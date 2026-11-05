
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { journalApi } from '../api/client';
import { errMsg, entryId, fmtDate } from '../utils';
import { Alert, Spinner } from '../components/ui';

const MOODS = [
  { value: 'HAPPY', label: 'Happy', emoji: '😊', color: '#16a34a' },
  { value: 'SAD', label: 'Sad', emoji: '😢', color: '#3b82f6' },
  { value: 'ANGRY', label: 'Angry', emoji: '😠', color: '#ef4444' },
  { value: 'ANXIOUS', label: 'Anxious', emoji: '😟', color: '#f59e0b' }
];

const getDateKey = (date) => {
  const d = new Date(date);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

export default function Dashboard() {
  const [entries, setEntries] = useState(null);
  const [error, setError] = useState('');
  const [q, setQ] = useState('');

  useEffect(() => {
    journalApi.list()
      .then(setEntries)
      .catch((e) => {
        setError(errMsg(e));
        setEntries([]);
      });
  }, []);

  const remove = async (e) => {
    if (!window.confirm(`Delete "${e.title}"?`)) return;

    try {
      await journalApi.remove(entryId(e));
      setEntries((l) =>
        l.filter((x) => entryId(x) !== entryId(e))
      );
    } catch (err) {
      setError(errMsg(err));
    }
  };

  // Calculate sentiment statistics and the last seven days.
  const analytics = useMemo(() => {
    const list = entries || [];

    const counts = {};
    MOODS.forEach((m) => {
      counts[m.value] = list.filter(
        (e) => e.sentiment === m.value
      ).length;
    });

    const moodTotal = Object.values(counts).reduce(
      (sum, count) => sum + count, 0
    );

    let cumulative = 0;
    const pieSegments = MOODS
      .filter((m) => counts[m.value] > 0)
      .map((m) => {
        const start = cumulative;
        cumulative += (counts[m.value] / (moodTotal || 1)) * 100;
        return `${m.color} ${start}% ${cumulative}%`;
      });

    const pieStyle = moodTotal > 0
      ? `conic-gradient(${pieSegments.join(', ')})`
      : '#e5e7eb';

    const today = new Date();
    const days = [];

    for (let i = 6; i >= 0; i--) {
      const day = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - i
      );

      const key = getDateKey(day);

      const dayEntries = list.filter(
        (e) => getDateKey(e.date) === key
      );

      const dayCounts = {};
      MOODS.forEach((m) => {
        dayCounts[m.value] = dayEntries.filter(
          (e) => e.sentiment === m.value
        ).length;
      });

      days.push({
        key,
        label: day.toLocaleDateString('en-IN', {
          weekday: 'short'
        }),
        total: dayEntries.length,
        counts: dayCounts
      });
    }

    const maxDay = Math.max(
      1,
      ...days.map((d) => d.total)
    );

    return {
      counts,
      moodTotal,
      pieStyle,
      days,
      maxDay
    };
  }, [entries]);

  const shown = useMemo(
    () =>
      (entries || [])
        .filter((e) =>
          `${e.title} ${e.content || ''}`
            .toLowerCase()
            .includes(q.toLowerCase())
        )
        .sort(
          (a, b) =>
            new Date(b.date) - new Date(a.date)
        ),
    [entries, q]
  );

  return (
    <>
      <div className="bar">
        <h2>Your entries</h2>
        <Link className="btn primary" to="/entry/new">
          + New entry
        </Link>
      </div>

      <Alert>{error}</Alert>

      {entries === null ? (
        <Spinner />
      ) : (
        <>
          {/* SENTIMENT DASHBOARD */}
          <section style={{ marginBottom: '28px' }}>
            <h2>Sentiment Overview</h2>

            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(145px, 1fr))',
                gap: '14px',
                marginBottom: '20px'
              }}
            >
              <div className="card">
                <small className="muted">Total entries</small>
                <h2>{entries.length}</h2>
              </div>

              {MOODS.map((m) => (
                <div className="card" key={m.value}>
                  <small className="muted">
                    {m.emoji} {m.label}
                  </small>
                  <h2 style={{ color: m.color }}>
                    {analytics.counts[m.value]}
                  </h2>
                </div>
              ))}
            </div>

            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
                gap: '18px'
              }}
            >
              {/* PIE CHART */}
              <div className="card">
                <h3>Sentiment Distribution</h3>

                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'center',
                    padding: '20px'
                  }}
                >
                  <div
                    role="img"
                    aria-label="Sentiment distribution pie chart"
                    style={{
                      width: '190px',
                      height: '190px',
                      borderRadius: '50%',
                      background: analytics.pieStyle,
                      display: 'grid',
                      placeItems: 'center'
                    }}
                  >
                    <div
                      style={{
                        width: '105px',
                        height: '105px',
                        borderRadius: '50%',
                        background: 'white',
                        display: 'grid',
                        placeItems: 'center',
                        textAlign: 'center'
                      }}
                    >
                      <div>
                        <strong style={{ fontSize: '25px' }}>
                          {analytics.moodTotal}
                        </strong>
                        <div className="muted" style={{ fontSize: '12px' }}>
                          With mood
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: '1fr 1fr',
                    gap: '12px'
                  }}
                >
                  {MOODS.map((m) => (
                    <div
                      key={m.value}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '7px',
                        fontSize: '14px'
                      }}
                    >
                      <span
                        style={{
                          width: '11px',
                          height: '11px',
                          borderRadius: '3px',
                          background: m.color
                        }}
                      />
                      {m.emoji} {m.label}: {analytics.counts[m.value]}
                    </div>
                  ))}
                </div>
              </div>

              {/* WEEKLY TREND */}
              <div className="card">
                <h3>Weekly Mood Trend</h3>
                <p className="muted" style={{ fontSize: '13px' }}>
                  Sentiment entries over the last seven days
                </p>

                <div
                  style={{
                    display: 'flex',
                    alignItems: 'end',
                    justifyContent: 'space-around',
                    gap: '8px',
                    height: '180px',
                    paddingTop: '15px'
                  }}
                >
                  {analytics.days.map((day) => (
                    <div
                      key={day.key}
                      style={{
                        flex: 1,
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'end',
                        gap: '7px'
                      }}
                    >
                      <small style={{ fontSize: '11px' }}>
                        {day.total || ''}
                      </small>

                      <div
                        title={`${day.total} entries`}
                        style={{
                          height: '125px',
                          width: '100%',
                          maxWidth: '35px',
                          display: 'flex',
                          flexDirection: 'column-reverse',
                          borderRadius: '5px 5px 0 0',
                          overflow: 'hidden',
                          background: '#f3f4f6'
                        }}
                      >
                        {MOODS.map((m) => {
                          const count = day.counts[m.value];

                          return count > 0 ? (
                            <div
                              key={m.value}
                              style={{
                                height: `${(count / analytics.maxDay) * 125}px`,
                                minHeight: '3px',
                                background: m.color
                              }}
                            />
                          ) : null;
                        })}
                      </div>

                      <small style={{ fontSize: '11px' }}>
                        {day.label}
                      </small>
                    </div>
                  ))}
                </div>

                <div
                  style={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: '12px',
                    marginTop: '20px'
                  }}
                >
                  {MOODS.map((m) => (
                    <small
                      key={m.value}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '5px'
                      }}
                    >
                      <span
                        style={{
                          width: '9px',
                          height: '9px',
                          borderRadius: '2px',
                          background: m.color
                        }}
                      />
                      {m.label}
                    </small>
                  ))}
                </div>
              </div>
            </div>
          </section>

          {/* EXISTING JOURNAL ENTRIES */}
          <div className="bar">
            <h2>Your entries</h2>
            <Link className="btn primary" to="/entry/new">
              + New entry
            </Link>
          </div>

          <input
            className="search"
            placeholder="Search entries..."
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />

          {shown.length === 0 ? (
            <div className="card center muted">
              {entries.length
                ? 'No matches.'
                : 'No entries yet. Write your first one!'}
            </div>
          ) : (
            <div className="grid">
              {shown.map((e) => (
                <article
                  className="card entry"
                  key={entryId(e) || e.date}
                >
                  <div className="row">
                    <h3>{e.title}</h3>

                    {e.sentiment && (
                      <span className={`badge ${e.sentiment}`}>
                        {e.sentiment}
                      </span>
                    )}
                  </div>

                  <small className="muted">
                    {fmtDate(e.date)}
                  </small>

                  <p>{e.content}</p>

                  <div className="row end">
                    <Link
                      className="btn ghost sm"
                      to={`/entry/${entryId(e)}`}
                    >
                      Edit
                    </Link>

                    <button
                      className="btn danger sm"
                      onClick={() => remove(e)}
                    >
                      Delete
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </>
      )}
    </>
  );
}
