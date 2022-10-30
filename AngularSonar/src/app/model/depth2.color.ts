export class Depth2Color {
    depthList: Array<Depth2ColorEntry> = new Array<Depth2ColorEntry>()
    deepWater: Depth2ColorEntry
    private shallowWater: Depth2ColorEntry;

    constructor() {
        this.shallowWater = new Depth2ColorEntry(0, '#f7fbff')
        this.depthList.push(new Depth2ColorEntry(0, '#f7fbff'))
        this.depthList.push(new Depth2ColorEntry(1, '#deebf7'))
        this.depthList.push(new Depth2ColorEntry(2, '#c6dbef'))
        this.depthList.push(new Depth2ColorEntry(3, '#9ecae1'))
        this.depthList.push(new Depth2ColorEntry(4, '#6baed6'))
        this.depthList.push(new Depth2ColorEntry(5, '#4292c6'))
        this.depthList.push(new Depth2ColorEntry(6, '#2171b5'))
        this.depthList.push(new Depth2ColorEntry(7, '#08519c'))
        this.deepWater = new Depth2ColorEntry(8, '#08306b')
    }

    public getColor(d: string): string {
        const depth = parseInt(d, 10);
        return this.getColorNum(depth);
    }

    public getColorNum(depth: number): string {
        console.log('public getColorNum(depth: number):' + depth);
        if (depth >= this.deepWater.depth) {
            return this.deepWater.color;
        }
        const colorEntry = this.depthList.find((depth2ColorEntry: Depth2ColorEntry) => depth2ColorEntry.depth === depth);
        if (colorEntry === undefined) {
            return this.shallowWater.color;
        }
        return colorEntry.color;
    }
}

export class Depth2ColorEntry {
    depth: number
    color: string

    constructor(depth: number, color: string) {
        this.depth = depth;
        this.color = color;
    }
}
