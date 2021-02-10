import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ClientService} from '../service/client.service';
import {SonarClientData} from '../service/SonarClientData';

@Component({
    selector: 'app-canvas',
    templateUrl: './canvas.component.html',
    styleUrls: ['./canvas.component.css']
})
export class CanvasComponent implements OnInit, AfterViewInit {
    @ViewChild('canvasId', {static: true})
    canvas: ElementRef<HTMLCanvasElement>;
    @ViewChild('canvasWrapper', {static: true})
    canvasWrapper: ElementRef<HTMLDivElement>;
    private measures = new Array<SonarClientData>()

    private ctx: CanvasRenderingContext2D;

    constructor(private clientService: ClientService) {
        clientService.getSonarClientData().subscribe(data => {
            if (data.isSonarAvailable) {
// TODO change color
                if (data.isMeasureSuccess) {
                    if (this.measures.push(data) > this.canvas.nativeElement.width / 2) {
                        this.measures.shift()
                    }
                    this.updateCanvas();
                }
            }
        })
        clientService.startConnection();
    }

    ngOnInit(): void {
        this.ctx = this.canvas.nativeElement.getContext('2d');
        this.canvas.nativeElement.width = this.canvasWrapper.nativeElement.offsetWidth + 10
        this.canvas.nativeElement.height = this.canvasWrapper.nativeElement.offsetHeight
    }

    animate(): void {
        this.ctx.fillStyle = 'white';
        this.ctx.fillRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);
    }

    draw() {
        console.log(' draw canvas size: width+height ', this.ctx.canvas.width, this.ctx.canvas.height)
        // TODO dirty hack to rotate screen
        this.canvas.nativeElement.width = 100
        this.canvas.nativeElement.height = 100
    }

    ngAfterViewInit(): void {
        console.log('ngAfterViewInit offsetWidth', this.canvasWrapper.nativeElement.offsetWidth);
        console.log('ngAfterViewInit offsetH', this.canvasWrapper.nativeElement.offsetHeight)
        this.canvas.nativeElement.width = this.canvasWrapper.nativeElement.offsetWidth + 10
        this.canvas.nativeElement.height = this.canvasWrapper.nativeElement.offsetHeight
    }


    private updateCanvas() {
        const maxDepth = this.measures.map((measure) => {
            return measure.depth
        }).reduce((acc, shot) => {
            return acc < shot ? shot : acc
        });
        this.animate()
        this.ctx.beginPath();
        let x = this.ctx.canvas.width - (this.measures.length * 2)

        for (const measure of this.measures) {
            const y = ((this.ctx.canvas.height - this.ctx.canvas.height / 10) * measure.depth) / maxDepth
            this.ctx.lineTo(x, y)
            this.ctx.fillStyle = 'orange'
            this.ctx.fillRect(x, y, 2, 30)
            this.ctx.fillStyle = 'grey'
            this.ctx.fillRect(x, y + 30, 2, this.ctx.canvas.height - y)
            this.ctx.stroke()
            x += 2;
        }
        this.ctx.font = 'small-caps bold 80px/1 sans-serif'
        this.ctx.fillStyle = 'black'
        const currVal = this.measures[this.measures.length - 1];
        this.ctx.fillText(currVal.depth.toFixed(1) + ' m', this.ctx.canvas.width - 250, 85)
        this.ctx.font = 'small-caps bold 30px/1 sans-serif'
        this.ctx.fillText(currVal.waterTemp.toFixed(1) + ' c' + String.fromCharCode(176), this.ctx.canvas.width - 100, 120)
        this.ctx.fillText(currVal.batteryLevel + ' %', this.ctx.canvas.width - 100, 150)
    }
}
