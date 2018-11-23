package tech.lacambra.fabric.chaincode.dispatch;

import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.lacambra.fabric.chaincode.execution.ContractExecutor;
import tech.lacambra.fabric.chaincode.metainfo.ContractMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.ContractsRegister;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfoLoader;
import tech.lacambra.fabric.client.messaging.DefaultMessageConverterProvider;
import tech.lacambra.fabric.client.messaging.IncomingApplicationMessage;
import tech.lacambra.fabric.client.messaging.MessageConverterProvider;
import tech.lacambra.fabric.client.messaging.OutgoingApplicationMessage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestDispatcherTest {

  RequestDispatcher requestDispatcher;
  ChaincodeStub stub;

  @BeforeEach
  void setUp() {

    stub = mock(ChaincodeStub.class);

    FunctionMetaInfoLoader functionMetaInfoLoader = new FunctionMetaInfoLoader();
    ContractsRegister register = new ContractsRegister(new ArrayList<>());
    List<FunctionMetaInfo> functionMetaInfo = functionMetaInfoLoader.exploreContract(SomeContract.class);
    ContractMetaInfo contractMetaInfo = new ContractMetaInfo(SomeContract.class, functionMetaInfo);

    register.register(contractMetaInfo);
    MessageConverterProvider messageConverterProvider = new DefaultMessageConverterProvider();
    requestDispatcher = new RequestDispatcher(new ContractExecutor(), register, messageConverterProvider);
  }

  @Test
  void invokeFunction1() {

    when(stub.getTxId()).thenReturn("txid");
    when(stub.getFunction()).thenReturn("SOME_FN");
    when(stub.getParameters()).thenReturn(Arrays.asList("sampleContract"));

    IncomingApplicationMessage m = new IncomingApplicationMessage(JsonValue.EMPTY_JSON_OBJECT);

    when(stub.getArgs()).thenReturn(Arrays.asList(null, null, m.toJson().toString().getBytes()));

    Chaincode.Response r = requestDispatcher.invoke(stub);

    Assertions.assertEquals(Chaincode.Response.Status.SUCCESS, r.getStatus(), r.getMessage());


  }

  @Test
  void invokeFunction2() {

    String worldStr = "world-" + System.currentTimeMillis();

    JsonObject jsonObject = Json.createObjectBuilder()
        .add("body", Json.createObjectBuilder()
            .add("hello", worldStr))
        .add("headers", Json.createObjectBuilder()
            .add("tid", 123)
        ).build();


    when(stub.getTxId()).thenReturn("txid");
    when(stub.getFunction()).thenReturn("greetFn");
    when(stub.getParameters()).thenReturn(Arrays.asList("sampleContract", new IncomingApplicationMessage(jsonObject).toJson().toString()));
    when(stub.getArgs()).thenReturn(Arrays.asList(null, null, new IncomingApplicationMessage(jsonObject).toJson().toString().getBytes()));

    Chaincode.Response r = requestDispatcher.invoke(stub);


    Assertions.assertEquals(Chaincode.Response.Status.SUCCESS, r.getStatus(), r.getMessage());

    OutgoingApplicationMessage m = new OutgoingApplicationMessage(Json.createReader(new StringReader(r.getStringPayload())).readObject());

    Assertions.assertEquals(worldStr + ":" + 123, m.getBody().getString("value"));


  }
}