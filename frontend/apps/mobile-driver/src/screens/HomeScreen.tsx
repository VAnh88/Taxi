import React, { useEffect, useRef, useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Switch } from 'react-native';
import { Client } from '@stomp/stompjs';
import { TripStatusChangedEvent } from '@org/shared-types';
import { apiClient } from '../api/client';
import { WS_BASE_URL } from '../api/config';
import { useAuth } from '../context/AuthContext';

/**
 * Phase 1: nhập tay tọa độ hiện tại thay vì lấy GPS thật (expo-location sẽ thêm ở phase sau).
 */
export default function HomeScreen({ navigation }: any) {
  const { driverId } = useAuth();
  const [online, setOnline] = useState(false);
  const [lat, setLat] = useState('21.3187');
  const [lng, setLng] = useState('105.5908');
  const clientRef = useRef<Client | null>(null);

  async function toggleOnline(value: boolean) {
    setOnline(value);
    await apiClient.patch(`/api/drivers/${driverId}/shift`, {
      shiftStatus: value ? 'ON' : 'OFF',
      lat: parseFloat(lat),
      lng: parseFloat(lng),
    });
  }

  useEffect(() => {
    if (!driverId) return;

    const client = new Client({
      webSocketFactory: () => new WebSocket(`${WS_BASE_URL}/ws`),
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe(`/topic/driver/${driverId}/incoming-trip`, (message) => {
          const event: TripStatusChangedEvent = JSON.parse(message.body);
          navigation.navigate('ActiveTrip', { tripId: event.tripId });
        });
      },
    });
    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [driverId]);

  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <Text style={styles.label}>{online ? 'Đang lên ca' : 'Đang xuống ca'}</Text>
        <Switch value={online} onValueChange={toggleOnline} />
      </View>

      <Text style={styles.label}>Vị trí hiện tại (tạm nhập tay)</Text>
      <View style={styles.rowInputs}>
        <TextInput style={[styles.input, styles.half]} value={lat} onChangeText={setLat} keyboardType="numeric" placeholder="Lat" />
        <TextInput style={[styles.input, styles.half]} value={lng} onChangeText={setLng} keyboardType="numeric" placeholder="Lng" />
      </View>
      <Button title="Cập nhật vị trí" onPress={() => toggleOnline(online)} />

      <Text style={styles.hint}>
        {online ? 'Đang chờ cuốc mới...' : 'Bật lên ca để bắt đầu nhận cuốc'}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 },
  rowInputs: { flexDirection: 'row', gap: 8, marginBottom: 12 },
  label: { fontWeight: '600', fontSize: 16 },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 10 },
  half: { flex: 1 },
  hint: { marginTop: 24, textAlign: 'center', color: '#777' },
});
