import {SonarData} from '../model/SonarData';
import {SonarClientData} from '../model/SonarClientData';
import {BehaviorSubject, Observable, timeout} from 'rxjs';
import {SonarState} from '../model/SonarState';
import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient, HttpHeaders} from "@angular/common/http";


@Injectable({
    providedIn: 'root'
})
export class ClientService {

    private readonly sonarInfo: BehaviorSubject<SonarState>;
    private sonarClientData: BehaviorSubject<SonarClientData>;
    private readonly endpoint: string;
    private clientInterval: any;

    constructor(private http: HttpClient) {
        const sonarClientData = new SonarClientData();
        sonarClientData.batteryLevel = 0;
        sonarClientData.waterTemp = 0;
        sonarClientData.depth = 0;
        sonarClientData.isSonarAvailable = false;
        this.sonarClientData = new BehaviorSubject<SonarClientData>(sonarClientData);

        this.endpoint = environment.url;
        this.sonarInfo = new BehaviorSubject<SonarState>({isSonarAvailable: false, isMeasureSuccess: false});
    }

    private handleMessage(messageStr: any) {
        const message: SonarData = JSON.parse(messageStr.toString())
        const data = new SonarClientData();
        if (message === undefined) {
            data.isSonarAvailable = false;
            data.isMeasureSuccess = false;
        } else {
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
        }
        this.setState({isSonarAvailable: data.isSonarAvailable, isMeasureSuccess: data.isMeasureSuccess});
        this.sonarClientData.next(data);

    }

    static updateBatteryLevel(batteryADC: number): number {
        try {
            const batteryVcc: number = (4.3 * Number(batteryADC.toFixed(2)) / 1023) - 0.25
            const batteryLevel = Math.trunc(100 * batteryVcc - 300)
            if (batteryLevel > 100) {
                return 100
            }
            if (batteryLevel < 0) {
                return 1
            }
            return batteryLevel
        } catch (e) {
            console.log(e);
            return 1
        }
    }

    public startConnection(): void {
        if (this.clientInterval === undefined) {
            console.log('startConnection')
            this.clientInterval = setInterval(() => {
                const currTime = new Date().getTime()
                this.http.get(this.endpoint, {responseType: 'text', params: {id: currTime}})
                    .pipe(timeout(environment.timeout))
                    .subscribe(
                        (data: any) => {
                            this.handleMessage(data)
                        },
                        (error) => {
                            this.handleMessageError(error)
                        }
                    )

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

    private handleMessageError(err: any) {
        console.error(err)
        const data = new SonarClientData();
        data.isSonarAvailable = false;
        data.isMeasureSuccess = false;
        this.sonarClientData.next(data)
    }
}
