import { Injectable } from '@angular/core';
import {filter, Observable} from 'rxjs';
import { MqttService, IMqttMessage } from 'ngx-mqtt';
import {map} from "rxjs/operators";

@Injectable({
    providedIn: 'root'
})
export class MyMqttService {
    private topic: string = 'deeper/depth';
    private message$: Observable<IMqttMessage>;

    constructor(private mqttService: MqttService) {
        this.message$ = this.mqttService.observe(this.topic);
    }

    subscribe<T>(): Observable<T> {
        return this.message$.pipe(
            map((message: IMqttMessage) => JSON.parse(message.payload.toString()) as T)
        );
    }
}
