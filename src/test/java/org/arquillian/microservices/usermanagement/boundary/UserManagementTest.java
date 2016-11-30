package org.arquillian.microservices.usermanagement.boundary;


import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(VertxUnitRunner.class)
public class UserManagementTest {

    private Vertx vertx;
    private Integer port;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        port = getRandomPort();

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port)
                );

        // We pass the options as the second parameter of the deployVerticle method.
        vertx.deployVerticle(UserManagementResource.class.getName(), options, context.asyncAssertSuccess());
    }

    private int getRandomPort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();

        return port;
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void should_return_user(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/1", response -> {
            context.assertEquals(response.statusCode(), 200);
            context.assertEquals(response.headers().get("content-type"), "application/json");
            response.bodyHandler(body -> {
                final JsonObject user = new JsonObject(body.toString());
                context.assertEquals(user.getString("name"), "Alex");
                async.complete();
            });
        });

    }

    @Test
    public void should_return_user_restassured() {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port);

        given()
                .spec(requestSpecBuilder.build())
        .when()
                .get("/1")
        .then()
                .assertThat().body("name", equalTo("Alex"));
    }

}
