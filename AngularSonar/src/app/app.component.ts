import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ActivityType, AndroidService} from './service/android.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {

    constructor(private router: Router, private androidService: AndroidService) {

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

    ngOnDestroy(): void {
    }
}


