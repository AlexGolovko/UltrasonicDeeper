import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {AppComponent} from './app.component';
import {ListViewModule} from '@syncfusion/ej2-angular-lists';
import {MapComponent} from './map/map.component';
import {ClientComponent} from './client/client.component';

// const appRoutes: Routes = [
//   {path: '', component: ClientComponent},
//   {path: 'map', component: MapComponent}
// ];

@NgModule({
  declarations: [
    AppComponent,
    MapComponent,
    ClientComponent
  ],
  imports: [
    BrowserModule,
    ListViewModule,
    // RouterModule.forRoot(appRoutes)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
