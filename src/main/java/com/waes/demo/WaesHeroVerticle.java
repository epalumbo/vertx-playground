package com.waes.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class WaesHeroVerticle extends AbstractVerticle {

    private MongoClient mongoClient;

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new WaesHeroVerticle());
    }

    @Override
    public void start(Future<Void> callback) {
        mongoClient = MongoClient.createShared(vertx, new JsonObject());
        dropExistingCollection()
            .recover(throwable -> Future.succeededFuture())
            .compose(then -> createNewCollection())
            .compose(then -> startServer())
            .setHandler(async -> {
                if (async.succeeded()) {
                    callback.complete();
                    System.out.println("HTTP server listening on " + async.result().actualPort());
                } else {
                    callback.fail(async.cause());
                }
            });
    }

    private Future<HttpServer> startServer() {
        final Router router = Router.router(vertx);
        router
            .route()
            .handler(context -> {
                context.response().putHeader(CONTENT_TYPE, APPLICATION_JSON);
                context.next();
            })
            .failureHandler(context -> {
                context.failure().printStackTrace();
                context.next();
            });
        router.get("/heroes").handler(this::getAll);
        router.post("/heroes").handler(BodyHandler.create()).handler(this::create);
        final Future<HttpServer> future = Future.future();
        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(8888, future);
        return future;
    }

    private Future<Void> createNewCollection() {
        final Future<Void> create = Future.future();
        mongoClient.createCollection("heroes", create);
        return create;
    }

    private Future<Void> dropExistingCollection() {
        final Future<Void> drop = Future.future();
        mongoClient.dropCollection("heroes", drop);
        return drop;
    }

    @Override
    public void stop() {
        mongoClient.close();
    }

    private void create(RoutingContext context) {
        final WaesHero waesHero = context.getBodyAsJson().mapTo(WaesHero.class);
        waesHero.setId(UUID.randomUUID().toString());
        final JsonObject document = JsonObject.mapFrom(waesHero);
        mongoClient.insert("heroes", document, async -> {
            if (async.succeeded()) {
                context.response().setStatusCode(201).end(document.encode());
            } else {
                context.fail(async.cause());
            }
        });
    }

    private void getAll(RoutingContext context) {
        mongoClient.find("heroes", new JsonObject(), async -> {
            if (async.succeeded()) {
                final List<JsonObject> result = async.result();
                context.response().end(Json.encode(result));
            } else {
                context.fail(async.cause());
            }
        });
    }

}
