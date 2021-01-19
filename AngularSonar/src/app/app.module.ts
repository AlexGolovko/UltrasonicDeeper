import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {AppComponent} from './app.component';
import {ListViewModule} from '@syncfusion/ej2-angular-lists';
import {MapComponent} from './map/map.component';
import {ClientComponent} from './client/client.component';
import {MapLoadComponent} from './map-load/map-load.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';


const appRoutes: Routes = [
  {path: '', component: ClientComponent},
  {path: 'map', component: MapComponent},
  {path: 'map-load', component: MapLoadComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    MapComponent,
    ClientComponent,
    MapLoadComponent
  ],
  imports: [
    BrowserModule,
    ListViewModule,
    RouterModule.forRoot(appRoutes),
    LeafletModule

  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
