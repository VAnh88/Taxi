import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useAuth } from '../context/AuthContext';
import LoginScreen from '../screens/LoginScreen';
import BookTripScreen from '../screens/BookTripScreen';
import TrackTripScreen from '../screens/TrackTripScreen';

const Stack = createNativeStackNavigator();

export default function RootNavigator() {
  const { token, loading } = useAuth();

  if (loading) {
    return null;
  }

  return (
    <NavigationContainer>
      <Stack.Navigator>
        {token ? (
          <>
            <Stack.Screen name="BookTrip" component={BookTripScreen} options={{ title: 'Đặt xe' }} />
            <Stack.Screen name="TrackTrip" component={TrackTripScreen} options={{ title: 'Theo dõi chuyến' }} />
          </>
        ) : (
          <Stack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
