import {SonarData} from '../SonarData';
import {SonarClientData} from './SonarClientData';
import {BehaviorSubject, Observable} from 'rxjs';
import {SonarState} from '../SonarState';
import {Injectable} from '@angular/core';
import {WebSocketServiceImpl} from './websocket/websocket.service';
import {WS} from './websocket/wsmessage';
import {environment} from '../../environments/environment';


@Injectable({
    providedIn: 'root'
})
export class ClientService {

    constructor(wsService: WebSocketServiceImpl) {
        this.wsService = wsService;
        const sonarClientData = new SonarClientData();
        sonarClientData.batteryLevel = 0;
        sonarClientData.waterTemp = 0;
        sonarClientData.depth = 0;
        sonarClientData.isSonarAvailable = false;
        this.sonarClientData = new BehaviorSubject<SonarClientData>(sonarClientData);
        this.wsService.on<SonarData>(WS.SONAR).subscribe((message) => {
            const data = new SonarClientData();
            try {
                data.isSonarAvailable = true;
                data.batteryLevel = ClientService.updateBatteryLevel(Number(message.battery));
                data.waterTemp = Number(message.temperature);
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
        });

        this.wsService.status.subscribe(isConnected => {
            this.setState({isSonarAvailable: isConnected, isMeasureSuccess: isConnected});
        });

        this.endpoint = environment.url;
        this.sonarInfo = new BehaviorSubject<SonarState>({isSonarAvailable: false, isMeasureSuccess: false});
    }

    private readonly sonarInfo: BehaviorSubject<SonarState>;
    private sonarClientData: BehaviorSubject<SonarClientData>;
    private readonly endpoint: string;
    private clientInterval: any;
    private wsService: WebSocketServiceImpl;

    static updateBatteryLevel(batteryADC: number): number {
        const batteryVcc: number = (4.3 * Number(batteryADC.toFixed(2)) / 1023) - 0.25
        const batteryLevel = Math.trunc(100 * batteryVcc - 300)
        if (batteryLevel > 100) {
            return 100;
        }
        if (batteryLevel < 0) {
            return 1;
        }
        return batteryLevel;
    }

    public startConnection(): void {
        if (this.clientInterval === undefined) {
            console.log('startConnection')
            this.clientInterval = setInterval(() => {
                this.wsService.send(WS.SONAR, '1');
            }, environment.interval);
        }
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

    async getSonarData(): Promise<SonarClientData> {
        const sonarClientData: SonarClientData = new SonarClientData();
        try {
            const sonarData = await this.http<SonarData>(this.endpoint);
            sonarClientData.depth = Number(sonarData.depth);
            sonarClientData.batteryLevel = ClientService.updateBatteryLevel(Number(sonarData.battery));
            sonarClientData.waterTemp = Math.round(Number(sonarData.temperature) / 0.1) * 0.1;
            sonarClientData.isSonarAvailable = true;
            if (200 === Number(sonarData.status)) {
                sonarClientData.isMeasureSuccess = true; // 'SonarApp';
            } else {
                sonarClientData.isMeasureSuccess = false; // 'Too deep/shallow';
                console.log('Something wrong=' + sonarData.status);
            }
        } catch (e) {
            sonarClientData.isSonarAvailable = false;
            console.log(e);
        }
        this.setState({isSonarAvailable: sonarClientData.isSonarAvailable, isMeasureSuccess: sonarClientData.isMeasureSuccess});
        return sonarClientData;
    }

    private async http<T>(
        request: RequestInfo
    ): Promise<T> {
        return await fetch(request).then(response => {
            // this.isSonarAvailable = true;
            return response.json();
        }).catch(err => {
                // this.isSonarAvailable = false;
                throw new Error(err.statusText);
            }
        );
    }
}
