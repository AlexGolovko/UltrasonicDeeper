import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';

const GEOLOCATION_ERRORS = {
    'errors.location.unsupportedBrowser': 'Browser does not support location services',
    'errors.location.permissionDenied': 'You have rejected access to your location',
    'errors.location.positionUnavailable': 'Unable to determine your location',
    'errors.location.timeout': 'Service timeout has been reached'
};

@Injectable({
    providedIn: 'root',
})
export class GeoService {
    private watcher: Observable<GeolocationPosition> = null

    public getLocation(geoLocationOptions?: any): Observable<GeolocationPosition> {
        geoLocationOptions = geoLocationOptions || {timeout: 5000};

        return Observable.create(observer => {

            if (window.navigator && window.navigator.geolocation) {
                window.navigator.geolocation.getCurrentPosition(
                    (position) => {
                        observer.next(position);
                        observer.complete();
                    },
                    (error) => {
                        switch (error.code) {
                            case 1:
                                observer.error(GEOLOCATION_ERRORS['errors.location.permissionDenied']);
                                break;
                            case 2:
                                observer.error(GEOLOCATION_ERRORS['errors.location.positionUnavailable']);
                                break;
                            case 3:
                                observer.error(GEOLOCATION_ERRORS['errors.location.timeout']);
                                break;
                        }
                    },
                    geoLocationOptions);
            } else {
                observer.error(GEOLOCATION_ERRORS['errors.location.unsupportedBrowser']);
            }

        });


    }

    public watchPosition(): Observable<GeolocationPosition> {
        if (this.watcher == null) {
            this.watcher = new Observable<GeolocationPosition>(observer => {
                window.navigator.geolocation.watchPosition(position => {
                    observer.next(position)
                }, error => {
                    console.log(error)
                }, {
                    timeout: 5000,
                    enableHighAccuracy: true,
                    maximumAge: 0
                })
            })
        }
        return this.watcher
    }
}
