import {Comparable} from './HashSet';

export class DepthMarker implements Comparable {
    readonly depth: string;
    readonly latitude: number;
    readonly longitude: number;

    constructor(depth: string, latitude: number, longitude: number) {
        this.depth = depth;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    equals(item: DepthMarker): boolean {
        if (item.depth !== this.depth) {
            return false
        }
        if (item.latitude !== this.latitude) {
            return false
        }
        return item.longitude === this.longitude;
    }

    hash(): number {
        let hash = 0;
        hash += this.longitude + this.latitude
        for (let i = 0; i < this.depth.length; i++) {
            const character = this.depth.charCodeAt(i);
            hash = ((hash << 5) - hash) + character;
            hash = hash & hash; // Convert to 32bit integer
        }
        return hash;
    }
}
