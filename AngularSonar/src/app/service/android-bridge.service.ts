import {Injectable} from '@angular/core';
import {JavaScriptInterface} from '../JavaInterface/JavaScriptInterface';
import {SonarClientData} from '../model/SonarClientData';
import {AndroidData} from '../model/AndroidData';
import {environment} from '../../environments/environment';
import {GeoSquare} from '../model/GeoSquare';
import {DepthMarker} from '../model/DepthMarker';

declare var TrackingService: JavaScriptInterface;

@Injectable({
    providedIn: 'root'
})
export class AndroidBridgeService {
    private readonly TrackingService: JavaScriptInterface;
    private androidDataList: Array<AndroidData> = new Array<AndroidData>()
    private androidListSendSize: number = environment.listSize;

    constructor() {
        if (typeof TrackingService === 'undefined') {
            console.log('TrackingService is undefined');
            this.TrackingService = new class implements JavaScriptInterface {
                findMarkers(s: string): string {
                    return '[{"depth":"3.14777","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14481","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14406","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14555","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14406","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14481","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14481","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14481","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14629","latitude":49.9582452,"longitude":36.3384585},{"depth":"3.14481","latitude":49.9580404,"longitude":36.3384029},{"depth":"3.14555","latitude":49.9580404,"longitude":36.3384029},{"depth":"3.14629","latitude":49.9580404,"longitude":36.3384029},{"depth":"3.14777","latitude":49.9579778,"longitude":36.3382165},{"depth":"3.14703","latitude":49.9579778,"longitude":36.3382165},{"depth":"3.14555","latitude":49.9579778,"longitude":36.3382165},{"depth":"3.14555","latitude":49.9579865,"longitude":36.3382168},{"depth":"3.14481","latitude":49.9579865,"longitude":36.3382168},{"depth":"3.14481","latitude":49.9579865,"longitude":36.3382168},{"depth":"3.14406","latitude":49.9580323,"longitude":36.3382749},{"depth":"3.14999","latitude":49.9580323,"longitude":36.3382749},{"depth":"3.14629","latitude":49.9580323,"longitude":36.3382749},{"depth":"3.14555","latitude":49.9580746,"longitude":36.3383187},{"depth":"3.14406","latitude":49.9580746,"longitude":36.3383187},{"depth":"3.14555","latitude":49.9580746,"longitude":36.3383187},{"depth":"3.14629","latitude":49.9581091,"longitude":36.3383654},{"depth":"3.14629","latitude":49.9581091,"longitude":36.3383654},{"depth":"3.14481","latitude":49.9581264,"longitude":36.3383966},{"depth":"3.14555","latitude":49.9581264,"longitude":36.3383966},{"depth":"3.14629","latitude":49.9581264,"longitude":36.3383966},{"depth":"3.14481","latitude":49.9581408,"longitude":36.3384053},{"depth":"3.14481","latitude":49.9581408,"longitude":36.3384053},{"depth":"3.14555","latitude":49.9581408,"longitude":36.3384053},{"depth":"3.14406","latitude":49.9581435,"longitude":36.3384056},{"depth":"3.14555","latitude":49.9581435,"longitude":36.3384056},{"depth":"3.14406","latitude":49.9581435,"longitude":36.3384056},{"depth":"3.14703","latitude":49.9581582,"longitude":36.338415},{"depth":"3.14629","latitude":49.9581582,"longitude":36.338415},{"depth":"3.14629","latitude":49.9581582,"longitude":36.338415},{"depth":"3.14332","latitude":49.9581636,"longitude":36.3384265},{"depth":"3.14481","latitude":49.9581636,"longitude":36.3384265},{"depth":"3.14332","latitude":49.9581636,"longitude":36.3384265},{"depth":"3.14481","latitude":49.9581664,"longitude":36.3384365},{"depth":"3.14777","latitude":49.9581664,"longitude":36.3384365},{"depth":"3.14406","latitude":49.9581664,"longitude":36.3384365},{"depth":"3.14629","latitude":49.9581718,"longitude":36.338444},{"depth":"3.14555","latitude":49.9581718,"longitude":36.338444},{"depth":"3.14406","latitude":49.9581718,"longitude":36.338444},{"depth":"3.14629","latitude":49.9581745,"longitude":36.3384495},{"depth":"3.14629","latitude":49.9581745,"longitude":36.3384495},{"depth":"3.14555","latitude":49.9581745,"longitude":36.3384495},{"depth":"3.14555","latitude":49.9581798,"longitude":36.3384518},{"depth":"3.14332","latitude":49.9581798,"longitude":36.3384518},{"depth":"3.14629","latitude":49.9581798,"longitude":36.3384518},{"depth":"3.14555","latitude":49.9581813,"longitude":36.3384557},{"depth":"3.14629","latitude":49.9581813,"longitude":36.3384557},{"depth":"3.14629","latitude":49.9581813,"longitude":36.3384557},{"depth":"3.14703","latitude":49.9581805,"longitude":36.3384588},{"depth":"3.01359","latitude":49.9581805,"longitude":36.3384588},{"depth":"3.14999","latitude":49.9581804,"longitude":36.3384635},{"depth":"3.14999","latitude":49.9581804,"longitude":36.3384635},{"depth":"3.14703","latitude":49.9581801,"longitude":36.3384631}]';
                }

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
        const data: AndroidData = new AndroidData(response.depth.toString(),
            response.batteryLevel.toString(),
            response.waterTemp.toString(),
            crd, String(Date.now()));
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


    getMarkers(geoSquare: GeoSquare): Array<DepthMarker> {
        const array = new Array<DepthMarker>();
        const markers = this.TrackingService.findMarkers(JSON.stringify(geoSquare));
        // const parse: Array<DepthMarker> = ;
        for (const parseElement of JSON.parse(markers) as Array<DepthMarker>) {
            array.push(new DepthMarker(parseElement.depth, parseElement.latitude, parseElement.longitude))
        }
        return array;
    }
}

