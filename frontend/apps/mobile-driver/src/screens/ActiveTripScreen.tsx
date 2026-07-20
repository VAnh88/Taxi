import React, { useEffect, useState } from 'react';
import { View, Text, Button, StyleSheet, Alert } from 'react-native';
import { Trip, TripStatus } from '@org/shared-types';
import { apiClient } from '../api/client';

const NEXT_STATUS: Partial<Record<TripStatus, { next: TripStatus; label: string }>> = {
  DRIVER_ASSIGNED: { next: 'DRIVER_ARRIVING', label: 'Xác nhận, bắt đầu tới đón khách' },
  DRIVER_ARRIVING: { next: 'CUSTOMER_ONBOARD', label: 'Khách đã lên xe' },
  CUSTOMER_ONBOARD: { next: 'COMPLETED', label: 'Hoàn tất chuyến đi' },
};

export default function ActiveTripScreen({ route, navigation }: any) {
  const { tripId } = route.params;
  const [status, setStatus] = useState<TripStatus>('DRIVER_ASSIGNED');
  const [pickupAddress, setPickupAddress] = useState('');
  const [dropoffAddress, setDropoffAddress] = useState('');
  const [price, setPrice] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function load() {
    const res = await apiClient.get<Trip>(`/api/trips/${tripId}`);
    setStatus(res.data.status);
    setPickupAddress(res.data.pickupAddress);
    setDropoffAddress(res.data.dropoffAddress);
    setPrice(res.data.price);
  }

  useEffect(() => {
    load();
  }, [tripId]);

  async function advance() {
    const action = NEXT_STATUS[status];
    if (!action) return;
    setSubmitting(true);
    try {
      const res = await apiClient.patch<Trip>(`/api/trips/${tripId}/status`, { status: action.next });
      setStatus(res.data.status);
      setPrice(res.data.price);
      if (res.data.status === 'COMPLETED') {
        Alert.alert('Hoàn tất', `Cước phí: ${res.data.price}đ`, [
          { text: 'OK', onPress: () => navigation.navigate('Home') },
        ]);
      }
    } catch (e) {
      Alert.alert('Lỗi', 'Không cập nhật được trạng thái');
    } finally {
      setSubmitting(false);
    }
  }

  const action = NEXT_STATUS[status];

  return (
    <View style={styles.container}>
      <Text style={styles.label}>Điểm đón: {pickupAddress}</Text>
      <Text style={styles.label}>Điểm trả: {dropoffAddress}</Text>
      <Text style={styles.status}>Trạng thái: {status}</Text>
      {price != null && <Text style={styles.label}>Cước phí: {price}đ</Text>}

      {action && (
        <Button title={submitting ? 'Đang xử lý...' : action.label} onPress={advance} disabled={submitting} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  label: { fontSize: 16, marginBottom: 8 },
  status: { fontSize: 18, fontWeight: '700', marginVertical: 16 },
});
