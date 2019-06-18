package com.waes.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> callback) {
        vertx.deployVerticle(GreeterVerticle::new, new DeploymentOptions(), deployment -> {
            if (deployment.succeeded()) {
                vertx
                    .createHttpServer()
                    .requestHandler(this::handleRequest)
                    .listen(0, http -> {
                        if (http.succeeded()) {
                            callback.complete();
                            System.out.println("HTTP server started on port " + http.result().actualPort());
                        } else {
                            callback.fail(http.cause());
                        }
                    });
            } else {
                callback.fail(deployment.cause());
            }
        });
    }

    private void handleRequest(HttpServerRequest request) {
        final String name = request.getParam("name");
        final HttpServerResponse response = request.response();
        vertx
            .eventBus()
            .sender("greetings")
            .send(name, reply -> writeTo(response, reply));
    }

    private void writeTo(HttpServerResponse response, AsyncResult<Message<Object>> reply) {
        if (reply.succeeded()) {
            response
                .putHeader("content-type", "text/plain")
                .end(reply.result().body().toString());
        } else {
            response
                .setStatusCode(400)
                .end("not allowed: " + reply.cause().getMessage());
        }
    }

}

class GreeterVerticle extends AbstractVerticle {

    private static String instanceId = UUID.randomUUID().toString();

    @Override
    public void start(Future<Void> callback) {
        vertx
            .eventBus()
            .consumer("greetings")
            .handler(message -> {
                final String name = message.body().toString();
                if (name.equals("Evil")) {
                    message.fail(1, "Evil is not welcome here!");
                } else {
                    message.reply(new JsonObject()
                        .put("greeting", "Hello " + name)
                        .put("instance", instanceId));
                }
            })
            .completionHandler(callback);
    }

}
