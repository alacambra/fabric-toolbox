package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.lacambra.fabric.client.ChaincodeClient;
import tech.lacambra.fabric.client.SDKUtilsWrapper;
import tech.lacambra.fabric.client.chaincode.PeerTransactionValidator;
import tech.lacambra.fabric.client.chaincode.managment.ChaincodeInstallationInfo;
import tech.lacambra.fabric.client.chaincode.managment.ChaincodeInstantiationInfo;
import tech.lacambra.fabric.client.chaincode.managment.ChaincodeManager;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeployChannel {


  @Test
  public void startChaincode() throws NetworkConfigurationException, IOException, InvalidArgumentException, TransactionException {

    String ccName = "fabcar";
    String ccVersion = "1";
    String ccSourceLocation = "/Users/albertlacambra/git/fabric-samples/chaincode/fabcar/java/deployment";

    NetworkConfig networkConfig = NetworkConfig.fromYamlFile(new File("/Users/albertlacambra/git/lacambra.tech/fabric-toolbox/stateless-client/src/test/resources/network-config.yaml"));
    printNetwork(networkConfig);


    HFClient hfClient = getTheClient(networkConfig);
    Channel channel = hfClient.loadChannelFromConfig("policies-channel", networkConfig);
    channel.initialize();
    Assertions.assertTrue(channel.isInitialized());
    Assertions.assertFalse(channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)).isEmpty());


    ChaincodeManager chaincodeManager = new ChaincodeManager();


    Map<Peer, ChaincodeInstallationInfo> deploymentInfo = chaincodeManager.installChaincodeBlocking(
        ccName,
        ccVersion,
        ccSourceLocation,
        channel.getPeers(),
        hfClient,
        TransactionRequest.Type.JAVA
    );


    deploymentInfo.values().forEach(d -> {
      Assertions.assertTrue(d.deploymentSucceed(), d::getMessage);
      System.out.println(d.getMessage());
        }
    );



    CompletableFuture<Map<Peer, ChaincodeInstantiationInfo>> results = chaincodeManager.instantiate(ccName, ccVersion, channel.getPeers(), channel.getOrderers(), channel, "", hfClient, TransactionRequest.Type.JAVA);

    try {
      results.get(Duration.ofSeconds(10).getSeconds(), TimeUnit.SECONDS).values().forEach(r -> {
        Assertions.assertTrue(r.peerInstantiationSucceed(), r::getMessage);
        System.out.println(r.getMessage());
      });
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      results.join().values().forEach(r -> {
        System.out.println("Rerunning instantiation");
        try {
          Assertions.assertTrue(chaincodeManager.checkInstantiatedChaincode(channel, channel.getPeers().iterator().next(), ccName, "", ccVersion));
        } catch (InvalidArgumentException e1) {
          e1.printStackTrace();
        } catch (ProposalException e1) {
          e1.printStackTrace();
        }
        System.out.println(r.getMessage());
      });
    }

    ChaincodeClient client = new ChaincodeClient().withStatelessClient(new StatelessClient())
        .withHFCLient(hfClient)
        .withChaincodeId(ccName, ccVersion)
        .withOrderers(channel.getOrderers())
        .withPeers(channel.getPeers())
        .withChannel(channel)
        .withResponsesValidator(new PeerTransactionValidator(new SDKUtilsWrapper()));

    SimulationInfo r = client.query("initLedger", Collections.emptyList()).join();
    Assertions.assertTrue(r.simulationsSucceed(), () -> r.getMessage());
  }


  private void printNetwork(NetworkConfig networkConfig) {
    networkConfig.getPeerNames().stream().map(n -> {
      try {
        System.out.println(n);
        return networkConfig.getPeerProperties(n);
      } catch (InvalidArgumentException e) {
        e.printStackTrace();
        return null;
      }
    }).filter(Objects::nonNull).forEach(System.out::println);
  }


  // Returns a new client instance
  private static HFClient getTheClient(NetworkConfig networkConfig) {
    try {

      HFClient client = HFClient.createNewInstance();

      client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

      User peerAdmin = networkConfig.getPeerAdmin("lacambra.tech");
      client.setUserContext(peerAdmin);

      return client;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
