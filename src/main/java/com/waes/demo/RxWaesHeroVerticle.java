package com.waes.demo;

import io.reactivex.Completable;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

import java.util.UUID;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class RxWaesHeroVerticle extends AbstractVerticle {

    private MongoClient mongoClient;

    @Override
    public Completable rxStart() {
        mongoClient = MongoClient.createShared(vertx, new JsonObject());
        return mongoClient
            .rxDropCollection("heroes")
            .onErrorComplete()
            .andThen(mongoClient.rxCreateCollection("heroes"))
            .andThen(startServer());
    }

    private Completable startServer() {
        final Router router = Router.router(vertx);
        router
            .route()
            .handler(context -> {
                context.response().putHeader(CONTENT_TYPE, APPLICATION_JSON);
                context.next();
            });
        router
            .get("/heroes")
            .handler(this::getAll);
        router
            .post("/heroes")
            .handler(BodyHandler.create())
            .handler(this::create);
        return vertx
            .createHttpServer()
            .requestHandler(router)
            .rxListen(8888)
            .ignoreElement();
    }

    private void create(RoutingContext context) {
        final WaesHero waesHero = context.getBodyAsJson().mapTo(WaesHero.class);
        waesHero.id = UUID.randomUUID().toString();
        final JsonObject document = JsonObject.mapFrom(waesHero);
        mongoClient
            .rxInsert("heroes", document)
            .ignoreElement()
            .doOnError(Throwable::printStackTrace)
            .subscribe(() -> context.response().end(document.encodePrettily()), context::fail);
    }

    private void getAll(RoutingContext context) {
        mongoClient
            .rxFind("heroes", new JsonObject())
            .map(Json::encodePrettily)
            .doOnError(Throwable::printStackTrace)
            .subscribe(json -> context.response().end(json), context::fail);
    }

}
