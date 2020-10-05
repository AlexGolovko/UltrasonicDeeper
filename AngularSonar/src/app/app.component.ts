import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClientService} from './service/client.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  public readonly title = 'SonarApp';
  public readonly fail = 'Too deep/shallow';
  public isAvailable: boolean;
  public isMeasureSuccess: boolean;

  constructor(private clientService: ClientService) {

  }

  ngOnInit(): void {
    this.clientService.getState().subscribe(value => {
        console.log('this.clientService.getState().subscribe(value => {' + value);
        this.isAvailable = value.isSonarAvailable;
        if (this.isAvailable === false) {
          this.isMeasureSuccess = true;
        } else {
          this.isMeasureSuccess = value.isMeasureSuccess;
        }
      }
    );
  }

  ngOnDestroy(): void {
  }
}



