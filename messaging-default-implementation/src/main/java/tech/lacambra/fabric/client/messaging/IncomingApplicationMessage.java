package tech.lacambra.fabric.client.messaging;

import tech.lacambra.fabric.commons.JsonUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class IncomingApplicationMessage {

  private JsonObject headers;
  private JsonObject body;

  public IncomingApplicationMessage(JsonObject message) {
    this(message.getJsonObject("headers"), message.getJsonObject("body"));
  }

  public IncomingApplicationMessage(JsonObject headers, JsonObject body) {

    if (headers == null) {
      headers = JsonValue.EMPTY_JSON_OBJECT;
    }

    if (body == null) {
      body = JsonValue.EMPTY_JSON_OBJECT;
    }

    this.headers = headers;
    this.body = body;
  }

  public JsonObject getHeaders() {
    return headers;
  }

  public JsonObject getBody() {
    return body;
  }

  public <T> T getHeader(String key, Class<T> clazz) {
    return JsonUtils.convertJsonValueToJavaValue(headers.getValue(key), clazz);
  }

  public JsonObject toJson() {
    return Json.createObjectBuilder().add("headers", headers).add("body", body).build();
  }

}
