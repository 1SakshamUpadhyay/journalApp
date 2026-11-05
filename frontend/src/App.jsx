
import { Routes, Route, Navigate } from 'react-router-dom';

import Layout from './components/Layout';
import { Protected, GuestOnly } from './components/Guards';

import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import EntryForm from './pages/EntryForm';
import Profile from './pages/Profile';
import Admin from './pages/Admin';
import GoogleCallback from './pages/GoogleCallback';

export default function App() {
  return (
    <Routes>

      {/* Guest routes */}
      <Route element={<GuestOnly />}>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
      </Route>

      {/* Google OAuth callback must be publicly accessible */}
      <Route
        path="/oauth/callback"
        element={<GoogleCallback />}
      />

      {/* Protected routes */}
      <Route element={<Protected />}>
        <Route element={<Layout />}>

          <Route path="/" element={<Dashboard />} />

          <Route path="/entry/new" element={<EntryForm />} />

          <Route path="/entry/:id" element={<EntryForm />} />

          <Route path="/profile" element={<Profile />} />

          <Route path="/admin" element={<Admin />} />

        </Route>
      </Route>

      <Route
        path="*"
        element={<Navigate to="/" replace />}
      />

    </Routes>
  );
}