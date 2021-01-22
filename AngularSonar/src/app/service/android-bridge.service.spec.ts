import { TestBed } from '@angular/core/testing';

import { AndroidBridgeService } from './android-bridge.service';

describe('AndroidBridgeService', () => {
  let service: AndroidBridgeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AndroidBridgeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
