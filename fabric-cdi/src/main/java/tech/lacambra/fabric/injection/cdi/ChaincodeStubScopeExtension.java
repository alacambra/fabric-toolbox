package tech.lacambra.fabric.injection.cdi;

import org.tomitribe.microscoped.core.ScopeContext;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import java.io.Serializable;

public class ChaincodeStubScopeExtension implements Extension, Serializable {


  public void addScope(@Observes final BeforeBeanDiscovery event) {
    event.addScope(ChaincodeRequestScope.class, true, false);
  }

  public void registerContext(@Observes final AfterBeanDiscovery event) {
    event.addContext(new ScopeContext<>(ChaincodeRequestScope.class));
  }

}
