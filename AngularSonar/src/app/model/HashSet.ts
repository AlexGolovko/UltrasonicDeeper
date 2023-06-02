export interface Hashable {
    hash(): number
}

export interface Comparable extends Hashable {
    equals(obj: Comparable): boolean
}

export class HashSet<T extends Comparable> {
    private bucketSize = 32
    private buckets = new Array<Array<T>>()

    constructor() {
        for (let i = 0; i < this.bucketSize; i++) {
            this.buckets.push(new Array<T>());
        }
    }

    public add(item: T): void {
        const bucketId = Math.abs(item.hash() % this.bucketSize);
        const bucket = this.buckets[bucketId];
        for (const t of bucket) {
            if (item.equals(t)) {
                return;
            }
        }
        bucket.push(item)
    }

    public contains(item: T): boolean {
        const bucketId = Math.abs(item.hash() % this.bucketSize);
        const bucket = this.buckets[bucketId];
        for (const t of bucket) {
            if (item.equals(t)) {
                return true
            }
        }
        return false
    }
}
