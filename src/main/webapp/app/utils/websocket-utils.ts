import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { Storage } from 'react-jhipster';
import { Observable, Subject } from 'rxjs';

let stompClient = null;
let connection: Promise<any>;
let connectedPromise: any = null;
let alreadyConnectedOnce = false;
const listeners: { [key: string]: Subject<any> } = {};

const createConnection = (): Promise<any> => new Promise(resolve => (connectedPromise = resolve));

export const connectWebSocket = () => {
  if (connectedPromise !== null || alreadyConnectedOnce) {
    return;
  }
  connection = createConnection();

  const loc = window.location;
  const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

  const headers = {};
  let url = '//' + loc.host + baseHref + '/websocket/tracker';
  const authToken = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
  if (authToken) {
    url += '?access_token=' + authToken;
  }
  const socket = new SockJS(url);
  stompClient = Stomp.over(socket, { protocols: ['v12.stomp'] });

  stompClient.connect(headers, () => {
    connectedPromise('success');
    connectedPromise = null;
    alreadyConnectedOnce = true;
  });
};

export const disconnectWebSocket = () => {
  if (stompClient !== null) {
    if (stompClient.connected) {
      stompClient.disconnect();
    }
    stompClient = null;
  }
  alreadyConnectedOnce = false;
};

export const subscribeToTopic = (topic: string): Observable<any> => {
  if (!listeners[topic]) {
    listeners[topic] = new Subject<any>();
    connection.then(() => {
      stompClient.subscribe(topic, data => {
        listeners[topic].next(JSON.parse(data.body));
      });
    });
  }
  return listeners[topic].asObservable();
};

export const unsubscribeFromTopic = (topic: string) => {
  if (listeners[topic]) {
    listeners[topic].complete();
    delete listeners[topic];
  }
};
