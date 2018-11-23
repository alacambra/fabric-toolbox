package tech.lacambra.fabric.client.messaging;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public interface MessageConverterProvider {
  Optional<MessageConverter> getConverter(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType);

  Optional<MessageConverter> getConverter(Class inputType, Class outputType);

  List<MessageConverter> getConverters();

  MessageConverterProvider addConverter(MessageConverter converter);
}
