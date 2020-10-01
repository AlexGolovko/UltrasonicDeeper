import {LatLngBounds} from 'leaflet';
import {BehaviorSubject, Observable} from 'rxjs';

export class MapService {
  private minZoom = 19;

  private lon2tile(lon, zoom): number {
    return (Math.floor((lon + 180) / 360 * Math.pow(2, zoom)));
  }

  private lat2tile(lat, zoom): number {
    return (Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2
      * Math.pow(2, zoom)));
  }

  public getTileNumber(bounds: LatLngBounds) {
    const topTile = this.lat2tile(bounds.getNorth(), this.minZoom);
    const leftTile = this.lon2tile(bounds.getWest(), this.minZoom);
    const bottomTile = this.lat2tile(bounds.getSouth(), this.minZoom);
    const rightTile = this.lon2tile(bounds.getEast(), this.minZoom);
    const width = Math.abs(leftTile - rightTile) + 1;
    const height = Math.abs(topTile - bottomTile) + 1;

// total tiles
    return width * height;
  }

  downloadMap(bounds: LatLngBounds): Observable<string> {
    const tiles = this.getTileNumber(bounds);
    const downloadState = new BehaviorSubject<string>('0/' + tiles);
    const mapPromise = new Promise(() => {
      downloadState.next('0/' + tiles);
    });
    return downloadState.asObservable();
  }
}
