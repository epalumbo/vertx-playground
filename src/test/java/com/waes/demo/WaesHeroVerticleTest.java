package com.waes.demo;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
            .sendJsonObject(payload, test.succeeding(postResponse -> test.verify(() -> {
                final JsonObject hero = postResponse.body();
                assertThat(hero.getString("_id")).isNotBlank();
                assertThat(hero.getString("name")).isEqualTo("somebody");
                webClient
                    .get(8888, "localhost", "/heroes")
                    .as(BodyCodec.jsonArray())
                    .expect(status(200))
                    .expect(contentType("application/json"))
                    .send(test.succeeding(getResponse -> test.verify(() -> {
                        final JsonArray array = getResponse.body();
                        assertThat(array).hasSize(1);
                        assertThat(array.getJsonObject(0)).isEqualTo(hero);
                        test.completeNow();
                    })));
            })));
    }

}
