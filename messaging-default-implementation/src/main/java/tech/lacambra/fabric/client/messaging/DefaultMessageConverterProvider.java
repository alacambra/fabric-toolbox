package tech.lacambra.fabric.client.messaging;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultMessageConverterProvider implements MessageConverterProvider {
  private final List<MessageConverter> converters;

  public DefaultMessageConverterProvider() {
    Reflections reflections = new Reflections("tech.lacambra.fabric.client.messaging.converters");
    Set<Class<? extends MessageConverter>> convertersTypes = reflections.getSubTypesOf(MessageConverter.class);

    converters = (List<MessageConverter>) convertersTypes.stream()
        .map(this::getConstructor)
        .map(this::instantiateConverter)
        .collect(Collectors.toList());
  }

  private Constructor getConstructor(Class clazz) {
    try {
      return clazz.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private MessageConverter instantiateConverter(Constructor<? extends MessageConverter> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public DefaultMessageConverterProvider(List<MessageConverter> converters) {
    this.converters = new ArrayList<>(converters);
  }

  @Override
  public Optional<MessageConverter> getConverter(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return getConverters()
        .stream()
        .filter(c -> c.canConvert(inputType, inputParametrizedType, outputType, outputParametrizedType))
        .findAny();
  }

  @Override
  public Optional<MessageConverter> getConverter(Class inputType, Class outputType) {
    return getConverters()
        .stream()
        .filter(c -> c.canConvert(inputType, null, outputType, null))
        .findAny();
  }

  @Override
  public List<MessageConverter> getConverters() {
    return new ArrayList<>(converters);
  }

  @Override
  public MessageConverterProvider addConverter(MessageConverter converter) {
    converters.add(converter);
    return this;
  }
}


