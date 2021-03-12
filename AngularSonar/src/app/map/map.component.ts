import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ClientService} from '../service/client.service';
import {GeoService} from '../service/geo.service';
import {Circle, DivIcon, Icon, LatLng, Map, Marker, TileLayer} from 'leaflet';
import {AndroidBridgeService} from '../service/android-bridge.service';


@Component({
    selector: 'app-map',
    templateUrl: './map.component.html',
    styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit {
    private map: Map;
    private tiles: TileLayer;
    private cachedTiles: TileLayer;
    private isAvailable = false;
    private isMeasureSuccess = false;
    private marker: Marker = null
    private circle: Circle;
    private crd: Position;
    public angle = -90
    private prevLatLng: LatLng = null
    private currLatLng: LatLng = null
    private prevArrowIconHtml: string;


    constructor(private clientService: ClientService, private geoService: GeoService, private androidService: AndroidBridgeService) {
        /*https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png*/
        // 'assets/Tiles/{z}/{x}/{y}.png'
        this.tiles = new TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href=”http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
            subdomains: 'abc',
            minZoom: 15,
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
        this.map = new Map('map', {
            center: [49.957943, 36.338340],
            zoom: 19,
            maxZoom: 19

        });
        this.clientService.getSonarClientData().subscribe(value => {
            if (value.isMeasureSuccess) {
                this.androidService.saveAndroidData(value, this.crd)
            }
            if (this.marker !== null) {
                const arrowAngle = this.getAngle(this.prevLatLng, this.currLatLng)
                const arrowIcon: string = value.isSonarAvailable ? value.isMeasureSuccess ? 'assets/arrowGreen.png' : 'assets/arrowYellow.png' : 'assets/arrowRed.png'
                const arrowIconHtml = '<img class="leaflet-marker-icon leaflet-zoom-animated" src="' + arrowIcon + '"  ' +
                    'style="width: 30px; height: 30px;transform: rotate(' + arrowAngle + 'deg);' +
                    '  -webkit-transform: rotate(' + arrowAngle + 'deg);' +
                    ' -moz-transform:rotate(' + arrowAngle + 'deg);' +
                    'transform-origin: 50% 50%" />'
                if (this.prevArrowIconHtml === arrowIconHtml) {
                    return
                } else {
                    this.prevArrowIconHtml = arrowIconHtml
                }
                const divIcon = new DivIcon({
                    className: 'my-div-icon',
                    html: arrowIconHtml,
                    iconSize: [30, 30],
                    iconAnchor: [15, 15]
                })
                this.marker.setIcon(divIcon)
            }
        })
        this.geoService.watchPosition().subscribe(value => {
            this.crd = value
            if (this.marker !== null) {
                const latlng = new LatLng(value.coords.latitude, value.coords.longitude)
                if (this.currLatLng == null) {
                    this.currLatLng = latlng;
                    this.prevLatLng = latlng
                } else {
                    this.prevLatLng = this.currLatLng
                    this.currLatLng = latlng
                }
                this.marker.setLatLng(latlng)
                this.circle.setLatLng(latlng)
                this.circle.setRadius(value.coords.accuracy / 2)
                this.map.panTo(latlng)

            }
        })
    }

    ngAfterViewInit(): void {
        this.initMap();
        this.map.invalidateSize();
    }

    private initMap(): void {
        this.tiles.addTo(this.map)
        this.cachedTiles.addTo(this.map)
        this.map.locate({setView: true, enableHighAccuracy: true});
        this.map.on('locationfound', event => {

            const divIcon = new DivIcon({
                className: 'my-div-icon',
                html: '<img class="leaflet-marker-icon leaflet-zoom-animated" src="assets/arrowGreen.png"  style="width: 30px; height: 30px;transform: rotate(-45deg);  -webkit-transform: rotate(-45deg); -moz-transform:rotate(-45deg);transform-origin: 50% 50%" />',
                iconSize: [30, 30],
                iconAnchor: [15, 15]
            })
            const radius = event.accuracy / 2;
            this.marker = new Marker(event.latlng, {icon: divIcon});
            this.marker.addTo(this.map)
            this.circle = new Circle(event.latlng, radius, {
                color: 'green'
            });
            this.circle.addTo(this.map);
        });
    }

    public getAngle(from: LatLng, to: LatLng): number {
        let angle: number;
        angle = (Math.atan2(to.lat - from.lat, to.lng - from.lng) * 180 / Math.PI - 90) * -1
        if (angle > 180) {
            angle = (angle - 180) * -1
        }
        // Arrow already rotated at 45deg
        return angle - 45
    }
}
