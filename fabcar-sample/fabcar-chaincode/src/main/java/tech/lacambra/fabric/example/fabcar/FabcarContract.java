package tech.lacambra.fabric.example.fabcar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import tech.lacambra.fabric.chaincode.execution.Body;
import tech.lacambra.fabric.chaincode.execution.Contract;
import tech.lacambra.fabric.chaincode.execution.ContractFunction;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Contract(id = FabcarContractParam.CONTRACT_ID)
public class FabcarContract {

  private static final Log LOGGER = LogFactory.getLog(FabcarContract.class);

  @ContractFunction
  public Car queryCar(ChaincodeStub stub, @Body("key") String carKey) {

    Objects.requireNonNull(carKey);

    byte[] carAsBytes = stub.getState(carKey);
    Car car = new Car(carAsBytes);

    LOGGER.info(String.format("[queryCar] Get car %s = %s", carKey, car));
    return car;
  }

  @ContractFunction(FabcarContractParam.INIT_LEDGER)
  public void initLedger(ChaincodeStub stub) {

    AtomicInteger i = new AtomicInteger(0);

    generateCars()
        .map(Car::toJson)
        .peek(c -> stub.putStringState(String.valueOf("CAR" + i.getAndIncrement()), c.toString()))
        .forEach(c -> LOGGER.info("[initLedger] Added car: " + c));
  }

  @ContractFunction(FabcarContractParam.CREATE_CAR)
  public void createCar(ChaincodeStub stub, @Body("key") String carKey, @Body("car") Car car) {

    String carAsString = car.toJson().toString();
    stub.putStringState(carKey, carAsString);

  }

  @ContractFunction(FabcarContractParam.QUERY_ALL_CARS)
  public List<Car> queryAllCars(ChaincodeStub stub) {

    String startKey = "CAR0";
    String endKey = "CAR999";

    try (QueryResultsIterator<KeyValue> it = stub.getStateByRange(startKey, endKey)) {

      List<Car> cars = StreamSupport.stream(it.spliterator(), false).map(this::toJsonRecord).map(Car::new).collect(Collectors.toList());
      LOGGER.info("[queryAllCars] CARS=" + cars);

      return cars;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @ContractFunction(FabcarContractParam.CHANGE_CAR_OWNER)
  public void changeCarOwner(ChaincodeStub stub, @Body("carKey") String carKey, @Body("owner") String owner) {

    Car car = Car.fromJson(stub.getStringState(carKey));
    car.setOwner(owner);

    stub.putStringState(carKey, car.toJson().toString());
  }

  private JsonObject toJsonRecord(KeyValue kv) {

    JsonObject recordJson = Json.createReader(new StringReader(kv.getStringValue())).readObject();
    return Json.createObjectBuilder().add("Key", kv.getKey()).add("Record", recordJson).build();

  }

  private Stream<Car> generateCars() {
    return Stream.of(
        new Car("Toyota", "Prius", "blue", "Tomoko"),
        new Car("Ford", "Mustang", "red", "Brad"),
        new Car("Hyundai", "Tucson", "green", "Jin Soo"),
        new Car("Volkswagen", "Passat", "yellow", "Max"),
        new Car("Tesla", "S", "black", "Adriana"),
        new Car("Peugeot", "205", "purple", "Michel"),
        new Car("Chery", "S22L", "white", "Aarav"),
        new Car("Fiat", "Punto", "violet", "Pari"),
        new Car("Tata", "Nano", "indigo", "Valeria"),
        new Car("Holden", "Barina", "brown", "Shotaro")
    );
  }
}