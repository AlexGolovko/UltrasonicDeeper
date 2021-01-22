import {MapCoordinates} from '../DTO/MapCoordinates';

export interface JavaScriptInterface {
  downloadMap(map: MapCoordinates): void

  saveTrackingList(data: string): void

  getActivity(): string;
}
