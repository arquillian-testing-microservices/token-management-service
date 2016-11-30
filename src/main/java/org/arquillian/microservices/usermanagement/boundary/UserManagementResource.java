package org.arquillian.microservices.usermanagement.boundary;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

public class UserManagementResource extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(UserManagementResource.class);
    }

    private Map<String, JsonObject> users = new HashMap<>();

    @Override
    public void start() {

        setUpUsers();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/:userId").handler(this::findUser);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8082)
                );

    }

    private void findUser(RoutingContext routingContext) {
        final String userId = routingContext.request().getParam("userId");
        final HttpServerResponse response = routingContext.response();

        if (userId == null) {
            sendError(400, response);
        } else {
            JsonObject user = users.get(userId);

            if (user == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(user.encodePrettily());
            }
        }

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void setUpUsers() {
        addUser(new JsonObject().put("id", "1").put("name", "Alex").put("age", 36));
        addUser(new JsonObject().put("id", "2").put("name", "Ada").put("age", 4));
        addUser(new JsonObject().put("id", "3").put("name", "Alexandra").put("age", 2));
    }

    private void addUser(JsonObject user) {
        users.put(user.getString("id"), user);
    }

}
