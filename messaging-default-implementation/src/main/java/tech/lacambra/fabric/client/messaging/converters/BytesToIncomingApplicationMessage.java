package tech.lacambra.fabric.client.messaging.converters;

import tech.lacambra.fabric.client.messaging.IncomingApplicationMessage;
import tech.lacambra.fabric.client.messaging.MessageConverter;
import tech.lacambra.fabric.client.messaging.MessageIOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class BytesToIncomingApplicationMessage implements MessageConverter {

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return inputType.equals(List.class) && inputParametrizedType.equals(byte[].class) && outputType.equals(IncomingApplicationMessage.class);
  }

  @Override
  public IncomingApplicationMessage convert(Object o, Class outputType, Type outputParametrizedType) {

    List<byte[]> l = (List<byte[]>) o;

    if (l.isEmpty()) {
      return new IncomingApplicationMessage(JsonValue.EMPTY_JSON_OBJECT);
    }

    try (ByteArrayInputStream bais = new ByteArrayInputStream(l.get(0))) {
      JsonObject json = Json.createReader(bais).readObject();
      return new IncomingApplicationMessage(json);
    } catch (IOException e) {
      throw new MessageIOException(e);
    }


  }
}
