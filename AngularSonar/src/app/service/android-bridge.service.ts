import {Injectable} from '@angular/core';
import {JavaScriptInterface} from '../JavaInterface/JavaScriptInterface';
import {MapCoordinates} from '../DTO/MapCoordinates';

declare var TrackingService: JavaScriptInterface;

@Injectable({
  providedIn: 'root'
})
export class AndroidBridgeService {
  private readonly TrackingService: JavaScriptInterface;

  constructor() {
    if (typeof TrackingService === 'undefined') {
      this.TrackingService = new class implements JavaScriptInterface {
        downloadMap(map: MapCoordinates): void {
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

  downloadMap(tiles: MapCoordinates) {
    this.TrackingService.downloadMap(tiles);
  }
}

