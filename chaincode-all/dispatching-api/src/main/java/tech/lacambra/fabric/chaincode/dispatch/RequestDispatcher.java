package tech.lacambra.fabric.chaincode.dispatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import tech.lacambra.fabric.chaincode.execution.ContractExecution;
import tech.lacambra.fabric.chaincode.execution.ContractExecutor;
import tech.lacambra.fabric.chaincode.metainfo.ContractMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.ContractsRegister;
import tech.lacambra.fabric.client.messaging.InvocationRequest;
import tech.lacambra.fabric.client.messaging.MessageConverterProvider;

public class RequestDispatcher extends ChaincodeBase {

  private static final Log LOGGER = LogFactory.getLog(RequestDispatcher.class);
  private final ContractsRegister contractsRegister;
  private final ContractExecutor contractExecutor;
  private final MessageConverterProvider readerWriterProvider;

  public RequestDispatcher(ContractExecutor contractExecutor, ContractsRegister contractsRegister, MessageConverterProvider readerWriterProvider) {
    this.contractExecutor = contractExecutor;
    this.contractsRegister = contractsRegister;
    this.readerWriterProvider = readerWriterProvider;
  }

  @Override
  public Response init(ChaincodeStub chaincodeStub) {
    return newSuccessResponse();
  }

  @Override
  public Response invoke(ChaincodeStub chaincodeStub) {

    String txId = chaincodeStub.getTxId();
    LOGGER.info("[invoke] Received invocation. TxId=" + txId);

    InvocationRequest invocationRequest = new CCInvocationRequest(chaincodeStub);
    ContractMetaInfo contract = contractsRegister
        .getContract(invocationRequest.getContractName())
        .orElseThrow(() -> new DispatcherException(String.format("Contract with id %s not found", invocationRequest.getContractName())));

    try {
      ContractExecution contractExecutionResult = contractExecutor.executeFunction(contract, invocationRequest, chaincodeStub, readerWriterProvider);
      return createResponse(contractExecutionResult);
    } catch (Exception e) {
      return createResponse(e);
    }
  }

  private Response createResponse(ContractExecution contractExecutionResult) {
    if (contractExecutionResult.isSucceed()) {
      return newSuccessResponse();
    } else {
      return newErrorResponse();
    }
  }

  private Response createResponse(Exception e) {
    return newErrorResponse(e);
  }
}
