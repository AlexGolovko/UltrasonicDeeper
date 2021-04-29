export class GeoSquare {
    constructor(north: number, east: number, south: number, west: number) {
        this.north = north
        this.east = east
        this.south = south
        this.west = west
    }

    public north: number
    public south: number
    public east: number
    public west: number
}
