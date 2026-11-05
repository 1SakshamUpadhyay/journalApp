import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { jwtSub } from '../utils';
const Ctx = createContext(null);
export const useAuth = () => useContext(Ctx);
export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const login = useCallback((t) => { localStorage.setItem('token', t); setToken(t); }, []);
  const logout = useCallback(() => { localStorage.removeItem('token'); setToken(null); }, []);
  useEffect(() => { window.addEventListener('auth:logout', logout); return () => window.removeEventListener('auth:logout', logout); }, [logout]);
  return <Ctx.Provider value={{ token, username: token ? jwtSub(token) : '', login, logout }}>{children}</Ctx.Provider>;
}
