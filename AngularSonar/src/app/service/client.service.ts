import {SonarData} from '../SonarData';
import {environment} from '../../environments/environment';
import {SonarClientData} from './SonarClientData';

export class ClientService {
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
    this.endpoint = environment.url;
  }

  async getSonarData(): Promise<SonarClientData> {
    const sonarClientData: SonarClientData = new SonarClientData();
    try {
      const sonarData = await this.http<SonarData>(
        this.endpoint
      );
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
