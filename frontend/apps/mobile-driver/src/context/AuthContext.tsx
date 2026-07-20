import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthResponse, Driver } from '@org/shared-types';
import { apiClient } from '../api/client';

interface AuthState {
  token: string | null;
  driverId: string | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [driverId, setDriverId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      const storedToken = await AsyncStorage.getItem('taxi_driver_token');
      const storedDriverId = await AsyncStorage.getItem('taxi_driver_id');
      setToken(storedToken);
      setDriverId(storedDriverId);
      setLoading(false);
    })();
  }, []);

  async function login(username: string, password: string) {
    const res = await apiClient.post<AuthResponse>('/api/auth/login', { username, password });
    await AsyncStorage.setItem('taxi_driver_token', res.data.token);
    setToken(res.data.token);

    // Token vừa lưu sẽ được interceptor gắn vào request tiếp theo.
    const meRes = await apiClient.get<Driver>('/api/drivers/me');
    await AsyncStorage.setItem('taxi_driver_id', meRes.data.id);
    setDriverId(meRes.data.id);
  }

  async function logout() {
    await AsyncStorage.removeItem('taxi_driver_token');
    await AsyncStorage.removeItem('taxi_driver_id');
    setToken(null);
    setDriverId(null);
  }

  return (
    <AuthContext.Provider value={{ token, driverId, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth phải dùng trong AuthProvider');
  return ctx;
}
