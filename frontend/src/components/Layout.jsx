import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
export default function Layout() {
  const { username, logout } = useAuth();
  return (
    <>
      <header className="nav"><div className="container nav-in">
        <span className="brand">📓 Journal</span>
        <nav>
          <NavLink to="/" end>Entries</NavLink><NavLink to="/profile">Profile</NavLink><NavLink to="/admin">Admin</NavLink>
        </nav>
        <div className="nav-user"><span className="muted">{username}</span><button className="btn ghost sm" onClick={logout}>Log out</button></div>
      </div></header>
      <main className="container"><Outlet /></main>
    </>
  );
}
