import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Circle, Icon, LatLng, Map, Marker, TileLayer} from 'leaflet';
import {MapService} from '../service/map.service';
import {GeoService} from '../service/geo.service';
import {AndroidBridgeService} from '../service/android-bridge.service';


@Component({
    selector: 'app-load',
    templateUrl: './map-load.component.html',
    styleUrls: ['./map-load.component.css']
})
export class MapLoadComponent implements OnInit, AfterViewInit {
    private map: Map;
    private tiles: TileLayer;
    private cachedTiles: TileLayer;
    downloadStatus: string;

    constructor(private mapService: MapService, private geoService: GeoService, private androidService: AndroidBridgeService) {
        /*https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png*/
        // 'assets/Tiles/{z}/{x}/{y}.png'
        this.tiles = new TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
            {
                attribution: '&copy; <a href=”http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
                subdomains: 'abc',
                minZoom: 12,
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
        // this.latitude = 49.957943;
        // this.longitude = 36.338340;
        const subscription = this.geoService.getLocation().subscribe(value => {
            this.map.setView(new LatLng(value.coords.latitude, value.coords.longitude), 12)
            subscription.unsubscribe();
        })
        this.map = new Map('map', {
            minZoom: 12,
            zoom: 15,
            maxZoom: 19
        });
        this.tiles.addTo(this.map);
        this.cachedTiles.addTo(this.map)
        this.map.invalidateSize();

        // TODO new marker
        // L.circle(new LatLng(this.latitude, this.longitude), {
        //     color: 'red',
        //     fillColor: '#f03',
        //     fillOpacity: 0.5,
        //     radius: 500
        // }).addTo(this.map);
        const greenIcon = new Icon({
            iconUrl: 'assets/marker-icon-2x.png',
            iconSize: [20, 32], // size of the icon
            iconAnchor: [10, 32], // point of the icon which will correspond to marker's location
        });
        let marker: Marker;
        let circle: Circle;
        this.geoService.getLocation().subscribe(value => {
            console.log(value.coords.latitude, value.coords.longitude, value.coords.accuracy)
            if (typeof marker !== 'undefined') {
                marker.remove()
                circle.remove()
            }
            // const divIcon = new DivIcon({
            //     className: 'my-div-icon',
            //     html: '<img class="my-div-image" src="assets/marker-icon-2x.png"/>' +
            //         '<span class="my-div-span">U R here</span>',
            //     iconSize: [50, 85]
            // })
            marker = new Marker(new LatLng(value.coords.latitude, value.coords.longitude), {
                icon: greenIcon
            })
            marker.addTo(this.map);
            circle = new Circle(new LatLng(value.coords.latitude, value.coords.longitude), {
                color: 'green',
                fillColor: '#82e70c',
                fillOpacity: 0.5,
                radius: (value.coords.accuracy / 2)
            })
            circle.addTo(this.map);
        });
    }


    public getTilesFromJava(): string {
        const tiles = this.mapService.getTiles(this.map.getBounds());
        return JSON.stringify(tiles);
    }
}
