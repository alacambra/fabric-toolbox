package tech.lacambra.fabric.client.messaging.converters;

import tech.lacambra.fabric.client.messaging.ErrorMessage;
import tech.lacambra.fabric.client.messaging.MessageConverter;

import javax.json.Json;
import javax.json.JsonObject;
import java.lang.reflect.Type;

public class ErrorMessageToJson implements MessageConverter {

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return inputType.equals(ErrorMessage.class);
  }

  @Override
  public JsonObject convert(Object o, Class outputType, Type outputParametrizedType) {

    ErrorMessage errorMessage = (ErrorMessage) o;

    return Json.createObjectBuilder()
        .add("code", errorMessage.getStatus())
        .add("message", errorMessage.getMessage()).build();
  }
}