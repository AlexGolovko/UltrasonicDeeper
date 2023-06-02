import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Circle, Icon, LatLng, Map, Marker, TileLayer} from 'leaflet';
import {MapService} from '../service/map.service';
import {GeoService} from '../service/geo.service';
import {AndroidBridgeService} from '../service/android-bridge.service';
import {GeoSquare} from '../model/GeoSquare';
import {DepthMarker} from '../model/DepthMarker';
import {HashSet} from '../model/HashSet';
import {Depth2Color} from '../model/depth2.color';


@Component({
    selector: 'app-load',
    templateUrl: './map-load.component.html',
    styleUrls: ['./map-load.component.css']
})
export class MapLoadComponent implements OnInit, AfterViewInit {
    downloadStatus: string;
    private map: Map;
    private tiles: TileLayer;
    private cachedTiles: TileLayer;
    private readonly cachedMarkers = new HashSet<DepthMarker>();
    private depth2color: Depth2Color = new Depth2Color();

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
        androidService.getMapCacheDir().subscribe(response => {
            console.log('androidService.getMapCacheDir():' + response)
            this.cachedTiles = new TileLayer(response + '/{z}/{x}/{y}.png', {
                minZoom: 19,
                maxZoom: 19,
                crossOrigin: true
            });
            this.cachedTiles.addTo(this.map)
        })

    }

    ngOnInit(): void {
        window['java'] = this;
        this.downloadStatus = 'Download';
    }

    ngAfterViewInit(): void {
        this.initMap();
    }

    public getTilesFromJava(): string {
        const tiles = this.mapService.getTiles(this.map.getBounds());
        return JSON.stringify(tiles);
    }

    private initMap(): void {
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
        this.map.invalidateSize({debounceMoveend: true});
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


        this.map.on('moveend', () => {
            if (this.map.getZoom() > 16) {
                console.log('Moveend')
                console.log(this.map.getZoom())
                console.log(this.map.getBounds())
                this.updateMarkers()
            }
        });
    }

    private updateMarkers() {
        const latLngBounds = this.map.getBounds();
        const geoSquare = new GeoSquare(latLngBounds.getNorth(), latLngBounds.getEast(), latLngBounds.getSouth(), latLngBounds.getWest())
        this.androidService.getMarkers(geoSquare).subscribe(marker => {
            if (!this.cachedMarkers.contains(marker)) {
                this.cachedMarkers.add(marker);
                console.log('add: ' + marker)
                const depthCircle = new Circle(new LatLng(marker.latitude, marker.longitude), {
                    radius: 2,
                    color: this.depth2color.getColor(marker.depth),
                    weight: 2,
                    fillColor: this.depth2color.getColor(marker.depth),
                    fill: true,
                    opacity: 10,
                    fillOpacity: 100

                })
                depthCircle.addTo(this.map)
            }
        })
    }
}

