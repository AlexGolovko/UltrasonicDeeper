import {LatLngBounds} from 'leaflet';
import {Injectable} from '@angular/core';
import {MapCoordinates} from '../model/MapCoordinates';
import {AndroidBridgeService} from './android-bridge.service';

@Injectable({
  providedIn: 'root',
})
export class MapService {
  private minZoom = 19;

  constructor(private androidBridge: AndroidBridgeService) {
  }

  private static lon2tile(lon, zoom): number {
    return (Math.floor((lon + 180) / 360 * Math.pow(2, zoom)));
  }

  private static lat2tile(lat, zoom): number {
    return (Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2
      * Math.pow(2, zoom)));
  }

  public getTiles(bounds: LatLngBounds): MapCoordinates {
    const mapCoordinates = new MapCoordinates();
    mapCoordinates.topTile = MapService.lat2tile(bounds.getNorth(), this.minZoom);
    mapCoordinates.leftTile = MapService.lon2tile(bounds.getWest(), this.minZoom);
    mapCoordinates.bottomTile = MapService.lat2tile(bounds.getSouth(), this.minZoom);
    mapCoordinates.rightTile = MapService.lon2tile(bounds.getEast(), this.minZoom);
    return mapCoordinates;
  }

  // public getTileNumber(bounds: LatLngBounds) {
  //   const mapCoordinates = this.getTiles(bounds);
  //   const width = Math.abs(mapCoordinates.leftTile - mapCoordinates.rightTile) + 1;
  //   const height = Math.abs(mapCoordinates.topTile - mapCoordinates.bottomTile) + 1;
  //   console.log({
  //     topTile: mapCoordinates.topTile,
  //     leftTile: mapCoordinates.leftTile,
  //     bottomTile: mapCoordinates.bottomTile,
  //     rightTile: mapCoordinates.rightTile
  //   });
  //   return width * height;
  // }

  // downloadMap(bounds: LatLngBounds): Observable<string> {
  //   const tiles = this.getTileNumber(bounds);
  //   const downloadState = new BehaviorSubject<string>('0/' + tiles);
  //   const mapPromise = new Promise(() => {
  //     downloadState.next('0/' + tiles);
  //   });
  //   this.androidBridge.downloadMap(this.getTiles(bounds));
  //   return downloadState.asObservable();
  // }
}
