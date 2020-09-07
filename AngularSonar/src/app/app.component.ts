import {Component, OnInit} from '@angular/core';
import {SonarState} from './SonarState';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  public readonly title = 'SonarApp';
  public readonly fail = 'Too deep/shallow';
  public isAvailable: boolean;
  public isMeasureSuccess: boolean;

  ngOnInit(): void {
    this.isAvailable = false;
    this.isMeasureSuccess = true;
  }

  onStatusUpdated(sonarState: SonarState) {
    this.isAvailable = sonarState.isSonarAvailable;
    if (this.isAvailable === false) {
      this.isMeasureSuccess = true;
    } else {
      this.isMeasureSuccess = sonarState.isMeasureSuccess;
    }
  }
}



