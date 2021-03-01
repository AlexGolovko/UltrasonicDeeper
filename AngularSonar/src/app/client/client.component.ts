import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClientService} from '../service/client.service';
import {SonarClientData} from '../service/SonarClientData';
import {GeoService} from '../service/geo.service';
import {environment} from '../../environments/environment';
import {AndroidData} from '../DTO/AndroidData';
import {AndroidBridgeService} from '../service/android-bridge.service';

@Component({
    selector: 'app-client',
    templateUrl: './client.component.html',
    styleUrls: ['./client.component.css']
})
export class ClientComponent implements OnInit, OnDestroy {
    public readonly title = 'SonarApp';
    public readonly fail = 'Too deep/shallow';
    private readonly firstElement = 'Wait a second';
    public sonarClientData: SonarClientData = new SonarClientData();
    private interval: any;
    public trackArray: Array<string>;
    public crd: Position;
    private intervalTime: number;
    private watchPosition: number;
    private androidDataList: Array<AndroidData>;
    private androidListSendSize: number = environment.listSize;
    public isAvailable = false;
    public isMeasureSuccess = false;


    constructor(private clientService: ClientService, private geoService: GeoService, private androidBridge: AndroidBridgeService) {
    }


    ngOnInit(): void {
        this.clientService.getSonarClientData().subscribe(data => {
            this.isAvailable = data.isSonarAvailable;
            if (data.isSonarAvailable) {
                this.sonarClientData.batteryLevel = data.batteryLevel;
                this.sonarClientData.waterTemp = data.waterTemp;
                this.isMeasureSuccess = data.isMeasureSuccess;
                if (data.isMeasureSuccess) {
                    this.sonarClientData.depth = data.depth;
                    this.increaseTrackArray(data.depth);
                    this.saveAndroidData(data, this.crd)
                }
            }
        });
        this.geoService.getLocation().subscribe(value => this.crd = value);
        this.androidDataList = new Array<AndroidData>();
        document.body.style.backgroundColor = 'black';
        this.intervalTime = environment.interval;
        this.trackArray = new Array<string>();
        this.trackArray.push(this.firstElement);
        this.clientService.startConnection();
    }

    ngOnDestroy(): void {
        clearInterval(this.interval);
        this.clientService.stopConnection();
        navigator.geolocation.clearWatch(this.watchPosition);
    }

    private increaseTrackArray(num: number): void {
        const array: Array<string> = Object.assign([], this.trackArray);
        const depth = Number(num.toFixed(2));
        if (array[0] === this.firstElement) {
            array.shift();
        }
        if (array.length > 8) {
            array.splice(-1, 1);
        }
        array.splice(0, 0, depth.toFixed(2));
        this.trackArray = array;
    }

    private saveAndroidData(response: SonarClientData, crd: Position) {
        const data: AndroidData = new AndroidData(response.depth.toString(), response.batteryLevel.toString(), response.waterTemp.toString(), crd, String(Date.now()));
        if (this.androidBridge.isAvailable()) {
            if (this.androidDataList.length > this.androidListSendSize) {
                this.androidBridge.saveTrackingList(JSON.stringify(this.androidDataList.splice(0)));
            }
            this.androidDataList.push(data);
        } else {
            console.log('TrackingService is undefined');
        }
    }
}


