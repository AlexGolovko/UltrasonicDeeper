import {AfterViewInit, Component, OnInit} from '@angular/core';
import {DivIcon, Icon, LatLng, Map, Marker, TileLayer} from 'leaflet';
import {MapService} from '../service/map.service';
import {GeoService} from '../service/geo.service';
import {AndroidBridgeService} from '../service/android-bridge.service';


@Component({
    selector: 'app-root',
    templateUrl: './map-load.component.html',
    styleUrls: ['./map-load.component.css']
})
export class MapLoadComponent implements OnInit, AfterViewInit {
    private map: Map;
    private tiles: TileLayer;
    private cachedTiles: TileLayer;
    downloadStatus: string;
    private longitude: number;
    private latitude: number;

    constructor(private mapService: MapService, private geoService: GeoService, private androidService: AndroidBridgeService) {
        /*https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png*/
        // 'assets/Tiles/{z}/{x}/{y}.png'
        this.tiles = new TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
            {
                attribution: '&copy; <a href=”http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
                subdomains: 'abc',
                minZoom: 5,
                maxZoom: 19,
                crossOrigin: true
            });
        this.cachedTiles = new TileLayer(androidService.getMapCacheDir() + '/{z}/{x}/{y}.png', {
            minZoom: 19,
            maxZoom: 19,
            crossOrigin: true
        });

    }

    ngOnInit(): void {
        window['java'] = this;
        this.downloadStatus = 'Download';
    }

    // download() {
    //     this.downloadStatus = 'In Progress';
    //     const bounds = this.map.getBounds();
    //     const north = bounds.getNorth();
    //     const south = bounds.getSouth();
    //     const east = bounds.getEast();
    //     const west = bounds.getWest();
    //     console.log(bounds);
    //     console.log('download= ' + this.mapService.getTileNumber(bounds));
    //     const observableDownloadState = this.mapService.downloadMap(bounds);
    //     observableDownloadState.subscribe(value => {
    //         this.downloadStatus = value;
    //     });
    // }

    ngAfterViewInit(): void {
        this.initMap();
    }

    private initMap(): void {
        this.latitude = 49.957943;
        this.longitude = 36.338340;
        this.geoService.getLocation().subscribe(value => {
            this.longitude = value.coords.longitude;
            this.latitude = value.coords.longitude;
        });

        this.map = new Map('map', {
            center: [this.latitude, this.longitude],
            zoom: 12,
            maxZoom: 19

        });
        this.tiles.addTo(this.map);
        this.cachedTiles.addTo(this.map)
        this.map.invalidateSize();
    }


    public getTilesFromJava(): string {
        const tiles = this.mapService.getTiles(this.map.getBounds());
        return JSON.stringify(tiles);
    }
}
