package tech.lacambra.fabric.chaincode.dispatch;

import org.hyperledger.fabric.shim.ChaincodeStub;
import tech.lacambra.fabric.client.messaging.InvocationRequest;

public class CCInvocationRequest implements InvocationRequest {

  private final int MESSAGE_POS = 2;
  private String targetContract;
  private String function;
  private Object payload;


  public CCInvocationRequest(ChaincodeStub stub) {
    function = stub.getFunction();
    targetContract = stub.getParameters().get(0);

    if (stub.getArgs().size() > MESSAGE_POS) {
      this.payload = stub.getArgs().subList(MESSAGE_POS, stub.getArgs().size());
    }
  }

  @Override
  public String getContractName() {
    return targetContract;
  }

  @Override
  public String getFunction() {
    return function;
  }

  @Override
  public Object getPayload() {
    return payload;
  }
}
