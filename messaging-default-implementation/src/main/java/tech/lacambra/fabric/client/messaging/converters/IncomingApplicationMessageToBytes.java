package tech.lacambra.fabric.client.messaging.converters;

import tech.lacambra.fabric.client.messaging.IncomingApplicationMessage;
import tech.lacambra.fabric.client.messaging.MessageConverter;
import tech.lacambra.fabric.client.messaging.MessageIOException;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class IncomingApplicationMessageToBytes implements MessageConverter {

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return outputType.equals(byte[].class) && inputType.equals(IncomingApplicationMessage.class);
  }

  @Override
  public byte[] convert(Object o, Class outputType, Type outputParametrizedType) {
    IncomingApplicationMessage a = (IncomingApplicationMessage) o;
    JsonObject j = a.toJson();

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Json.createWriter(baos).write(j);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new MessageIOException(e);
    }
  }
}
