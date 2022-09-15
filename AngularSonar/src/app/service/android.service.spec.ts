import { TestBed } from '@angular/core/testing';

import { AndroidService } from './android.service';

describe('AndroidService', () => {
  let service: AndroidService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AndroidService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
