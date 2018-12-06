package tech.lacambra.fabric.chaincode.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.lacambra.fabric.chaincode.metainfo.ContractMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfoLoader;
import tech.lacambra.fabric.client.messaging.DefaultMessageConverterProvider;
import tech.lacambra.fabric.client.messaging.InvocationRequest;
import tech.lacambra.fabric.client.messaging.MessageConverterProvider;
import tech.lacambra.fabric.client.messaging.OutgoingApplicationMessage;

import javax.json.Json;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ContractExecutorTest {

  private static final Log LOGGER = LogFactory.getLog(ContractExecutorTest.class);

  private ContractExecutor cut;
  private MessageConverterProvider messageConverterProvider;
  ContractMetaInfo contractMetaInfo;

  @BeforeEach
  void setUp() {
    cut = new ContractExecutor();
    messageConverterProvider = new DefaultMessageConverterProvider();
//    messageConverterProvider.addConverter();

    List<FunctionMetaInfo> functionMetaInfo = new FunctionMetaInfoLoader().exploreContract(ContractSample.class);
    contractMetaInfo = new ContractMetaInfo(ContractSample.class, functionMetaInfo);

  }

  @Test
  void executeFunctionFails() {
    ContractExecution contractExecution = cut.executeFunction(
        contractMetaInfo,
        creaInvocationRequest("cName", "fn1", Json.createObjectBuilder().build()),
        null,
        messageConverterProvider
    );

    Assertions.assertFalse(contractExecution.isSucceed());
  }

  @Test
  void executeFunctionSucceed() {
    ContractExecution contractExecution = cut.executeFunction(
        contractMetaInfo,
        creaInvocationRequest("cName", "fn1", Arrays.asList(Json.createObjectBuilder().build().toString().getBytes())),
        null,
        messageConverterProvider
    );

    Assertions.assertTrue(contractExecution.isSucceed(), () -> {
      contractExecution.getThrowable().printStackTrace();
      return contractExecution.getThrowable().getMessage();
    });
  }

  @Test
  void executeFunctionSucceed2() {

    String v = "hello";
    LOGGER.info("[RequestDispatcher] Registered converters:" + messageConverterProvider.getConverters()
        .stream()
        .map(Object::getClass)
        .map(Class::getName)
        .collect(Collectors.joining(", ")));

    ContractExecution contractExecution = cut.executeFunction(
        contractMetaInfo,
        creaInvocationRequest("cName", "fn2", Arrays.asList(Json.createObjectBuilder().add("body", Json.createObjectBuilder().add("param", v)).build().toString().getBytes())),
        null,
        messageConverterProvider
    );

    Assertions.assertEquals(v, ((OutgoingApplicationMessage) contractExecution.getPayload()).getBody().getString("value"), () -> {
      contractExecution.getThrowable().printStackTrace();
      return contractExecution.getThrowable().getMessage();
    });
  }


  private InvocationRequest creaInvocationRequest(String cName, String fn, Object payload) {
    return new InvocationRequest() {
      @Override
      public String getContractName() {
        return cName;
      }

      @Override
      public String getFunction() {
        return fn;
      }

      @Override
      public Object getPayload() {
        return payload;
      }
    };
  }
}