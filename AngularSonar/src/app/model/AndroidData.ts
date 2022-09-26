import {SonarData} from './SonarData';

export class AndroidData extends SonarData {
    public time: string;
    readonly accuracy: number;
    readonly altitude: number | null;
    readonly altitudeAccuracy: number | null;
    readonly heading: number | null;
    readonly latitude: number;
    readonly longitude: number;
    readonly speed: number | null;

    constructor(depth: string, battery: string, temperature: string, position: GeolocationPosition, date: string) {
        super(depth, battery, temperature);
        this.time = date;
        if (position) {
            this.accuracy = position.coords.accuracy;
            this.altitude = position.coords.altitude;
            this.altitudeAccuracy = position.coords.altitudeAccuracy;
            this.heading = position.coords.heading;
            this.latitude = position.coords.latitude;
            this.longitude = position.coords.longitude;
            this.speed = position.coords.speed;
        }
    }

}
