import {HttpParams} from '@angular/common/http';

export class GeoSquare {
    public north: number
    public south: number
    public east: number
    public west: number

    constructor(north: number, east: number, south: number, west: number) {
        this.north = north
        this.east = east
        this.south = south
        this.west = west
    }

    toHttpParams(): HttpParams {
        return new HttpParams()
            .append('north', String(this.north))
            .append('east', String(this.east))
            .append('south', String(this.south))
            .append('west', String(this.west))
    }
}
