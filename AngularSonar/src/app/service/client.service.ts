import {SonarData} from '../SonarData';
import {environment} from '../../environments/environment';
import {SonarClientData} from './SonarClientData';
import {BehaviorSubject, Observable} from 'rxjs';
import {SonarState} from '../SonarState';
import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private static sonarInfo: BehaviorSubject<SonarState>;

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


  constructor() {
    console.log('In constructor');
    this.endpoint = environment.url;
    if (ClientService.sonarInfo == null) {
      ClientService.sonarInfo = new BehaviorSubject<SonarState>({isSonarAvailable: false, isMeasureSuccess: false});
    }
  }

  getState(): Observable<SonarState> {
    return ClientService.sonarInfo.asObservable();
  }

  setState(newState: SonarState): void {
    ClientService.sonarInfo.next(newState);
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
