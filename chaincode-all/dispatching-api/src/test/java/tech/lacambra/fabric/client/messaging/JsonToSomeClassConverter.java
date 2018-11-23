package tech.lacambra.fabric.client.messaging;

import javax.json.JsonObject;
import java.lang.reflect.Type;

public class JsonToSomeClassConverter implements MessageConverter {


  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {

    return inputType.equals(JsonObject.class) && outputType.equals(SomeClass.class);
  }

  @Override
  public SomeClass convert(Object o, Class outputType, Type outputParametrizedType) {
    JsonObject object = (JsonObject) o;

    SomeClass someClass = new SomeClass(object.getInt("value"), object.getString("anotherValue"));

    return someClass;
  }
}
