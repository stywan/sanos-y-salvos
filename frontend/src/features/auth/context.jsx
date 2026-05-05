import { createContext, useContext, useState, useCallback } from 'react';

const AuthCtx = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const raw = localStorage.getItem('ssv_user');
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  });

  const login = useCallback((userData, token) => {
    localStorage.setItem('ssv_token', token);
    localStorage.setItem('ssv_user', JSON.stringify(userData));
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('ssv_token');
    localStorage.removeItem('ssv_user');
    setUser(null);
  }, []);

  return (
    <AuthCtx.Provider value={{ user, login, logout }}>
      {children}
    </AuthCtx.Provider>
  );
}

export function useAuthContext() {
  const ctx = useContext(AuthCtx);
  if (!ctx) throw new Error('useAuthContext must be used inside AuthProvider');
  return ctx;
}
