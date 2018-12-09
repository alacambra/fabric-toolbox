package tech.lacambra.fabric.chaincode.dispatch;

import org.hyperledger.fabric.shim.ChaincodeStub;
import tech.lacambra.fabric.injection.cdi.ChaincodeRequestScope;

@ChaincodeRequestScope
public class ChaincodeStubHolder {

  private ChaincodeStub chaincodeStub;

  public ChaincodeStub getChaincodeStub() {
    return chaincodeStub;
  }

  public void setChaincodeStub(ChaincodeStub chaincodeStub) {

    if(chaincodeStub != null){
      this.chaincodeStub = chaincodeStub;
    }else {
      throw new DispatcherException("ChaincodeStub already set");
    }

  }
}
