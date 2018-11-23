package tech.lacambra.fabric.chaincode.execution;

import org.hyperledger.fabric.shim.ChaincodeStub;
import tech.lacambra.fabric.client.messaging.*;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ContractExecutor {

  public ContractExecution executeFunction(Object contract, InvocationRequest invocationRequest, ChaincodeStub stub, MessageConverterProvider converterProvider) {

    try {
      IncomingApplicationMessage applicationMessage = converterProvider
          .getConverter(List.class, byte[].class, IncomingApplicationMessage.class, null)
          .orElseThrow(() -> new ContractExecutionException("No converter found for byte[] to IncomingApplicationMessage"))
          .convert(invocationRequest.getPayload(), IncomingApplicationMessage.class, null);

      String function = invocationRequest.getFunction();

      Method method = Stream.of(contract.getClass().getMethods())
          .filter(m -> m.getName().equalsIgnoreCase(function))
          .findFirst()
          .orElseThrow(() -> new ContractExecutionException(String.format("Function not found for contract %s and function %s", contract.getClass(), function)));

      Object[] paramsInstance = getParamInstances(method, applicationMessage, converterProvider);
      Object returnValue = method.invoke(contract, paramsInstance);
      Object convertedValue = convertReturnedValue(returnValue, method, converterProvider);

      return new ContractExecution(convertedValue);

    } catch (Exception e) {
      e.printStackTrace();
      return new ContractExecution(new ContractExecutionException(e));
    }
  }

  private Object convertReturnedValue(Object value, Method method, MessageConverterProvider converterProvider) {

    if (value == null) {
      return null;
    }


    Class<?> returnType = method.getReturnType();
    Type returnGenericType = method.getGenericReturnType();

    Optional<MessageConverter> converter = converterProvider.getConverter(returnType, returnGenericType, JsonObject.class, null);
    Object outgoingApplicationMessage;

    if (OutgoingApplicationMessage.class.isAssignableFrom(returnType)) {
      //If a specific converter exists for IncommingApplicationMessage use it, otherwise pass it directly
      outgoingApplicationMessage = converter.map(r -> r.convert(r, JsonObject.class, null)).orElse(value);
    } else {

      Object jsonObject = converter
          .map(r -> r.convert(value, JsonObject.class, null))
          .orElseThrow(() -> new ContractExecutionException("No reader found for return type" + returnType));

      outgoingApplicationMessage = new OutgoingApplicationMessage(JsonValue.EMPTY_JSON_OBJECT, (JsonObject) jsonObject);
    }

    return outgoingApplicationMessage;
  }

  private Object[] getParamInstances(Method method, IncomingApplicationMessage applicationMessage, MessageConverterProvider converterProvider) {

    Class<?>[] paramTypes = method.getParameterTypes();
    Type[] paramGenericTypes = method.getGenericParameterTypes();
    Annotation[][] annotations = method.getParameterAnnotations();

    Object[] paramsInstance = new Object[paramTypes.length];

    for (int i = 0; i < paramTypes.length; i++) {

      JsonValue value = extractInputJsonValue(applicationMessage, annotations[i]);
      Class outputType = paramTypes[i];
      Type genericOutputType = paramGenericTypes[i];

      Class inputType = value == null ? applicationMessage.getClass() : value.getClass();
      Object inputInstance = value == null ? applicationMessage : value;

      Optional<MessageConverter> converter = converterProvider.getConverter(inputType, null, outputType, genericOutputType);

      Object paramInstance;

      //Make check allowing subclasses of IncomingApplicationMessage.class
      if (IncomingApplicationMessage.class.isAssignableFrom(outputType)) {
        //If a specific converter exists for IncomingApplicationMessage use it, otherwise pass it directly
        paramInstance = converter.map(r -> r.convert(inputInstance, outputType, genericOutputType)).orElse(applicationMessage);
      } else {
        paramInstance = converter
            .map(r -> r.convert(inputInstance, outputType, genericOutputType))
            .orElseThrow(() -> new ContractExecutionException("No reader found for " + inputType));
      }

      paramsInstance[i] = paramInstance;
    }
    return paramsInstance;
  }

  private JsonValue extractInputJsonValue(IncomingApplicationMessage message, Annotation[] annotations) {

    if (annotations == null || annotations.length == 0) {
      return message.getBody();
    }

    Body bodyAnnotation = getPayloadAnnotation(annotations);
    Header headerAnnotation = getHeaderAnnotation(annotations);

    if (bodyAnnotation != null && headerAnnotation != null) {
      throw new ContractExecutionException("Value cannot be taklen from both, @Header and @Payload.");
    }

    if (bodyAnnotation != null) {
      return extractFromJsonObject(bodyAnnotation.value(), message.getBody());
    }

    if (headerAnnotation != null) {
      return extractFromJsonObject(headerAnnotation.value(), message.getHeaders());
    }

    return null;
  }

  private JsonValue extractFromJsonObject(String key, JsonObject jsonObject) {

    if (key == null || key.isEmpty()) {
      return jsonObject;
    }

    if (jsonObject.containsKey(key)) {
      return jsonObject.get(key);
    }

    throw new ContractExecutionException(String.format("Key not found. Key=%s, availableKeys=%s", key, jsonObject.keySet()));
  }

  private Body getPayloadAnnotation(Annotation[] annotations) {
    return (Body) Stream.of(annotations).filter(a -> a instanceof Body).findAny().orElse(null);
  }

  private Header getHeaderAnnotation(Annotation[] annotations) {
    return (Header) Stream.of(annotations).filter(a -> a instanceof Header).findAny().orElse(null);
  }
}