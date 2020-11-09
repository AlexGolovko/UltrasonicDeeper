export interface WsMessage<T> {
  event: string;
  data: T;
}

export enum WS {
  SONAR = 'sonar',
  IS_ALIVE = 'ping'
}

