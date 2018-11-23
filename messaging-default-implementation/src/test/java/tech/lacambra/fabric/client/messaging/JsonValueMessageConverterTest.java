package tech.lacambra.fabric.client.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.lacambra.fabric.client.messaging.converters.JsonValueMessageConverter;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonValueMessageConverterTest {

  JsonValueMessageConverter cut;

  @BeforeEach
  void setUp() {
    cut = new JsonValueMessageConverter();
  }

  @Test
  void canConvert() {

    assertTrue(cut.canConvert(JsonString.class, null, String.class, null));
    assertFalse(cut.canConvert(JsonString.class, null, Boolean.class, null));
    assertTrue(cut.canConvert(JsonValue.class, null, Boolean.class, null));
    assertFalse(cut.canConvert(JsonNumber.class, null, Integer.class, null));
    System.out.println(Json.createObjectBuilder().add("s", true).build().get("s").getClass());

  }
}