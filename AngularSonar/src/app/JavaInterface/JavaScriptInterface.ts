export interface JavaScriptInterface {
    downloadMap(tiles: string): void

    saveTrackingList(data: string): void

    getMapCacheDir(): string

    findMarkers(data: string): string;
}
