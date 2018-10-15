package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChaincodeManager {

  private static final Logger LOGGER = Logger.getLogger(ChaincodeManager.class.getName());

  private ExecutorService executorService = new ForkJoinPool(4);
  private Duration deployCCTimeout = Duration.ofMinutes(1);

  private StatelessClient chaincodeClient;
  private final SDKUtilsWrapper sdkUtilsWrapper;


  public ChaincodeManager(StatelessClient chaincodeClient, SDKUtilsWrapper sdkUtilsWrapper) {
    this.chaincodeClient = Objects.requireNonNull(chaincodeClient);
    this.sdkUtilsWrapper = Objects.requireNonNull(sdkUtilsWrapper);
  }

  public ChaincodeManager(StatelessClient chaincodeClient) {
    this(chaincodeClient, new SDKUtilsWrapper());
  }

  public Map<Peer, ChaincodeDeploymentInfo> deployChaincodeBlocking(String ccName, String ccVersion, String chaincodeSourceLocation, Collection<Peer> peers, HFClient client, TransactionRequest.Type type) {

    Objects.requireNonNull(ccName);
    Objects.requireNonNull(ccVersion);
    Objects.requireNonNull(chaincodeSourceLocation);
    Objects.requireNonNull(client);
    Objects.requireNonNull(type);

    try {
      return deployChaincode(ccName, ccVersion, chaincodeSourceLocation, peers, client, type).get(deployCCTimeout.getSeconds(), TimeUnit.SECONDS);
    } catch (TimeoutException | InterruptedException | ExecutionException e) {
      throw new ChaincodeDeploymentException(e);
    }
  }

  public CompletableFuture<Map<Peer, ChaincodeDeploymentInfo>> deployChaincode(String ccName, String ccVersion, String chaincodeSourceLocation, Collection<Peer> peers, HFClient client, TransactionRequest.Type type) {

    Objects.requireNonNull(ccName);
    Objects.requireNonNull(ccVersion);
    Objects.requireNonNull(chaincodeSourceLocation);
    Objects.requireNonNull(client);
    Objects.requireNonNull(type);

    ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(ccName).setVersion(ccVersion).build();

    if (peers == null || peers.isEmpty()) {
      throw new IllegalArgumentException("no peers given");
    }

    Set<Peer> toDeployPeers = new HashSet<>(peers);

    final Map<Peer, ChaincodeDeploymentInfo> results = new ConcurrentHashMap<>(peers.size());

    Future<?> f = executorService.submit(() ->
        toDeployPeers
            .parallelStream()
            .forEach(peer -> {
                  ChaincodeDeploymentInfo result = deployChaincode(chaincodeID, chaincodeSourceLocation, peer, client, type);
                  results.put(peer, result);
                }
            )
    );

    return CompletableFuture.supplyAsync(() -> {
      try {
        f.get(deployCCTimeout.getSeconds(), TimeUnit.SECONDS);
        return results;
      } catch (TimeoutException | InterruptedException | ExecutionException e) {
        throw new ChaincodeDeploymentException(e);
      }
    }, executorService);
  }

  public ChaincodeDeploymentInfo deployChaincode(ChaincodeID chaincodeID, String chaincodeSourceLocation, Peer peer, HFClient client, TransactionRequest.Type type) {

    Objects.requireNonNull(chaincodeID);
    Objects.requireNonNull(chaincodeSourceLocation);
    Objects.requireNonNull(client);
    Objects.requireNonNull(peer);
    Objects.requireNonNull(type);

    if (isChaincodeDeployed(peer, chaincodeID, client)) {
      return ChaincodeDeploymentInfo.alreadyDeployed(chaincodeID);
    }


    Collection<ProposalResponse> responses;

    LOGGER.info("[deployChaincode] Installing chaincode. Preparing Request Structure to install chaincode");
    InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
    installProposalRequest.setChaincodeID(chaincodeID);

    try {
      installProposalRequest.setChaincodeSourceLocation(new File(chaincodeSourceLocation));
    } catch (InvalidArgumentException e) {
      LOGGER.warning("[deployChaincode] Problems loading ccSourceLocation. Error=" + e.getMessage());
    }

    installProposalRequest.setChaincodeLanguage(type);
    installProposalRequest.setChaincodeVersion(chaincodeID.getVersion());

    try {
      responses = client.sendInstallProposal(installProposalRequest, Collections.singletonList(peer));

      if (responses.isEmpty()) {
        //TODO: when could not come?
        return null;
      } else {
        ProposalResponse response = responses.iterator().next();
        return ChaincodeDeploymentInfo.fromProposalResponse(response);
      }
    } catch (ProposalException | InvalidArgumentException e) {
      throw new FabricClientException(e);
    }
  }

  public Map<Peer, SimulationInfo> instantiateOrUpgradeChaincode(
      ChaincodeID chaincodeID,
      Collection<Peer> peers,
      Collection<Orderer> orderers,
      Channel channel,
      String endorsementPolicyConfigFile,
      HFClient client,
      TransactionRequest.Type type) {

    Objects.requireNonNull(chaincodeID);
    Objects.requireNonNull(channel);
    Objects.requireNonNull(client);
    Objects.requireNonNull(endorsementPolicyConfigFile);

    if (peers == null || peers.isEmpty()) {
      throw new FabricClientException("No peers given");
    }

    if (orderers == null || orderers.isEmpty()) {
      throw new FabricClientException("No orderer given");
    }

    Map<Peer, SimulationInfo> results = new HashMap<>(peers.size());

    for (Peer p : peers) {

      SimulationInfo result = instantiateOrUpgradeChaincode(chaincodeID, p, orderers, channel, endorsementPolicyConfigFile, client, type);
      results.put(p, result);

    }

    return results;
  }

  public SimulationInfo instantiateOrUpgradeChaincode(
      ChaincodeID chaincodeID,
      Peer peer,
      Collection<Orderer> orderers,
      Channel channel,
      String endorsementPolicyConfigFile,
      HFClient client,
      TransactionRequest.Type type) {

    Collection<ProposalResponse> responses;
    boolean shouldUpgrade;
    boolean shouldInstantiate;

        /*
        Fetch all chaincodes of peer that matches with the given name. It may have several versions
         */
    Set<Query.ChaincodeInfo> chaincodeInfos = getInstantiatedChaincodeOfChannel(peer, channel)
        .stream()
        .filter(chaincodeInfo -> chaincodeInfo.getName().equals(chaincodeID.getName()))
        .collect(Collectors.toSet());


    shouldInstantiate = chaincodeInfos.isEmpty();
    shouldUpgrade = !shouldInstantiate && chaincodeInfos
        .stream()
        .noneMatch(chaincodeInfo -> chaincodeInfo.getVersion().equals(chaincodeID.getVersion()));

    try {
      if (shouldInstantiate) {
        LOGGER.info("[instantiateOrUpgradeChaincode] Chaincode will be instantiated. ChaincodeID=" + Printer.toString(chaincodeID));
        InstantiateProposalRequest instantiateProposalRequest = instantiateChainCodeProposalRequest(chaincodeID, endorsementPolicyConfigFile, client, type);
        //TODO: instead one peer the whole list of selected peers could be passed
        responses = channel.sendInstantiationProposal(instantiateProposalRequest, Collections.singleton(peer));
      } else if (shouldUpgrade) {
        LOGGER.info("[instantiateOrUpgradeChaincode] Chaincode will be updated. ChaincodeID=" + Printer.toString(chaincodeID));
        UpgradeProposalRequest upgradeProposalRequest = upgradeChainCodeProposalRequest(chaincodeID, endorsementPolicyConfigFile, client, type);
        responses = channel.sendUpgradeProposal(upgradeProposalRequest);
      } else {
        LOGGER.info("[instantiateOrUpgradeChaincode] no need to instantiateChaincode or upgrade. Chaincode exists on the correct version");
        return SimulationInfo.createValid("no need to instantiateChaincode or upgrade. Chaincode exists on the correct version");
      }
    } catch (ProposalException | InvalidArgumentException e) {
      LOGGER.warning("[instantiateOrUpgradeChaincode] An error happens. Error=" + e.getMessage());
      return SimulationInfo.createInvalid(e.getMessage());
    }

    SimulationInfo responsesValidatorResult = new PeerTransactionValidator(sdkUtilsWrapper).validate(responses);

    if (responsesValidatorResult.canBeSendToOrderer()) {
      chaincodeClient.sendTransactionToOrderer(responsesValidatorResult, orderers, channel);
    }

    return responsesValidatorResult;
  }

  public Set<Query.ChaincodeInfo> getChaincodeInfoOfPeer(Peer peer, HFClient client) {
    Set<Query.ChaincodeInfo> chaincodeInfos = Collections.emptySet();
    try {
      chaincodeInfos = new HashSet<>(client.queryInstalledChaincodes(peer));
    } catch (InvalidArgumentException | ProposalException e) {
      LOGGER.warning("[getChaincodeInfoOfPeer] Exception when fetching chaincodes of peer="
          + peer.getName()
          + ". Error=" + e.getMessage()
      );
    }
    return chaincodeInfos;
  }

  public Set<Query.ChaincodeInfo> getInstantiatedChaincodeOfChannel(Peer peer, Channel channel) {
    Set<Query.ChaincodeInfo> chaincodeInfos = Collections.emptySet();
    try {
      chaincodeInfos = new HashSet<>(channel.queryInstantiatedChaincodes(peer));
    } catch (InvalidArgumentException | ProposalException e) {
      LOGGER.warning("[getInstantiatedChaincodeOfChannel] Exception when fetching chaincodes of peer="
          + peer.getName()
          + ". Error=" + e.getMessage()
      );
    }
    return chaincodeInfos;
  }

  public boolean isChaincodeDeployed(Peer peer, ChaincodeID chaincodeID, HFClient client) {

    String ccName = chaincodeID.getName();
    String ccPath = chaincodeID.getPath();
    String ccVersion = chaincodeID.getVersion();

    String printableCCId = String.format(
        "chaincode: %s, at version: %s, on peer: %s"
        , ccName, ccVersion, peer.getName());

    LOGGER.info("[isChaincodeDeployed] Checking cc " + printableCCId);

    Set<Query.ChaincodeInfo> chaincodeInfos;
    chaincodeInfos = getChaincodeInfoOfPeer(peer, client);

    boolean found = false;

    for (Query.ChaincodeInfo chaincodeInfo : chaincodeInfos) {
      found = ccName.equals(chaincodeInfo.getName()) && ccPath.equals(chaincodeInfo.getPath()) && ccVersion.equals(chaincodeInfo.getVersion());
      if (found) {
        break;
      }
    }

    LOGGER.info("[isChaincodeDeployed] Chaincode found=" + found + ", " + printableCCId);
    return found;
  }

  public boolean isChaincodeOnCurrentVersion(ChaincodeID chaincodeID, Query.ChaincodeInfo chaincodeInfo) {
    return chaincodeInfo.getVersion().equals(chaincodeID.getVersion());
  }

  private <T extends TransactionRequest> T prepareGenericProposalRequest(
      ChaincodeID chaincodeID,
      Supplier<T> proposalRequestSupplier,
      String endorsementPolicyConfigFile,
      User user,
      TransactionRequest.Type type) {

    T proposalRequest = prepareGenericProposalRequest(chaincodeID, user, proposalRequestSupplier, type);
    ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = getChaincodeEndorsementPolicy(endorsementPolicyConfigFile);
    if (chaincodeEndorsementPolicy != null) {
      proposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
    }

    return proposalRequest;
  }

  private <T extends TransactionRequest> T prepareGenericProposalRequest(
      ChaincodeID chaincodeID,
      User user,
      Supplier<T> proposalRequestSupplier,
      TransactionRequest.Type type
  ) {

    T proposalRequest = proposalRequestSupplier.get();

    proposalRequest.setChaincodeID(chaincodeID);
    proposalRequest.setChaincodeLanguage(type);
    proposalRequest.setArgs(new ArrayList<>(0));
    proposalRequest.setUserContext(user);

    return proposalRequest;
  }

  private ChaincodeEndorsementPolicy getChaincodeEndorsementPolicy(String endorsementPolicyConfigFile) {
    if (endorsementPolicyConfigFile == null || endorsementPolicyConfigFile.isEmpty()) {
      return null;
    }
    return getChaincodeEndorsementPolicy(new File(endorsementPolicyConfigFile));
  }

  private ChaincodeEndorsementPolicy getChaincodeEndorsementPolicy(File endorsementPolicyConfigFile) {
    ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
    try {
      chaincodeEndorsementPolicy.fromYamlFile(endorsementPolicyConfigFile);
      LOGGER.info("[getChaincodeEndorsementPolicy] Policies correctly loaded. FileName=" + endorsementPolicyConfigFile);
    } catch (IOException | ChaincodeEndorsementPolicyParseException e) {
      throw new FabricClientException(e);
    }

    return chaincodeEndorsementPolicy;
  }

  private UpgradeProposalRequest upgradeChainCodeProposalRequest(
      ChaincodeID chaincodeID,
      String endorsementPolicyConfigFile,
      HFClient client,
      TransactionRequest.Type type) {


    return prepareGenericProposalRequest(chaincodeID, client::newUpgradeProposalRequest, endorsementPolicyConfigFile, client.getUserContext(), type);
  }

  private InstantiateProposalRequest instantiateChainCodeProposalRequest(
      ChaincodeID chaincodeID,
      String endorsementPolicyConfigFile,
      HFClient client,
      TransactionRequest.Type type) {

    InstantiateProposalRequest instantiateProposalRequest =
        prepareGenericProposalRequest(chaincodeID, client::newInstantiationProposalRequest, endorsementPolicyConfigFile, client.getUserContext(), type);

    Map<String, byte[]> tm = new HashMap<>();
    tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
    tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));

    try {
      instantiateProposalRequest.setTransientMap(tm);
    } catch (InvalidArgumentException e) {
      LOGGER.warning("[upgradeChainCodeProposalRequest] Problems setting TransientMap. Error=" + e.getMessage());
    }

    return instantiateProposalRequest;
  }
}