import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ClientService} from '../service/client.service';
import {GeoService} from '../service/geo.service';
import {Map, TileLayer} from 'leaflet';


@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit {
  private map: Map;
  private tiles: TileLayer;

  constructor(private clientService: ClientService, private geoService: GeoService) {

    /*https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png*/
    // 'assets/Tiles/{z}/{x}/{y}.png'
    this.tiles = new TileLayer('assets/Tiles/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href=”http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      subdomains: 'abc',
      minZoom: 13,
      maxZoom: 19,
      crossOrigin: true
    });

  }

  ngOnInit(): void {

  }

  ngAfterViewInit(): void {
    this.initMap();
    this.map.invalidateSize();

    // this.map.__initMapHandlers();
  }

  private initMap(): void {
    this.map = new Map('map', {
      center: [49.957943, 36.338340],
      zoom: 12,
      maxZoom: 19

    });
    this.tiles.addTo(this.map);
  }

  private saveMap() {

  }
}
