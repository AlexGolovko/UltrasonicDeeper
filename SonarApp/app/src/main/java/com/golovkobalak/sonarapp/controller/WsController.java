package com.golovkobalak.sonarapp.controller;

import android.util.Log;
import io.javalin.Javalin;
import io.javalin.websocket.WsConnectContext;

import java.util.HashSet;
import java.util.Set;

public class WsController {

    public static final String TAG = WsController.class.getSimpleName();
    private Javalin app;

    private Set<WsConnectContext> sessions = new HashSet<>();

    public void start() {
        app = Javalin.create().start(7070);
        app.ws("/sonar", ws -> {
            ws.onConnect(ctx -> {
                Log.d(TAG, "connected");
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
                sessions.stream()
                        .filter(context -> context.session.isOpen())
                        .forEach(session -> session.send(message));
            });
        });
        Log.d(TAG, "ws started");
    }
}