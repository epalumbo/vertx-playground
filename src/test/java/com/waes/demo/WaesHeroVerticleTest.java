package com.waes.demo;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.ext.web.client.predicate.ResponsePredicate.contentType;
import static io.vertx.ext.web.client.predicate.ResponsePredicate.status;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
class WaesHeroVerticleTest {

    private WebClient webClient;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext test) {
        webClient = WebClient.create(vertx);
        vertx.deployVerticle(new WaesHeroVerticle(), test.completing());
    }

    @Test
    void shouldCreateNewHero(VertxTestContext test) {
        final JsonObject payload = new JsonObject()
            .put("name", "somebody");
        webClient
            .post(8888, "localhost", "/heroes")
            .as(BodyCodec.jsonObject())
            .expect(status(201))
            .expect(contentType("application/json"))
            .sendJsonObject(payload, test.succeeding(response -> {
                final JsonObject body = response.body();
                assertThat(body.getString("_id")).isNotBlank();
                assertThat(body.getString("name")).isEqualTo("somebody");
                test.completeNow();
            }));
    }

}
