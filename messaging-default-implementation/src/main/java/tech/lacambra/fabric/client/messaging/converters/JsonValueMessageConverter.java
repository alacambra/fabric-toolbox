package tech.lacambra.fabric.client.messaging.converters;

import tech.lacambra.fabric.client.messaging.MessageConverter;
import tech.lacambra.fabric.client.messaging.MessageIOException;

import javax.json.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class JsonValueMessageConverter implements MessageConverter {

  private static final List<Object> NON_ACCEPTED_FOR_BOOLEAN = Arrays.asList(JsonString.class, JsonNumber.class, JsonArray.class, JsonObject.class);

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {


    return (JsonString.class.isAssignableFrom(inputType) && outputType.equals(String.class)) ||
        (JsonObject.class.isAssignableFrom(inputType) && outputType.equals(JsonObject.class)) ||
        (JsonArray.class.isAssignableFrom(inputType) && outputType.equals(JsonArray.class)) ||
        (JsonValue.class.isAssignableFrom(inputType) && outputType.equals(Boolean.class) && !NON_ACCEPTED_FOR_BOOLEAN.contains(inputType));
  }

  @Override
  public Object convert(Object o, Class outputType, Type outputParametrizedType) {

    JsonValue v = (JsonValue) o;

    switch (v.getValueType()) {

      case ARRAY:
        return o;
      case OBJECT:
        return o;
      case STRING:
        return ((JsonString) v).getString();
      case TRUE:
        return true;
      case FALSE:
        return false;
      case NULL:
        return null;
      default:
        throw new MessageIOException("Not possible to convert object to type " + v.getValueType());
    }
  }
}
