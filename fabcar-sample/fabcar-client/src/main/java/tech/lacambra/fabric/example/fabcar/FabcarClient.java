package tech.lacambra.fabric.example.fabcar;

import tech.lacambra.fabric.client.ChaincodeClient;
import tech.lacambra.fabric.client.messaging.IncomingApplicationMessage;
import tech.lacambra.fabric.client.messaging.OutgoingApplicationMessage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class FabcarClient {

  private ChaincodeClient client;

  public FabcarClient(ChaincodeClient client) {
    this.client = client;
  }


  public CompletableFuture<Car> initLedger() {

    IncomingApplicationMessage incomingApplicationMessage = new IncomingApplicationMessage(JsonValue.EMPTY_JSON_OBJECT);

    return client.invoke(FabcarContractParam.INIT_LEDGER, Arrays.asList(FabcarContractParam.CONTRACT_ID, incomingApplicationMessage.toJson().toString())).thenApply(fabricTxInfo -> {

      if (!fabricTxInfo.isSuccessful()) {
        fabricTxInfo.getFabricException().ifPresent(e -> {
          throw new RuntimeException(e);
        });
        throw new RuntimeException(fabricTxInfo.getMessage());
      }

      JsonObject jsonObject = Json.createReader(new StringReader(fabricTxInfo.getChainCodeResponse())).readObject();
      OutgoingApplicationMessage outgoingApplicationMessage = new OutgoingApplicationMessage(jsonObject);

      return new Car(outgoingApplicationMessage.getBody());

    }).exceptionally(throwable -> null);
  }

  public CompletableFuture<Car> createCar(String carKey, Car car) {

    Json.createObjectBuilder().add("body", Json.createObjectBuilder().add("key", carKey).add("car", car.toJson()).build());
    IncomingApplicationMessage incomingApplicationMessage = new IncomingApplicationMessage(Json.createObjectBuilder().add("body", Json.createObjectBuilder().add("key", carKey).add("car", car.toJson()).build()).build());

    return client.invoke(FabcarContractParam.CREATE_CAR, Arrays.asList(FabcarContractParam.CONTRACT_ID, incomingApplicationMessage.toJson().toString())).thenApply(fabricTxInfo -> {

      if (!fabricTxInfo.isSuccessful()) {
        fabricTxInfo.getFabricException().ifPresent(e -> {
          throw new RuntimeException(e);
        });
        throw new RuntimeException(fabricTxInfo.getMessage());
      }

      JsonObject jsonObject = Json.createReader(new StringReader(fabricTxInfo.getChainCodeResponse())).readObject();
      OutgoingApplicationMessage outgoingApplicationMessage = new OutgoingApplicationMessage(jsonObject);

      return new Car(outgoingApplicationMessage.getBody());

    }).exceptionally(throwable -> null);
  }

  public CompletableFuture<Car> queryAllCars() {

    IncomingApplicationMessage incomingApplicationMessage = new IncomingApplicationMessage(JsonValue.EMPTY_JSON_OBJECT);

    return client.query(FabcarContractParam.QUERY_ALL_CARS, Arrays.asList(FabcarContractParam.CONTRACT_ID, incomingApplicationMessage.toJson().toString())).thenApply(simulationInfo -> {

      if (!simulationInfo.simulationsSucceed()) {
        throw new RuntimeException(simulationInfo.getMessage());
      }

      JsonObject jsonObject = Json.createReader(new StringReader(simulationInfo.getPayloadAsString())).readObject();
      OutgoingApplicationMessage outgoingApplicationMessage = new OutgoingApplicationMessage(jsonObject);

      return new Car(outgoingApplicationMessage.getBody());

    }).exceptionally(throwable -> null);
  }

  public CompletableFuture<Car> changeCarOwner(String carKey, String owner) {

    IncomingApplicationMessage incomingApplicationMessage = new IncomingApplicationMessage(Json.createObjectBuilder().add("body", Json.createObjectBuilder().add("key", carKey).add("owner", owner).build()).build());

    return client.invoke(FabcarContractParam.CHANGE_CAR_OWNER, Arrays.asList(FabcarContractParam.CONTRACT_ID, incomingApplicationMessage.toJson().toString())).thenApply(fabricTxInfo -> {

      if (!fabricTxInfo.isSuccessful()) {
        fabricTxInfo.getFabricException().ifPresent(e -> {
          throw new RuntimeException(e);
        });
        throw new RuntimeException(fabricTxInfo.getMessage());
      }

      JsonObject jsonObject = Json.createReader(new StringReader(fabricTxInfo.getChainCodeResponse())).readObject();
      OutgoingApplicationMessage outgoingApplicationMessage = new OutgoingApplicationMessage(jsonObject);

      return new Car(outgoingApplicationMessage.getBody());

    }).exceptionally(throwable -> null);
  }
}
