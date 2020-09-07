export class GeoService {
  public crd: Position;

  public geo(crd: Position): void {
    this.crd = crd;
    const options = {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    };
    console.log(crd);
    navigator.geolocation.watchPosition(position => {
      this.crd = position;
    }, err => {
      console.log(err);
      this.geo(crd);
    }, options);
  }
}
