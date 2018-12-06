package tech.lacambra.fabric.example.fabcar;

import tech.lacambra.fabric.client.messaging.MessageConverter;
import tech.lacambra.fabric.client.messaging.MessageIOException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;
import java.lang.reflect.Type;
import java.util.List;

public class CarConverter implements MessageConverter {

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {

    return (JsonObject.class.isAssignableFrom(inputType) && outputType.equals(Car.class)) ||
        Car.class.equals(inputType) && JsonObject.class.isAssignableFrom(outputType) ||
        (List.class.isAssignableFrom(inputType) && inputParametrizedType.equals(Car.class) && JsonArray.class.isAssignableFrom(outputType));

  }

  @Override
  public Object convert(Object o, Class outputType, Type outputParametrizedType) {

    if (outputType.equals(Car.class)) {

      JsonObject jCar = (JsonObject) o;
      return new Car(jCar);

    } else if (JsonObject.class.isAssignableFrom(outputType)) {

      Car c = (Car) o;
      return c.toJson();

    } else if (JsonArray.class.isAssignableFrom(outputType)) {

      List<Car> cars = (List<Car>) o;
      return cars.stream().map(Car::toJson).collect(JsonCollectors.toJsonArray());
    }

    throw new MessageIOException("Invalid converter for " + outputType.getName());
  }
}
