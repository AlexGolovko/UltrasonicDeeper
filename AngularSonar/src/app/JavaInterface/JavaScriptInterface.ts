export interface JavaScriptInterface {
  downloadMap(north: number, south: number, east: number, west: number): void

  saveTrackingList(data: string): void
}
