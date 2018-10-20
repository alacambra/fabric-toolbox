package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class DeployChannel {


  @Test
  public void startChaincode() throws NetworkConfigurationException, IOException, InvalidArgumentException, TransactionException {

    String ccName = "fabcar";
    String version = "2";
    String ccSourceLocation = "/Users/albertlacambra/git/fabric-samples/chaincode/fabcar/java/deployment";

    NetworkConfig networkConfig = NetworkConfig.fromYamlFile(new File("/Users/albertlacambra/git/lacambra.tech/fabric-toolbox/stateless-client/src/test/resources/network-config.yaml"));
    printNetwork(networkConfig);

    HFClient hfClient = getTheClient(networkConfig);
    Channel channel = hfClient.loadChannelFromConfig("foo", networkConfig);
    channel.initialize();
    Assertions.assertTrue(channel.isInitialized());


    //Start Chaincode
    ChaincodeManager chaincodeManager = new ChaincodeManager();


    Map<Peer, ChaincodeInstallationInfo> deploymentInfo = chaincodeManager.installChaincodeBlocking(
        ccName,
        version,
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

    CompletableFuture<Map<Peer, ChaincodeInstantiationInfo>> results = chaincodeManager.instantiate(ccName, version, channel.getPeers(), channel.getOrderers(), channel, "", hfClient, TransactionRequest.Type.JAVA);

    results.join().values().forEach(r -> {
      Assertions.assertTrue(r.peerInstantiationSucceed(), r::getMessage);
      System.out.println(r.getMessage());
    });
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

      User peerAdmin = networkConfig.getPeerAdmin("Org1");
      client.setUserContext(peerAdmin);

      return client;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
