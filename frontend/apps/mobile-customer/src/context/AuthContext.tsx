import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthResponse } from '@org/shared-types';
import { apiClient } from '../api/client';

interface AuthState {
  userId: string | null;
  token: string | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [userId, setUserId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      const storedToken = await AsyncStorage.getItem('taxi_customer_token');
      const storedUserId = await AsyncStorage.getItem('taxi_customer_user_id');
      setToken(storedToken);
      setUserId(storedUserId);
      setLoading(false);
    })();
  }, []);

  async function login(username: string, password: string) {
    const res = await apiClient.post<AuthResponse>('/api/auth/login', { username, password });
    await AsyncStorage.setItem('taxi_customer_token', res.data.token);
    await AsyncStorage.setItem('taxi_customer_user_id', res.data.userId);
    setToken(res.data.token);
    setUserId(res.data.userId);
  }

  async function logout() {
    await AsyncStorage.removeItem('taxi_customer_token');
    await AsyncStorage.removeItem('taxi_customer_user_id');
    setToken(null);
    setUserId(null);
  }

  return (
    <AuthContext.Provider value={{ token, userId, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth phải dùng trong AuthProvider');
  return ctx;
}
