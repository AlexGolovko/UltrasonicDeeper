import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class WebSocketConfig {
    reconnectAttempts: number;
    reconnectInterval: number;
    url: string;

    constructor() {
        this.url = environment.wsEndpoint;
    }

}
