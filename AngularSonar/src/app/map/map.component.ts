import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ClientService} from '../service/client.service';
import {GeoService} from '../service/geo.service';
import {Circle, Icon, LatLng, Map, Marker, TileLayer} from 'leaflet';
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


    constructor(private clientService: ClientService, private geoService: GeoService, private androidService: AndroidBridgeService) {
        /*https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png*/
        // 'assets/Tiles/{z}/{x}/{y}.png'
        this.tiles = new TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href=”http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
            subdomains: 'abc',
            minZoom: 13,
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
            if (this.marker != null) {
                this.marker.setIcon(new Icon({
                    iconUrl: value.isSonarAvailable ? value.isMeasureSuccess ? 'assets/arrowGreen.png' : 'assets/arrowYellow.png' : 'assets/arrowRed.png',
                    iconSize: [30, 30], // size of the icon
                    iconAnchor: [15, 15], // point of the icon which will correspond to marker's location
                }))
            }
        })
        this.geoService.watchPosition().subscribe(value => {
            this.crd = value
            if (this.marker != null) {
                const latlng = new LatLng(value.coords.latitude, value.coords.longitude)
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
        // let marker: Marker;
        // let circle: Circle;
        // const greenIcon = new Icon({
        //     iconUrl: 'assets/arrow.png',
        //     iconSize: [30, 30], // size of the icon
        //     iconAnchor: [10, 32], // point of the icon which will correspond to marker's location
        // });
        // this.geoService.getLocation().subscribe(value => {
        //     console.log(value.coords.latitude, value.coords.longitude, value.coords.accuracy)
        //     if (typeof marker !== 'undefined') {
        //         marker.remove()
        //         circle.remove()
        //     }
        //     // const divIcon = new DivIcon({
        //     //     className: 'my-div-icon',
        //     //     html: '<img class="my-div-image" src="assets/marker-icon-2x.png"/>' +
        //     //         '<span class="my-div-span">U R here</span>',
        //     //     iconSize: [50, 85]
        //     // })
        //     marker = new Marker(new LatLng(value.coords.latitude, value.coords.longitude), {
        //         icon: greenIcon
        //     })
        //     marker.addTo(this.map);
        //     circle = new Circle(new LatLng(value.coords.latitude, value.coords.longitude), {
        //         color: 'green',
        //         fillColor: '#82e70c',
        //         fillOpacity: 0.5,
        //         radius: (value.coords.accuracy / 2)
        //     })
        //     circle.addTo(this.map);
        // });
        this.map.on('locationfound', event => {
            console.log('locationfound')
            const arrowIcon = new Icon({
                iconUrl: this.isAvailable ? this.isMeasureSuccess ? 'assets/arrowGreen.png' : 'assets/arrowYellow.png' : 'assets/arrowRed.png',
                iconSize: [30, 30], // size of the icon
                iconAnchor: [15, 15], // point of the icon which will correspond to marker's location
            });
            const radius = event.accuracy / 2;
            this.marker = new Marker(event.latlng, {icon: arrowIcon});
            this.marker.addTo(this.map)
            //  .bindPopup('You are within ' + radius + ' meters from this point').openPopup();
            this.circle = new Circle(event.latlng, radius, {
                color: 'green'
            });
            this.circle.addTo(this.map);
        });
    }
}
