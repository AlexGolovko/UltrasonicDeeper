import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AndroidBridgeService} from './service/android-bridge.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {

  constructor(private router: Router, private androidBridge: AndroidBridgeService) {

  }

  ngOnInit(): void {
    if (this.androidBridge.getActivity() === 'map') {
      this.router.navigate(['load']);
    }
  }

  ngOnDestroy(): void {
  }
}


