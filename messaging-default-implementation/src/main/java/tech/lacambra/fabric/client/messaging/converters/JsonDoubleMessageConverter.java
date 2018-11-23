package tech.lacambra.fabric.client.messaging.converters;

import tech.lacambra.fabric.client.messaging.MessageConverter;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class JsonDoubleMessageConverter implements MessageConverter {

  private static final List<Object> NON_ACCEPTED_FOR_BOOLEAN = Arrays.asList(JsonString.class, JsonNumber.class, JsonArray.class, JsonObject.class);

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return (JsonNumber.class.equals(inputType) && outputType.equals(Double.class));
  }

  @Override
  public Object convert(Object o, Class outputType, Type outputParametrizedType) {
    return ((JsonNumber) o).doubleValue();
  }
}