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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestDispatcher extends ChaincodeBase {

  private static final Log LOGGER = LogFactory.getLog(RequestDispatcher.class);
  private final ContractsRegister contractsRegister;
  private final ContractExecutor contractExecutor;
  private final MessageConverterProvider messageConverterProvider;

  public RequestDispatcher(ContractExecutor contractExecutor, ContractsRegister contractsRegister, MessageConverterProvider messageConverterProvider) {
    this.contractExecutor = contractExecutor;
    this.contractsRegister = contractsRegister;
    this.messageConverterProvider = messageConverterProvider;

    LOGGER.info("[RequestDispatcher] Contracts found: " + contractsRegister.getRegister().stream().map(Objects::toString).collect(Collectors.joining(", ")));
    LOGGER.info("[RequestDispatcher] MessageConverters found: " + messageConverterProvider.getConverters().stream().map(Object::getClass).map(Class::getName).collect(Collectors.joining(", ")));
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
      ContractExecution contractExecutionResult = contractExecutor.executeFunction(contract, invocationRequest, chaincodeStub, messageConverterProvider);
      return createResponse(contractExecutionResult);
    } catch (Exception e) {
      return createResponse(e);
    }
  }

  private Response createResponse(ContractExecution contractExecutionResult) {

    Object resultPayload = contractExecutionResult.getPayload();

    byte[] bytes = null;

    if (resultPayload != null) {

      bytes = messageConverterProvider
          .getConverter(resultPayload.getClass(), byte[].class)
          .orElseThrow(DispatcherException::new)
          .convert(resultPayload, byte[].class, null);
    }

    if (contractExecutionResult.isSucceed()) {
      return newSuccessResponse("", bytes);
    } else {
      String message = Optional.ofNullable(contractExecutionResult.getThrowable()).map(Throwable::getMessage).orElse("");
      return newErrorResponse(message, bytes);
    }
  }

  private Response createResponse(Exception e) {
    e.printStackTrace();
    return newErrorResponse(e);
  }
}
