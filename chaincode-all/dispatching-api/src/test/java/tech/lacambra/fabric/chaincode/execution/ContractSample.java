package tech.lacambra.fabric.chaincode.execution;

import javax.json.JsonObject;

@Contract(id = "sample")
public class ContractSample {

  public void fn1(@Body JsonObject body) {


  }

  public String fn2(@Body("param") String bodyparam) {

    return bodyparam;

  }


}
