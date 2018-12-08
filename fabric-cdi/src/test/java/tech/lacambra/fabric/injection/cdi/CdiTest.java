package tech.lacambra.fabric.injection.cdi;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.Test;
import org.tomitribe.microscoped.core.ScopeContext;

import javax.enterprise.inject.spi.BeanManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CdiTest {
  CountDownLatch latch;
  ConcurrentHashMap<String, Integer> ids = new ConcurrentHashMap();

  @Test
  public void checkInjection() throws InterruptedException {

    Weld weld = new Weld();

    weld.addExtension(new ChaincodeStubScopeExtension());
    WeldContainer container = weld.initialize();


    assertEquals(container.getBeanManager(), container.getBeanManager());


    ChaincodeStubMock stubMock1 = new ChaincodeStubMock();
    ChaincodeStubMock stubMock2 = new ChaincodeStubMock();

    latch = new CountDownLatch(4);

    assertNotEquals(stubMock1, stubMock2);

    inject(container, stubMock1, "a");
    inject(container, stubMock1, "b");
    inject(container, stubMock2, "c");
    inject(container, stubMock2, "d");

    latch.await(500, TimeUnit.MILLISECONDS);

    assertEquals(ids.get("a"), ids.get("b"));
    assertNotEquals(ids.get("b"), ids.get("c"));
    assertEquals(ids.get("d"), ids.get("c"));

    weld.shutdown();
  }

  private void inject(WeldContainer container, ChaincodeStubMock stubMock, String id) {
    new Thread(() -> {
      sleep(100);
      BeanManager beanManager = container.getBeanManager();
      ScopeContext<ChaincodeStubMock> ctx = (ScopeContext<ChaincodeStubMock>) beanManager.getContext(ChaincodeRequestScope.class);
      ChaincodeStubMock previous = ctx.enter(stubMock);
      try {
        SomeService service = container.select(SomeService.class).get();
        assertEquals(container.select(AnotherService.class).get().getSomeService(), service);
        container.select(AnotherService.class).get().getSomeService().sayHello();
        service.sayHello();
        ids.put(id, service.getId());
      } finally {
        ctx.exit(previous);
      }
      latch.countDown();
    }).start();
  }

  private static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
