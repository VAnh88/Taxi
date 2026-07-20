import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert } from 'react-native';
import { CreateTripRequest, Trip } from '@org/shared-types';
import { apiClient } from '../api/client';

/**
 * Phase 1: nhập tay địa chỉ + tọa độ để luồng end-to-end chạy được.
 * Map picker thật (chọn điểm trên bản đồ) sẽ làm ở phase sau — xem plan.
 */
export default function BookTripScreen({ navigation }: any) {
  const [pickupAddress, setPickupAddress] = useState('');
  const [pickupLat, setPickupLat] = useState('21.3187');
  const [pickupLng, setPickupLng] = useState('105.5908');
  const [dropoffAddress, setDropoffAddress] = useState('');
  const [dropoffLat, setDropoffLat] = useState('21.33');
  const [dropoffLng, setDropoffLng] = useState('105.60');
  const [submitting, setSubmitting] = useState(false);

  async function handleBook() {
    setSubmitting(true);
    try {
      const body: CreateTripRequest = {
        pickupAddress,
        pickupLat: parseFloat(pickupLat),
        pickupLng: parseFloat(pickupLng),
        dropoffAddress,
        dropoffLat: parseFloat(dropoffLat),
        dropoffLng: parseFloat(dropoffLng),
        sourceChannel: 'CUSTOMER_APP',
      };
      const res = await apiClient.post<Trip>('/api/trips', body);
      navigation.navigate('TrackTrip', { tripId: res.data.id });
    } catch (e) {
      Alert.alert('Lỗi', 'Không đặt được xe, thử lại sau');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.label}>Điểm đón</Text>
      <TextInput style={styles.input} value={pickupAddress} onChangeText={setPickupAddress} placeholder="Địa chỉ đón" />
      <View style={styles.row}>
        <TextInput style={[styles.input, styles.half]} value={pickupLat} onChangeText={setPickupLat} keyboardType="numeric" placeholder="Lat" />
        <TextInput style={[styles.input, styles.half]} value={pickupLng} onChangeText={setPickupLng} keyboardType="numeric" placeholder="Lng" />
      </View>

      <Text style={styles.label}>Điểm trả</Text>
      <TextInput style={styles.input} value={dropoffAddress} onChangeText={setDropoffAddress} placeholder="Địa chỉ trả" />
      <View style={styles.row}>
        <TextInput style={[styles.input, styles.half]} value={dropoffLat} onChangeText={setDropoffLat} keyboardType="numeric" placeholder="Lat" />
        <TextInput style={[styles.input, styles.half]} value={dropoffLng} onChangeText={setDropoffLng} keyboardType="numeric" placeholder="Lng" />
      </View>

      <Button title={submitting ? 'Đang đặt xe...' : 'Đặt xe'} onPress={handleBook} disabled={submitting} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  label: { fontWeight: '600', marginTop: 12, marginBottom: 4 },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 10, marginBottom: 8 },
  row: { flexDirection: 'row', gap: 8 },
  half: { flex: 1 },
});
