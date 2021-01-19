import {Injectable, OnDestroy} from '@angular/core';
import {WebSocketSubject, WebSocketSubjectConfig} from 'rxjs/internal-compatibility';
import {interval, Observable, Observer, Subject, SubscriptionLike} from 'rxjs';
import {distinctUntilChanged, filter, map, share, takeWhile} from 'rxjs/operators';
import {WsMessage} from './wsmessage';
import {WebSocketConfig} from './websoketconfig';

export interface WebsocketService {
  status: Observable<boolean>;

  on<T>(event: string): Observable<T>;

  send(event: string, data: any): void;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketServiceImpl implements WebsocketService, OnDestroy {

  private config: WebSocketSubjectConfig<WsMessage<any>>;

  private websocketSub: SubscriptionLike;
  private statusSub: SubscriptionLike;

  private reconnection$: Observable<number>;
  private websocket$: WebSocketSubject<WsMessage<any>>;
  private connection$: Observer<boolean>;
  private wsMessages$: Subject<WsMessage<any>>;

  private reconnectInterval: number;
  private readonly reconnectAttempts: number;
  private isConnected: boolean;


  public status: Observable<boolean>;

  constructor(private wsConfig: WebSocketConfig) {
    this.wsMessages$ = new Subject<WsMessage<any>>();
    this.reconnectInterval = wsConfig.reconnectInterval || 5000; // pause between connections
    this.reconnectAttempts = wsConfig.reconnectAttempts || 10; // number of connection attempts

    this.config = {
      url: wsConfig.url,
      closeObserver: {
        next: (event: CloseEvent) => {
          this.websocket$ = null;
          this.connection$.next(false);
        }
      },
      openObserver: {
        next: (event: Event) => {
          console.log('WebSocket connected!');
          this.connection$.next(true);
        }
      }
    };

    // connection status
    this.status = new Observable<boolean>((observer) => {
      this.connection$ = observer;
    }).pipe(share(), distinctUntilChanged());

    // run reconnect if not connection
    this.statusSub = this.status
      .subscribe((isConnected) => {
        this.isConnected = isConnected;

        if (!this.reconnection$ && typeof (isConnected) === 'boolean' && !isConnected) {
          this.reconnect();
        }
      });

    this.websocketSub = this.wsMessages$.subscribe(
      null, (error: ErrorEvent) => console.error('WebSocket error!', error)
    );

    this.connect();
  }

  ngOnDestroy() {
    this.websocketSub.unsubscribe();
    this.statusSub.unsubscribe();
  }


  /*
  * connect to WebSocked
  * */
  private connect(): void {
    this.websocket$ = new WebSocketSubject(this.config);

    this.websocket$.subscribe(
      (message) => this.wsMessages$.next(message),
      (error: Event) => {
        if (!this.websocket$) {
          // run reconnect if errors
          this.reconnect();
        }
      });
  }


  /*
  * reconnect if not connecting or errors
  * */
  private reconnect(): void {
    this.reconnection$ = interval(this.reconnectInterval)
      .pipe(takeWhile((v, index) => index < this.reconnectAttempts && !this.websocket$));

    this.reconnection$.subscribe(
      () => this.connect(),
      null,
      () => {
        // Subject complete if reconnect attempts ending
        this.reconnection$ = null;

        if (!this.websocket$) {
          this.wsMessages$.complete();
          this.connection$.complete();
        }
      });
  }


  /*
  * on message event
  * */
  public on<T>(event: string): Observable<T> {
    if (event) {
      return this.wsMessages$.pipe(
        filter((message: WsMessage<T>) => message.event === event),
        map((message: WsMessage<T>) => message.data)
      );
    }
  }


  /*
  * on message to server
  * */
  public send(event: string, data: any = {}): void {
    if (event && this.isConnected) {
      this.websocket$.next(JSON.stringify({event, data}) as any);
    } else {
      console.error('Send error!');
    }
  }

}

