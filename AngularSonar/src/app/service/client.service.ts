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
  private readonly sonarInfo: BehaviorSubject<SonarState>;
  private sonarClientData: BehaviorSubject<SonarClientData>;
  private readonly endpoint: string;
  private batteryLevels: Map<number, number> = new Map([
    [4.2, 100],
    [4.1, 90],
    [4.0, 80],
    [3.9, 70],
    [3.8, 60],
    [3.7, 50],
    [3.6, 40],
    [3.5, 30],
    [3.4, 20],
    [3.3, 10]
  ]);
  private wsService: WebSocketServiceImpl;


  constructor(wsService: WebSocketServiceImpl) {
    this.wsService = wsService;
    this.wsService.on<SonarData>(WS.SONAR).subscribe((message) => {
      const data = new SonarClientData();
      try {
        data.isSonarAvailable = true;
        data.depth = Number(message.depth);
        data.batteryLevel = this.updateBatteryLevel(Number(message.battery));
        data.waterTemp = Number(message.temperature);
        if (200 === Number(message.status)) {
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
      console.log(isConnected);
      if (!isConnected) {
        this.setState({isSonarAvailable: false, isMeasureSuccess: false});
      }
    });

    let a = 0;
    setInterval(() => {
      this.wsService.send(WS.SONAR,);
      a = a + 1;
    }, environment.interval);
    this.endpoint = environment.url;
    this.sonarInfo = new BehaviorSubject<SonarState>({isSonarAvailable: false, isMeasureSuccess: false});
    const sonarClientData = new SonarClientData();
    sonarClientData.batteryLevel = 0;
    sonarClientData.waterTemp = 0;
    sonarClientData.depth = 0;
    sonarClientData.isSonarAvailable = false;
    this.sonarClientData = new BehaviorSubject<SonarClientData>(sonarClientData);
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
      sonarClientData.batteryLevel = this.updateBatteryLevel(Number(sonarData.battery));
      sonarClientData.waterTemp = Number(sonarData.temperature);
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

  private updateBatteryLevel(batteryADC: number): number {
    const batteryVcc: number = (4.3 * Number(batteryADC.toFixed(2)) / 1023) - 0.1;
    for (const [key, value] of this.batteryLevels.entries()) {
      if (batteryVcc > key) {
        return value;
      }
    }
  }


}
