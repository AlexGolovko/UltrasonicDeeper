export class SonarData {
    depth: string;
    status: string;
    battery: string;
    temperature: string;

    constructor(depth: string, battery: string, temperature: string) {
        this.depth = depth;
        this.battery = battery;
        this.temperature = temperature;
    }
}
