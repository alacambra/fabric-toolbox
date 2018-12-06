package tech.lacambra.fabric.chaincode.execution;

import javax.json.JsonObject;

@Contract(id = "sample")
public class ContractSample {

  @ContractFunction("fn1")
  public void fn1(@Body JsonObject body) {


  }

  @ContractFunction
  public String fn2(@Body("param") String bodyparam) {

    return bodyparam;

  }


}
