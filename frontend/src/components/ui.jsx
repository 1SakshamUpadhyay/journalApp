export const Spinner = ({ label = 'Loading…' }) => <div className="center muted"><span className="spin" /> {label}</div>;
export const Alert = ({ type = 'error', children }) => children ? <div className={`alert ${type}`} role="alert">{children}</div> : null;
export const Field = ({ label, error, as, ...p }) => (
  <label className="field"><span>{label}</span>
    {as === 'textarea' ? <textarea {...p} /> : <input {...p} />}
    {error && <small className="err">{error}</small>}
  </label>
);
export const Button = ({ loading, children, variant = '', ...p }) => <button className={`btn ${variant}`} disabled={loading || p.disabled} {...p}>{loading ? 'Please wait…' : children}</button>;
export const AuthCard = ({ title, children }) => <div className="auth"><div className="card"><h1>📓 {title}</h1>{children}</div></div>;
