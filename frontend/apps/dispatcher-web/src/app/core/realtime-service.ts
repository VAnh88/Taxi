import { Injectable, OnDestroy, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { FleetLocationEvent, TripStatusChangedEvent } from '@org/shared-types';
import { WS_SOCKJS_URL } from './api.config';

@Injectable({ providedIn: 'root' })
export class RealtimeService implements OnDestroy {
  private client: Client | null = null;

  readonly fleetLocations = signal<Map<string, FleetLocationEvent>>(new Map());
  readonly lastTripUpdate = signal<TripStatusChangedEvent | null>(null);

  connect(): void {
    if (this.client) {
      return;
    }
    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_SOCKJS_URL),
      reconnectDelay: 3000,
      onConnect: () => {
        this.client?.subscribe('/topic/fleet/locations', (message) => {
          const event: FleetLocationEvent = JSON.parse(message.body);
          const next = new Map(this.fleetLocations());
          next.set(event.driverId, event);
          this.fleetLocations.set(next);
        });

        this.client?.subscribe('/topic/dispatch/trip-updates', (message) => {
          const event: TripStatusChangedEvent = JSON.parse(message.body);
          this.lastTripUpdate.set(event);
        });
      },
    });
    this.client.activate();
  }

  ngOnDestroy(): void {
    this.client?.deactivate();
  }
}
