import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MapLoadComponent} from './map-load.component';

describe('MapLoadComponent', () => {
    let component: MapLoadComponent;
    let fixture: ComponentFixture<MapLoadComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [MapLoadComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(MapLoadComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
