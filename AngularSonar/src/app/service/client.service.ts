import {SonarData} from '../model/SonarData';
import {SonarClientData} from '../model/SonarClientData';
import {BehaviorSubject, Observable} from 'rxjs';
import {SonarState} from '../model/SonarState';
import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';


@Injectable({
    providedIn: 'root'
})
export class ClientService {

    private readonly sonarInfo: BehaviorSubject<SonarState>;
    private sonarClientData: BehaviorSubject<SonarClientData>;
    private clientInterval: any;

    constructor() {
        const sonarClientData = new SonarClientData();
        sonarClientData.batteryLevel = 0;
        sonarClientData.waterTemp = 0;
        sonarClientData.depth = 0;
        this.sonarClientData = new BehaviorSubject<SonarClientData>(sonarClientData);
        this.sonarInfo = new BehaviorSubject<SonarState>({isSonarAvailable: false, isMeasureSuccess: false});
        this.connectWebSocket()
    }

    private connectWebSocket() {
        const webSocket = new WebSocket(environment.wsEndpoint);

        webSocket.onopen = () => {
            console.log('WebSocket is connected.');

            const observer = {
                next: message => this.handleMessage(message),
                error: err => this.handleMessageError(err),
                complete: () => console.log('complete'),
            };
            webSocket.onmessage = (event) => {
                observer.next(event.data);
            };
            webSocket.onerror = (event) => {
                observer.error(event);
            };
            webSocket.onclose = () => {
                console.log('WebSocket connection closed. Reconnecting...');
                this.connectWebSocket();
            };
        };

        webSocket.onclose = () => {
            console.log('WebSocket connection closed. Reconnecting...');
            this.connectWebSocket();
        };
    }

    private handleMessage(messageStr: any) {
        if (messageStr === undefined || JSON.parse(messageStr).error !== undefined) {
            const data = new SonarClientData();
            data.isSonarAvailable = false;
            data.isMeasureSuccess = false;
            this.sonarClientData.next(data);
            console.log(`handleMessage error: ${JSON.parse(messageStr).error}`)
            return;
        }
        try {
            const message: SonarData = JSON.parse(messageStr)
            const data = new SonarClientData();
            try {
                data.isSonarAvailable = true;
                if (Number(message.battery) > 0) {
                    data.batteryLevel = ClientService.updateBatteryLevel(Number(message.battery));
                }
                if (Number(message.temperature) > -273) {
                    data.waterTemp = Number(message.temperature);
                }
                if (200 === Number(message.status)) {
                    data.depth = Number(message.depth);
                    data.isMeasureSuccess = true; // 'SonarApp';
                } else {
                    data.isMeasureSuccess = false; // 'Too deep/shallow';
                    console.log('Something wrong=' + message.status);
                }
            } catch (e) {
                data.isSonarAvailable = false;
                console.log(e);
            }

            this.setState({isSonarAvailable: data.isSonarAvailable, isMeasureSuccess: data.isMeasureSuccess});
            this.sonarClientData.next(data);
        } catch (Exception) {
            const data = new SonarClientData();
            data.isSonarAvailable = false;
            data.isMeasureSuccess = false;
            console.log(Exception);
        }

    }

    static updateBatteryLevel(voltage: number): number {
        const maxVoltage = 4.15; // Maximum voltage (fully charged)
        const minVoltage = 3.0; // Minimum voltage (discharged)
        try {
            // Calculate the percentage based on the voltage range
            const batteryLevel = ((voltage - minVoltage) / (maxVoltage - minVoltage)) * 100;
            if (batteryLevel > 100) {
                return 100
            }
            if (batteryLevel < 0) {
                return 1
            }
            return Math.round(batteryLevel)
        } catch (e) {
            console.log(e);
            return 1
        }
    }

    public startConnection(): void {
        // if (this.clientInterval === undefined) {
        //     console.log('startConnection')
        //     this.clientInterval = setInterval(() => {
        //         const currTime = new Date().getTime()
        //         this.http.get(environment.url, {responseType: 'text', params: {id: currTime}})
        //             .pipe(timeout(environment.timeout))
        //             .subscribe(
        //                 (data: any) => {
        //                     this.handleMessage(data)
        //                 },
        //                 (error) => {
        //                     this.handleMessageError(error)
        //                 }
        //             )
        //
        //     }, environment.interval);
        // }
    }

    public stopConnection(): void {
        clearInterval(this.clientInterval);
    }

    getState(): Observable<SonarState> {
        return this.sonarInfo.asObservable();
    }

    setState(newState: SonarState): void {
        this.sonarInfo.next(newState);
    }

    getSonarClientData(): Observable<SonarClientData> {
        return this.sonarClientData.asObservable();
    }

    private handleMessageError(err: any) {
        console.error(err)
        const data = new SonarClientData();
        data.isSonarAvailable = false;
        data.isMeasureSuccess = false;
        this.sonarClientData.next(data)
    }
}
