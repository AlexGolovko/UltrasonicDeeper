import {Injectable} from '@angular/core';
import {JavaScriptInterface} from '../JavaInterface/JavaScriptInterface';
import {SonarClientData} from './SonarClientData';
import {AndroidData} from '../DTO/AndroidData';
import {environment} from '../../environments/environment';

declare var TrackingService: JavaScriptInterface;

@Injectable({
    providedIn: 'root'
})
export class AndroidBridgeService {
    private readonly TrackingService: JavaScriptInterface;
    private androidDataList: Array<AndroidData>;
    private androidListSendSize: number = environment.listSize;

    constructor() {
        if (typeof TrackingService === 'undefined') {
            console.log('TrackingService is undefined');
            this.TrackingService = new class implements JavaScriptInterface {
                getMapCacheDir(): string {
                    return '';
                }

                downloadMap(map: string): void {
                }

                getActivity(): string {
                    return 'mock';
                }

                saveTrackingList(data: string): void {
                }
            }();
        } else {
            this.TrackingService = TrackingService;
        }
    }

    getActivity(): string {
        return this.TrackingService.getActivity();
    }

    isAvailable() {
        return typeof this.TrackingService !== 'undefined';
    }

    saveTrackingList(data: string) {
        this.TrackingService.saveTrackingList(data);
    }

    getMapCacheDir(): string {
        return this.TrackingService.getMapCacheDir()
    }

    saveAndroidData(response: SonarClientData, crd: Position): void {
        const data: AndroidData = new AndroidData(response.depth.toString(), response.batteryLevel.toString(), response.waterTemp.toString(), crd, String(Date.now()));
        if (this.isAvailable()) {
            if (this.androidDataList.length > this.androidListSendSize) {
                this.saveTrackingList(JSON.stringify(this.androidDataList.splice(0)));
            }
            this.androidDataList.push(data);
        } else {
            console.log('TrackingService is undefined');
        }
    }

    // downloadMap(tiles: MapCoordinates) {
    //     this.TrackingService.downloadMap(JSON.stringify(tiles));
    // }


}

