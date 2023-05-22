package com.golovkobalak.sonarapp.controller;

import android.util.Log;
import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WsController {

    public static final String TAG = WsController.class.getSimpleName();
    public static final String ERROR_SONAR_IS_NOT_SEND_MESSAGE = "{\"error\":\"Sonar is not send message\"}";
    private static final ScheduledExecutorService errorNotifier = new ScheduledThreadPoolExecutor(1);
    private Javalin app;

    private Set<WsConnectContext> sessions = new HashSet<>();
    private Set<ScheduledFuture> tasks = new HashSet<>();

    public void start() {
        app = Javalin.create().start(7070);
        app.ws("/sonar", ws -> {
            ws.onConnect(ctx -> {
                Log.d(TAG, "connected");
                scheduleErrorNotification();
                if ("192.168.4.1".equals(ctx.session.getRemote().getInetSocketAddress().getHostString())) {
                    return;
                }
                sessions.add(ctx);
            });
            ws.onClose(ctx -> {
                Log.d(TAG, "closed");
                sessions.remove(ctx);
            });
            ws.onMessage(ctx -> {
                String message = ctx.message();
                Log.d(TAG, "onMessage" + message);
                if ("192.168.4.1".equals(ctx.session.getRemote().getInetSocketAddress().getHostString())) {
                    scheduleErrorNotification();
                    sessions.stream()
                            .filter(context -> context.session.isOpen())
                            .forEach(session -> session.send(message));
                }
            });
        });
        Log.d(TAG, "ws started");
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