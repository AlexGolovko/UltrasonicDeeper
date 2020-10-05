import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClientService} from '../service/client.service';
import {SonarClientData} from '../service/SonarClientData';
import {GeoService} from '../service/geo.service';
import {environment} from '../../environments/environment';
import {AndroidData} from '../DTO/AndroidData';
import {JavaScriptInterface} from '../JavaInterface/JavaScriptInterface';

@Component({
  selector: 'app-client',
  templateUrl: './client.component.html',
  styleUrls: ['./client.component.css'],
  providers: [ClientService, GeoService]
})
export class ClientComponent implements OnInit, OnDestroy {
  public sonarClientData: SonarClientData;
  private interval: any;
  public trackArray: Array<string>;
  public crd: Position;
  private isFirstElement: boolean;
  private intervalTime: number;
  private watchPosition: number;
  private androidDataList: Array<AndroidData>;


  constructor(private clientService: ClientService, private geoService: GeoService) {

  }



  ngOnInit(): void {
    this.androidDataList = new Array<AndroidData>();
    document.body.style.backgroundColor = 'black';
    this.intervalTime = environment.interval;
    this.sonarClientData = new SonarClientData();
    this.sonarClientData.batteryLevel = 0;
    this.sonarClientData.waterTemp = 0;
    this.sonarClientData.depth = 0;
    this.sonarClientData.isSonarAvailable = false;
    this.trackArray = new Array<string>();
    this.isFirstElement = true;
    this.trackArray.push('Wait a second');
    this.geo();
    this.interval = setInterval(async () => {
      this.clientService.getSonarData().then(response => {
        // this.statusUpdated.emit({isSonarAvailable: response.isSonarAvailable, isMeasureSuccess: response.isMeasureSuccess});
        if (response.isSonarAvailable) {
          this.sonarClientData.batteryLevel = response.batteryLevel;
          this.sonarClientData.waterTemp = response.waterTemp;
          if (response.isMeasureSuccess) {
            this.sonarClientData.depth = response.depth;
            this.saveAndroidData(response, this.crd);
            this.increaseTrackArray(response.depth);
          }
        }
      });
    }, this.intervalTime);
  }

  ngOnDestroy(): void {
    clearInterval(this.interval);
    navigator.geolocation.clearWatch(this.watchPosition);
  }

  private increaseTrackArray(num: number): void {
    const array: Array<string> = Object.assign([], this.trackArray);
    const depth = Number(num.toFixed(2));
    if (this.isFirstElement) {
      array.shift();
      this.isFirstElement = false;
    }
    if (array.length > 8) {
      array.splice(-1, 1);
    }
    array.splice(0, 0, depth.toFixed(2));
    this.trackArray = array;
  }

  public geo(): void {
    const options = {
      enableHighAccuracy: true,
      timeout: 100000,
      maximumAge: 0
    };
    this.watchPosition = navigator.geolocation.watchPosition(position => {
      this.crd = position;
    }, err => {
      console.log(err);
      this.geo();
    }, options);
  }

  private saveAndroidData(response: SonarClientData, crd: Position) {
    const data: AndroidData = new AndroidData(response.depth.toString(), response.batteryLevel.toString(), response.waterTemp.toString(), crd, String(Date.now()));
    if (typeof TrackingService !== 'undefined') {
      if (this.androidDataList.length > 100) {
        TrackingService.saveTrackingList(JSON.stringify(this.androidDataList.splice(0)));
      }
      this.androidDataList.push(data);
    } else {
      console.log('TrackingService is undefined');
    }
  }
}

declare var TrackingService: JavaScriptInterface;
