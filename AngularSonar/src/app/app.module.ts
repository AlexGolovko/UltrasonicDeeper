import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {AppComponent} from './app.component';
import {ListViewModule} from '@syncfusion/ej2-angular-lists';
import {MapComponent} from './map/map.component';
import {ClientComponent} from './client/client.component';
import {MapLoadComponent} from './map-load/map-load.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';
import {CanvasComponent} from './canvas/canvas.component';
import {HttpClientModule} from '@angular/common/http';
import {IMqttServiceOptions, MqttModule,} from 'ngx-mqtt';
import {environment} from "../environments/environment";

const appRoutes: Routes = [
    {path: '', component: ClientComponent},
    {path: 'map', component: MapComponent},
    {path: 'load', component: MapLoadComponent},
    {path: 'chart', component: CanvasComponent}
];
export const MQTT_SERVICE_OPTIONS: IMqttServiceOptions = {
    hostname: environment.mqttHostname,
    port: 9001,
    // path: '/mqtt'
};

@NgModule({
    declarations: [
        AppComponent,
        MapComponent,
        ClientComponent,
        MapLoadComponent,
        CanvasComponent
    ],
    imports: [
        BrowserModule,
        ListViewModule,
        RouterModule.forRoot(appRoutes),
        LeafletModule,
        HttpClientModule,
        MqttModule.forRoot(MQTT_SERVICE_OPTIONS
        ),

    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
