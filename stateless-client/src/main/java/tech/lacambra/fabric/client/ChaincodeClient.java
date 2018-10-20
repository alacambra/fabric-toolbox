package tech.lacambra.fabric.client;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import tech.lacambra.fabric.client.stateless.FabricTxInfo;
import tech.lacambra.fabric.client.stateless.PeerTransactionValidator;
import tech.lacambra.fabric.client.stateless.SimulationInfo;
import tech.lacambra.fabric.client.stateless.StatelessClient;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ChaincodeClient {

  private ChaincodeID chaincodeID;
  private String functionName;
  private Collection<String> params;
  private Collection<Orderer> orderers;
  private Channel channel;
  private HFClient client;
  private PeerTransactionValidator responsesValidator;
  private StatelessClient statelessClient;


  public ChaincodeClient(StatelessClient statelessClient) {
    this.statelessClient = statelessClient;
  }

  public ChaincodeClient withChaincodeId(String ccName, String ccVersion) {
    chaincodeID = ChaincodeID.newBuilder().setName(ccName).setVersion(ccVersion).build();
    return this;
  }

  public ChaincodeClient withFunction(String functionName) {
    this.functionName = functionName;
    return this;
  }

  public ChaincodeClient withParams(Collection<String> params) {
    this.params = params;
    return this;
  }

  public ChaincodeClient withOrderers(Collection<Orderer> orderers) {
    this.orderers = orderers;
    return this;
  }

  public ChaincodeClient withPeers(String ccName, String ccVersion) {
    chaincodeID = ChaincodeID.newBuilder().setName(ccName).setVersion(ccVersion).build();
    return this;
  }

  public ChaincodeClient withHFCLient(HFClient client) {
    this.client = client;
    return this;
  }

  public ChaincodeClient withStatelessClient(StatelessClient statelessClient) {
    this.statelessClient = statelessClient;
    return this;
  }

  public ChaincodeClient withResponsesValidator(PeerTransactionValidator responsesValidator) {
    this.responsesValidator = responsesValidator;
    return this;
  }

  public ChaincodeClient withChannel(Channel channel) {
    this.channel = channel;
    return this;
  }

  public CompletableFuture<FabricTxInfo> invoke() {
    return statelessClient.invokeChaincode(chaincodeID, functionName, params, orderers, channel, client, responsesValidator);
  }

  public CompletableFuture<FabricTxInfo> invoke(String functionName, Collection<String> params) {
    return statelessClient.invokeChaincode(chaincodeID, functionName, params, orderers, channel, client, responsesValidator);
  }

  public CompletableFuture<FabricTxInfo> invoke(String ccName, String ccVersion, String functionName, Collection<String> params) {
    return statelessClient.invokeChaincode(
        ChaincodeID.newBuilder().setVersion(ccVersion).setName(ccName).build(), functionName, params, orderers, channel, client, responsesValidator);
  }

  public CompletableFuture<SimulationInfo> query() {
    return statelessClient.query(chaincodeID, functionName, params, channel, client, responsesValidator);
  }

  public CompletableFuture<SimulationInfo> query(String functionName, Collection<String> params) {
    return statelessClient.query(chaincodeID, functionName, params, channel, client, responsesValidator);
  }

  public CompletableFuture<SimulationInfo> query(String ccName, String ccVersion, String functionName, Collection<String> params) {
    return statelessClient.query(
        ChaincodeID.newBuilder().setVersion(ccVersion).setName(ccName).build(), functionName, params, channel, client, responsesValidator);
  }
}
