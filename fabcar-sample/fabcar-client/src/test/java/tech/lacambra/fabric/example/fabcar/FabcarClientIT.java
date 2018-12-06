package tech.lacambra.fabric.example.fabcar;


import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.lacambra.fabric.client.ChaincodeClient;
import tech.lacambra.fabric.client.SDKUtilsWrapper;
import tech.lacambra.fabric.client.chaincode.PeerTransactionValidator;
import tech.lacambra.fabric.client.stateless.StatelessClient;

import java.io.File;
import java.io.IOException;

public class FabcarClientIT {

  private static FabcarClient cut;


  @BeforeAll
  public static void setup() {

    Channel channel = null;
    String ccName = "fabcar";
    String ccVersion = "6";
    String channelName = "working-channel";


    final HFClient hfClient;
    try {
      NetworkConfig networkConfig = NetworkConfig.fromYamlFile(new File("/Users/albertlacambra/git/lacambra.tech/fabric-toolbox/stateless-client/src/test/resources/network-config.yaml"));
      hfClient = getTheClient(networkConfig);
      channel = hfClient.loadChannelFromConfig(channelName, networkConfig);
      channel.initialize();

    } catch (InvalidArgumentException | NetworkConfigurationException | TransactionException | IOException e) {
      throw new RuntimeException(e);
    }

    ChaincodeClient client = new ChaincodeClient().withStatelessClient(new StatelessClient())
        .withHFCLient(hfClient)
        .withChaincodeId(ccName, ccVersion)
        .withOrderers(channel.getOrderers())
        .withPeers(channel.getPeers())
        .withChannel(channel)
        .withResponsesValidator(new PeerTransactionValidator(new SDKUtilsWrapper()));

    cut = new FabcarClient(client);

  }

  @Test
  public void t() {
    cut.initLedger().join();
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