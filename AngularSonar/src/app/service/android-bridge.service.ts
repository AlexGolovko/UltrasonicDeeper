import {Injectable} from '@angular/core';
import {SonarClientData} from '../model/SonarClientData';
import {AndroidData} from '../model/AndroidData';
import {environment} from '../../environments/environment';
import {GeoSquare} from '../model/GeoSquare';
import {DepthMarker} from '../model/DepthMarker';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';


@Injectable({
    providedIn: 'root'
})
export class AndroidBridgeService {

    private http: HttpClient;
    private baseUrl: string;

    constructor(http: HttpClient) {
        this.http = http
        this.baseUrl = 'http://' + environment.androidHost + ':8080';
    }

    getMapCacheDir(): Observable<string> {
        return this.http.get<string>(this.baseUrl + '/system/mapCacheDir');
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

