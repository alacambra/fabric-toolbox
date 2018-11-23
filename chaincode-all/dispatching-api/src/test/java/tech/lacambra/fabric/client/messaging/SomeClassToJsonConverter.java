package tech.lacambra.fabric.client.messaging;

import javax.json.Json;
import javax.json.JsonObject;
import java.lang.reflect.Type;

public class SomeClassToJsonConverter implements MessageConverter {

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return inputType.equals(SomeClass.class) && outputType.equals(JsonObject.class);
  }

  @Override
  public JsonObject convert(Object o1, Class outputType, Type outputParametrizedType) {
    SomeClass o = (SomeClass) o1;
    return Json.createObjectBuilder().add("value", o.getValue()).add("anotherValue", o.getAnotherValue()).build();
  }

}