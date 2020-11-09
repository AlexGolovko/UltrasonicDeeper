import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketConfig {
  constructor() {
    this.url = environment.wsEndpoint;
  }

  reconnectAttempts: number;
  reconnectInterval: number;
  url: string;

}
