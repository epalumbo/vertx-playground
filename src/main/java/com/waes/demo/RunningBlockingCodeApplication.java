package com.waes.demo;

import io.vertx.core.Vertx;

import static java.lang.Thread.currentThread;

public class RunningBlockingCodeApplication {

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.executeBlocking(
            callback -> {
                try {
                    System.out.println("About to sleep a bit / " + currentThread().getName());
                    Thread.sleep(1000); // blocking!
                    callback.complete();
                } catch (Exception e) {
                    callback.fail(e);
                }
            },
            async -> {
                if (async.succeeded()) {
                    System.out.println("Beautiful rest / " + currentThread().getName());
                } else {
                    System.err.println("Wanna sleep! Let me sleep!");
                }
            });
    }

}
