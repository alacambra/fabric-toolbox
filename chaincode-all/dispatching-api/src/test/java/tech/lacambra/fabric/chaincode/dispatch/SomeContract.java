package tech.lacambra.fabric.chaincode.dispatch;

import tech.lacambra.fabric.chaincode.execution.Body;
import tech.lacambra.fabric.chaincode.execution.Contract;
import tech.lacambra.fabric.chaincode.execution.ContractFunction;
import tech.lacambra.fabric.chaincode.execution.Header;

import javax.json.JsonObject;
import java.util.Objects;

@Contract(id = "sampleContract")
public class SomeContract {

  @ContractFunction("SOME_FN")
  public void function() {

  }

  @ContractFunction("greetFn")
  public String greet(@Body JsonObject body, @Header("tid") Integer tid) {

    String hello = body.getString("hello");
    Objects.requireNonNull(tid);

    if (hello == null) {
      throw new RuntimeException();
    }

    return hello + ":" + tid;
  }

}
