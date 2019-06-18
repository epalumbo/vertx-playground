package com.waes.demo;

import io.vertx.core.Vertx;

import java.text.MessageFormat;

import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.currentThread;
import static java.util.stream.IntStream.rangeClosed;

public class NoVerticleApplication {

    private final Vertx vertx;

    private NoVerticleApplication(Vertx vertx) {
        this.vertx = vertx;
    }

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        final NoVerticleApplication application = new NoVerticleApplication(vertx);
        rangeClosed(1, getRuntime().availableProcessors()).forEach(application::startServer);
    }

    private void startServer(final int number) {
        vertx
            .createHttpServer()
            .requestHandler(req -> {
                final String message = MessageFormat.format(
                    "Hello from server {0} running on thread {1}",
                    number, currentThread().getName());
                req.response().end(message);
            })
            .listen(8888);
    }

}
