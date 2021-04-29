export interface JavaScriptInterface {
    downloadMap(tiles: string): void

    saveTrackingList(data: string): void

    getActivity(): string

    getMapCacheDir(): string

    findMarkers(data: string): string;
}
