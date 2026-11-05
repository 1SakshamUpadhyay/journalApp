import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
export const Protected = () => (useAuth().token ? <Outlet /> : <Navigate to="/login" replace />);
export const GuestOnly = () => (useAuth().token ? <Navigate to="/" replace /> : <Outlet />);
