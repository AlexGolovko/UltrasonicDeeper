import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ActivityType} from './model/Activity';
import {AndroidBridgeService} from './service/android-bridge.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

    constructor(private router: Router, private androidService: AndroidBridgeService) {

    }

    ngOnInit(): void {
        this.androidService.getActivity().subscribe(activity => {
            if (activity.activity === ActivityType.LOAD) {
                this.router.navigate(['load']);
            }
            if (activity.activity === ActivityType.MAP) {
                this.router.navigate(['map']);
            }
        })
    }
}


