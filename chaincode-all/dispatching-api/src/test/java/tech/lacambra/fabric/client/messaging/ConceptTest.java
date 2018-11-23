package tech.lacambra.fabric.client.messaging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConceptTest {

  @BeforeEach
  void setUp() {
  }

  @Test
  public void check() throws InvocationTargetException, IllegalAccessException {


    JsonObject input = Json.createObjectBuilder().add("value", 1).add("anotherValue", "str").build();

    List<MessageConverter> converters = new ArrayList<>();
    converters.add(new JsonToSomeClassConverter());
    converters.add(new SomeClassToJsonConverter());

    Method method = Stream.of(getClass().getDeclaredMethods())
        .filter(m -> m.getName().equalsIgnoreCase("checkMethod"))
        .findAny().get();

    Class oCls = method.getParameterTypes()[0];

    MessageConverter converter = converters.stream().filter(c -> c.canConvert(JsonObject.class, null, oCls, null)).findAny().get();
    Object o1 = converter.convert(input, oCls, null);
    Object o2 = method.invoke(this, o1);

    Class rCls = method.getReturnType();
//    Assertions.assertEquals(Void.TYPE, rCls);
    System.out.println(rCls);
    converter = converters.stream().filter(c -> c.canConvert(rCls, null, JsonObject.class, null)).findAny().get();

    o1 = converter.convert(o2, JsonObject.class, null);

    Assertions.assertTrue(JsonObject.class.isAssignableFrom(o1.getClass()), o1.getClass() + "");

    JsonObject j = (JsonObject) o1;
    Assertions.assertEquals(j.getInt("value"), input.getInt("value"));
    Assertions.assertEquals(j.getString("anotherValue"), input.getString("anotherValue"));


  }

  private SomeClass checkMethod(SomeClass someClass) {
    System.out.println("executed:" + someClass);
    return someClass;
  }

  @Test
  public void check2() throws InvocationTargetException, IllegalAccessException {


    JsonArray input = Json.createArrayBuilder()
        .add(Json.createObjectBuilder().add("value", 1).add("anotherValue", "str").build())
        .add(Json.createObjectBuilder().add("value", 2).add("anotherValue", "str2").build())
        .build();

    List<MessageConverter> converters = new ArrayList<>();
    converters.add(new JsonToSomeClassConverter());
    converters.add(new SomeClassToJsonConverter());
    converters.add(new JsonToSomeClassListConverter());
    converters.add(new SomeClassListToJsonConverter());

    Method method = Stream.of(getClass().getDeclaredMethods())
        .filter(m -> m.getName().equalsIgnoreCase("checkMethod2"))
        .findAny()
        .get();

    Class oCls = method.getParameterTypes()[0];
    Type ogType = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];

    MessageConverter converter = converters.stream().filter(c -> c.canConvert(JsonObject.class, null, oCls, ogType)).findAny().get();
    Object o1 = converter.convert(input, oCls, ogType);
    Object o2 = method.invoke(this, o1);

    Class rCls = method.getReturnType();
    Type rgType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
//    Assertions.assertEquals(Void.TYPE, rCls);
    System.out.println(rCls);
    System.out.println(rgType);
    converter = converters.stream().filter(c -> c.canConvert(rCls, rgType, JsonObject.class, null)).findAny().get();

    o1 = converter.convert(o2, JsonObject.class, null);

    System.out.println(o1);

    Assertions.assertTrue(JsonObject.class.isAssignableFrom(o1.getClass()), o1.getClass() + "");

//    JsonObject j = (JsonObject) o1;
//    Assertions.assertEquals(j.getInt("value"), input.getInt("value"));
//    Assertions.assertEquals(j.getString("anotherValue"), input.getString("anotherValue"));


  }

  private List<SomeClass> checkMethod2(List<SomeClass> someClass) {
    System.out.println("executed2:" + someClass);
    return someClass;
  }
}

class JsonToSomeClassListConverter implements MessageConverter {

  JsonToSomeClassConverter converter = new JsonToSomeClassConverter();


  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {

    return inputType.equals(JsonObject.class) && outputType.equals(List.class) && outputParametrizedType.equals(SomeClass.class);
  }

  @Override
  public List<SomeClass> convert(Object o, Class outputType, Type outputParametrizedType) {
    JsonArray arr = (JsonArray) o;
    return arr.stream().map(j -> new SomeClass(j.asJsonObject().getInt("value"), j.asJsonObject().getString("anotherValue"))).collect(Collectors.toList());
  }
}


class SomeClassListToJsonConverter implements MessageConverter {

  SomeClassToJsonConverter jsonConverter = new SomeClassToJsonConverter();

  @Override
  public boolean canConvert(Class inputType, Type inputParametrizedType, Class outputType, Type outputParametrizedType) {
    return inputType.equals(List.class) && inputParametrizedType.equals(SomeClass.class) && outputType.equals(JsonObject.class);
  }

  @Override
  public JsonObject convert(Object o, Class outputType, Type outputParametrizedType) {
    List<SomeClass> o1 = (List<SomeClass>) o;
    JsonArray a = o1.stream().map(v -> jsonConverter.convert(v, null, null)).collect(JsonCollectors.toJsonArray());
    return Json.createObjectBuilder().add("value", a).build();
  }


//  @Override
//  public JsonObject convert(List<SomeClass> o) {
//
//    JsonArray a = o.stream().map(jsonConverter::convert).collect(JsonCollectors.toJsonArray());
//    return Json.createObjectBuilder().add("value", a).build();
//  }
}