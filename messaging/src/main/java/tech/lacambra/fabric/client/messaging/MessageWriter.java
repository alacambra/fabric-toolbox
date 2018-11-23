package tech.lacambra.fabric.client.messaging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface MessageWriter<W> {

  W write(Object t, Class<?> type, Type genericType, Annotation[] annotations);

  boolean isWritable(Class<?> type, Type genericType, Annotation[] annotations);
}