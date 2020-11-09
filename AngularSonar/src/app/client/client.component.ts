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
  styleUrls: ['./client.component.css']
})
export class ClientComponent implements OnInit, OnDestroy {
  public sonarClientData: SonarClientData = new SonarClientData();
  private interval: any;
  public trackArray: Array<string>;
  public crd: Position;
  private isFirstElement: boolean;
  private intervalTime: number;
  private watchPosition: number;
  private androidDataList: Array<AndroidData>;


  constructor(private clientService: ClientService, private geoService: GeoService) {
    this.clientService.getSonarClientData().subscribe(data => {
      if (data.isSonarAvailable) {
        this.sonarClientData.batteryLevel = data.batteryLevel;
        this.sonarClientData.waterTemp = data.waterTemp;
        if (data.isMeasureSuccess) {
          this.sonarClientData.depth = data.depth;
          this.saveAndroidData(data, this.crd);
          this.increaseTrackArray(data.depth);
        }
      }
    });
  }


  ngOnInit(): void {
    this.androidDataList = new Array<AndroidData>();
    document.body.style.backgroundColor = 'black';
    this.intervalTime = environment.interval;
    this.trackArray = new Array<string>();
    this.isFirstElement = true;
    this.trackArray.push('Wait a second');
    this.geo();
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
