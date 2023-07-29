export class SonarData {
    depth: string;
    status: string;
    battery: string;
    temperature: string;
    error: string;

    constructor(depth: string, battery: string, temperature: string, error: string) {
        this.depth = depth;
        this.battery = battery;
        this.temperature = temperature;
        this.error = error;
    }
}
