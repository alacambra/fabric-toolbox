package tech.lacambra.fabric.commons;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;

class NilJsonObjectBuilder implements JsonObjectBuilder {


  @Override
  public JsonObjectBuilder add(String name, JsonValue value) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, String value) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, BigInteger value) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, BigDecimal value) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, int value) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, long value) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, double value) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, boolean value) {
    return this;
  }

  @Override
  public JsonObjectBuilder addNull(String name) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, JsonObjectBuilder builder) {
    return this;
  }

  @Override
  public JsonObjectBuilder add(String name, JsonArrayBuilder builder) {
    return this;
  }

  @Override
  public JsonObject build() {
    return JsonValue.EMPTY_JSON_OBJECT;
  }
}
