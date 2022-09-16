import {Injectable} from '@angular/core';
import {JavaScriptInterface} from '../JavaInterface/JavaScriptInterface';
import {SonarClientData} from '../model/SonarClientData';
import {AndroidData} from '../model/AndroidData';
import {environment} from '../../environments/environment';
import {GeoSquare} from '../model/GeoSquare';
import {DepthMarker} from '../model/DepthMarker';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Activity} from '../model/Activity';

declare var TrackingService: JavaScriptInterface;

@Injectable({
    providedIn: 'root'
})
export class AndroidBridgeService {
    private androidDataList: Array<AndroidData> = new Array<AndroidData>()
    private androidListSendSize: number = environment.listSize;

    private http: HttpClient;
    private baseUrl: string;

    constructor(http: HttpClient) {
        this.http = http
        this.baseUrl = 'http://' + environment.androidHost + ':8080';
    }

    getActivity(): Observable<Activity> {
        return this.http.get<Activity>(this.baseUrl + '/activity', {observe: 'body', responseType: 'json'});
    }

    getMapCacheDir(): Observable<string> {
        return this.http.get<string>(this.baseUrl + '/system/mapCacheDir');
    }

    saveAndroidData(response: SonarClientData, crd: Position): void {
        const data: AndroidData = new AndroidData(response.depth.toString(),
            response.batteryLevel.toString(),
            response.waterTemp.toString(),
            crd, String(Date.now()));
        if (this.androidDataList.length > this.androidListSendSize) {
            this.http.post(this.baseUrl + '/tracking', JSON.stringify(this.androidDataList.splice(0)))
        }
        this.androidDataList.push(data);
    }

    getMarkers(geoSquare: GeoSquare): Observable<DepthMarker> {
        return new Observable((observer) => {
            const markerObservable = this.http.get<Array<DepthMarker>>(this.baseUrl + '/marker', {
                params: geoSquare.toHttpParams()
            });
            markerObservable.subscribe(resp => {
                resp.forEach(depthMarker => {
                    observer.next(new DepthMarker(depthMarker.depth, depthMarker.latitude, depthMarker.longitude))
                })
            })
        })
    }
}

