package tech.lacambra.fabric.client.messaging;

import java.lang.reflect.Type;

public interface MessageConverter {

  boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType);

  <O> O convert(Object o, Class outputType, Type outputParametrizedType);

}
