import {TestBed} from '@angular/core/testing';

import {WebSocketServiceImpl} from './websocket.service';

describe('WebSocketService', () => {
    let service: WebSocketServiceImpl;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(WebSocketServiceImpl);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
