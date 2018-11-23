package tech.lacambra.fabric.client.messaging;

public interface InvocationRequest {

  String getContractName();

  String getFunction();

  Object getPayload();
}
