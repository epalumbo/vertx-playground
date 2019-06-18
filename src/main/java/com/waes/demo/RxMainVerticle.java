package com.waes.demo;

import io.reactivex.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;

import java.util.UUID;

public class RxMainVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        return Completable.mergeArray(deployConsumer(), startServer());
    }

    private Completable deployConsumer() {
        return vertx
            .rxDeployVerticle(RxGreeterVerticle::new, new DeploymentOptions())
            .ignoreElement();
    }

    private Completable startServer() {
        return vertx
            .createHttpServer()
            .requestHandler(this::handleRequest)
            .rxListen(0)
            .doOnSuccess(started -> System.out.println("HTTP server listening on " + started.actualPort()))
            .ignoreElement();
    }

    private void handleRequest(HttpServerRequest request) {
        final HttpServerResponse response = request.response();
        vertx
            .eventBus()
            .rxSend("greetings", request.getParam("name"))
            .subscribe(
                reply -> response.end(reply.body().toString()),
                throwable -> response.setStatusCode(500).end(throwable.getMessage()));
    }

}

class RxGreeterVerticle extends AbstractVerticle {

    private static String instanceId = UUID.randomUUID().toString();

    @Override
    public Completable rxStart() {
        final MessageConsumer<Object> consumer = vertx.eventBus().consumer("greetings");
        consumer.handler(this::greet);
        return consumer.rxCompletionHandler();
    }

    private void greet(Message<Object> message) {
        final String name = message.body().toString();
        if (name.equals("Evil")) {
            message.fail(1, "Evil is not welcome here!");
        } else {
            message.reply(new JsonObject()
                .put("greeting", "Hello " + name + "!")
                .put("instance", instanceId));
        }
    }

}
