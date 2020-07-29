import {Component, OnInit} from '@angular/core';
import {SonarData} from './SonarData';
import {environment} from 'src/environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'AngularSonar';
  batteryLevel: number;
  waterTemp: number;
  depth: number;
  private endpoint: string;
  private interval: any;
  public trackArray: Array<string>;
  public crd: Position;
  isSonarAvailable: boolean;
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
  private firstElement = true;

  constructor() {
  }

  ngOnInit(): void {
    document.body.style.backgroundColor = 'black';
    this.endpoint = environment.url;
    this.batteryLevel = 0;
    this.waterTemp = 0;
    this.depth = 0;
    this.trackArray = new Array<string>();
    this.isSonarAvailable = false;
    this.trackArray.push('Wait a second');
    this.geo();
    this.mainLoop();

  }

  private async mainLoop() {
    this.interval = setInterval(async () => {
      let sonarData: SonarData;
      try {
        sonarData = await this.http<SonarData>(
          this.endpoint
        );
        this.depth = Number(sonarData.depth);
        this.batteryLevel = this.updateBatteryLevel(Number(sonarData.battery));
        this.waterTemp = Number(sonarData.temperature);
        this.isSonarAvailable = true;
        this.increaseTrackArray(this.depth);
        // console.log(sonarData);
      } catch (e) {
        this.isSonarAvailable = false;
        console.log(e);
      }
    }, 500);
  }

  private updateBatteryLevel(batteryADC: number): number {
    const batteryVcc: number = (4.3 * Number(batteryADC.toFixed(2)) / 1023) - 0.1;
    for (const [key, value] of this.batteryLevels.entries()) {
      if (batteryVcc > key) {
        return value;
      }
    }
  }

  private geo(): void {
    const options = {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    };
    navigator.geolocation.watchPosition(position => {
      this.crd = position;
    }, err => {
      console.log(err);
      this.geo();
    }, options);
  }

  private increaseTrackArray(num: number): void {
    const array: Array<string> = Object.assign([], this.trackArray);
    const depth = Number(num.toFixed(2));
    if (this.firstElement) {
      array.shift();
      this.firstElement = false;
    }
    if (array.length > 8) {
      array.splice(-1, 1);
    }
    array.splice(0, 0, depth.toFixed(2));
    this.trackArray = array;
  }

  private async http<T>(
    request: RequestInfo
  ): Promise<T> {
    return await fetch(request).then(response => {
      this.isSonarAvailable = true;
      return response.json();
    }).catch(err => {
        this.isSonarAvailable = false;
        throw  new Error(err.statusText);
      }
    );
  }
}



