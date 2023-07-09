package com.golovkobalak.sonarapp.controller;

import android.util.Log;
import com.golovkobalak.sonarapp.service.TrackingService;
import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;
import okhttp3.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WsController {

    public static final String TAG = WsController.class.getSimpleName();
    public static final String ERROR_SONAR_IS_NOT_SEND_MESSAGE = "{\"error\":\"Sonar is not send message\"}";
    private static final ScheduledExecutorService errorNotifier = new ScheduledThreadPoolExecutor(1);
    private Javalin app;
    private TrackingService trackingService = new TrackingService();

    private final Set<WsConnectContext> sessions = new HashSet<>();
    private Set<ScheduledFuture> tasks = new HashSet<>();
    private WebSocket webSocket;

    public void execute() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://192.168.4.1:5000/sonar")
                .build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // WebSocket connection successful
                Log.d(TAG, "ws client connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(TAG, LocalTime.now() + " onMessage: " + text);
                scheduleErrorNotification();
                sendMessageToClients(text);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "onClosed: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d(TAG, "onFailure: " + t);
                reconnect();
            }
        });
    }

    private void reconnect() {
        // Delay the reconnection attempt using a timer
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Attempt to reconnect");
                execute(); // Attempt to reconnect
            }
        }, 500);
    }

    public void start() {
        execute();
        app = Javalin.create(config -> {
            QueuedThreadPool threadPool = new QueuedThreadPool(4, 1);
            config.server(() -> new Server(threadPool));
            config.enableDevLogging();
        });
        app.ws("/sonar", ws -> {
            ws.onConnect(ctx -> {
                Log.d(TAG, "connected");
                if ("192.168.4.1".equals(ctx.session.getRemote().getInetSocketAddress().getHostString())) {
                    return;
                }
                scheduleErrorNotification();
                sessions.add(ctx);
            });
            ws.onClose(ctx -> {
                Log.d(TAG, "closed");
                sessions.remove(ctx);
            });
        });
        app.start(7070);
        Log.d(TAG, "ws started");
    }

    private void sendMessageToClients(String message) {
        sessions.stream()
                .filter(context -> context.session.isOpen())
                .forEach(session -> session.send(message));
    }

    private void scheduleErrorNotification() {
        tasks.forEach(task -> task.cancel(true));
        tasks.clear();
        tasks.add(errorNotifier.schedule(this::sendErrorMessageToOtherClients, 1, TimeUnit.SECONDS));
    }

    private void sendErrorMessageToOtherClients() {
        sessions.stream()
                .filter(context -> context.session.isOpen())
                .forEach(session -> {
                    session.send(ERROR_SONAR_IS_NOT_SEND_MESSAGE);
                });
    }


    public void destroy() {
        try {
            app.close();
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }
}