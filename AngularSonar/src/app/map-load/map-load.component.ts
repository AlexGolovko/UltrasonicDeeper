import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Map, TileLayer} from 'leaflet';
import {JavaScriptInterface} from '../JavaInterface/JavaScriptInterface';
import {MapService} from '../service/map.service';


@Component({
  selector: 'app-map-load',
  templateUrl: './map-load.component.html',
  styleUrls: ['./map-load.component.css'],
  providers: [MapService]
})
export class MapLoadComponent implements OnInit, AfterViewInit {
  private map: Map;
  private tiles: TileLayer;
  private inter: JavaScriptInterface;
  downloadStatus: string;

  constructor(private mapService: MapService) {
    /*https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png*/
    // 'assets/Tiles/{z}/{x}/{y}.png'
    this.tiles = new TileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        attribution: '&copy; <a href=”http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        subdomains: 'abc',
        minZoom: 13,
        maxZoom: 19,
        crossOrigin: true
      });

  }

  ngOnInit(): void {
    this.downloadStatus = 'Download';
  }

  download() {
    this.downloadStatus = 'In Progress';
    const bounds = this.map.getBounds();
    const north = bounds.getNorth();
    const south = bounds.getSouth();
    const east = bounds.getEast();
    const west = bounds.getWest();
    console.log('download= ' + this.mapService.getTileNumber(bounds));
    const observableDownloadState = this.mapService.downloadMap(bounds);
    observableDownloadState.subscribe(value => {
      this.downloadStatus = value;
    });

  }

  ngAfterViewInit(): void {
    this.initMap();
    this.map.invalidateSize();
    // const control = L.control.saveTiles(this.tiles, {
    //   saveButtonHtml: '<i class="fa fa-download" aria-hidden="true"></i>'
    // });

    // L.control.
    // control.addTo(this.map);
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


}
