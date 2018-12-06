package tech.lacambra.fabric.example.fabcar;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.mock.peer.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.READY;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.REGISTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PeerSimulation {

  private ChaincodeMockPeer mockPeer;

  @AfterEach
  public void afterTest() throws Exception {
    if (mockPeer != null) {
      mockPeer.stop();
      mockPeer = null;
    }
  }

  @Test
  public void test() {

    List<ScenarioStep> scenarioSteps;

    try {


      scenarioSteps = new ArrayList<>();
      scenarioSteps.add(new RegisterStep());
      scenarioSteps.add(new PutValueStep("test"));

      mockPeer = ChaincodeMockPeer.startServer(scenarioSteps);

      FabcarLauncher.main(new String[]{"-a", "127.0.0.1:7052", "-i", "fabcar"});
      ChaincodeBase chaincodeBase = FabcarLauncher.getRequestDispatcher();
      checkScenarioStepEnded(mockPeer, 1, 5000, TimeUnit.MILLISECONDS);

      assertEquals(mockPeer.getLastMessageSend().getType(), READY);
      assertEquals(mockPeer.getLastMessageRcvd().getType(), REGISTER);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void checkScenarioStepEnded(final ChaincodeMockPeer s, final int step, final int timeout, final TimeUnit units) throws Exception {
    try {
      TimeoutUtil.runWithTimeout(new Thread(() -> {
        while (true) {
          if (s.getLastExecutedStep() == step) return;
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
          }
        }
      }), timeout, units);
    } catch (TimeoutException e) {
      fail("Got timeout, first step not finished");
    }
  }
}