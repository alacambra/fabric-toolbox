package tech.lacambra.fabric.client.messaging.converters;


import tech.lacambra.fabric.client.messaging.MessageConverter;
import tech.lacambra.fabric.commons.JsonUtils;

import javax.json.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class BasicTypeToJsonObject implements MessageConverter {

  private static final List<Class> acceptedClass;

  static {
    acceptedClass = Arrays.asList(
        String.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        Boolean.class,
        BigInteger.class,
        Float.class,
        int.class,
        long.class,
        double.class,
        boolean.class
    );
  }

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return (acceptedClass.contains(inputType) || JsonValue.class.isAssignableFrom(inputType)) &&
        (!JsonStructure.class.isAssignableFrom(inputType) && JsonObject.class.isAssignableFrom(outputType));
  }

  @Override
  public JsonObject convert(Object o, Class outputType, Type outputParametrizedType) {

    JsonObjectBuilder builder = Json.createObjectBuilder();
    JsonUtils.addValueToObjectBuilder(builder, "value", o);

    return builder.build();
  }
}
