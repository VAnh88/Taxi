import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Client } from '@stomp/stompjs';
import { Trip, TripStatus, TripStatusChangedEvent, TRIP_STATUS_LABEL_VI } from '@org/shared-types';
import { apiClient } from '../api/client';
import { WS_BASE_URL } from '../api/config';

export default function TrackTripScreen({ route }: any) {
  const { tripId } = route.params;
  const [status, setStatus] = useState<TripStatus>('REQUESTED');
  const [driverId, setDriverId] = useState<string | null>(null);

  useEffect(() => {
    apiClient.get<Trip>(`/api/trips/${tripId}`).then((res) => {
      setStatus(res.data.status);
      setDriverId(res.data.driverId);
    });

    const client = new Client({
      webSocketFactory: () => new WebSocket(`${WS_BASE_URL}/ws`),
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe(`/topic/trip/${tripId}`, (message) => {
          const event: TripStatusChangedEvent = JSON.parse(message.body);
          setStatus(event.status);
          setDriverId(event.driverId);
        });
      },
    });
    client.activate();

    return () => {
      client.deactivate();
    };
  }, [tripId]);

  return (
    <View style={styles.container}>
      <Text style={styles.status}>{TRIP_STATUS_LABEL_VI[status] ?? status}</Text>
      {driverId && <Text style={styles.detail}>Mã tài xế: {driverId}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  status: { fontSize: 20, fontWeight: '600', textAlign: 'center' },
  detail: { marginTop: 12, color: '#555' },
});
