import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AndroidService {

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
}

export enum ActivityType {
    LOAD = 'LOAD', MAP = 'MAP'
}

export class Activity {
    public activity: ActivityType;
}
