package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.protos.peer.FabricTransaction;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StatelessClient {

  private static final Logger logger = Logger.getLogger(StatelessClient.class.getName());
  private final ScheduledThreadPoolExecutor delayer;
  private final ExecutorService executorService;
  private final Duration timeout;

  public StatelessClient(ExecutorService executorService) {
    this.executorService = executorService;
    delayer = new ScheduledThreadPoolExecutor(1);
    timeout = Duration.ofSeconds(1);
  }

  public StatelessClient() {
    this(ForkJoinPool.commonPool());
  }

  /**
   * @param chaincodeID
   * @param functionName
   * @param params
   * @param orderers
   * @param channel
   * @param client
   * @param responsesValidator
   * @return
   */
  public CompletableFuture<FabricTxInfo> invokeChaincode(ChaincodeID chaincodeID, String functionName, Collection<String> params, Collection<Orderer> orderers, Channel channel, HFClient client, PeerTransactionValidator responsesValidator) {

    TransactionProposalRequest transactionProposalRequest = createTransactionProposalRequest(chaincodeID, functionName, new ArrayList<>(params), client);

    return CompletableFuture
        .supplyAsync(() -> getProposalResponses(channel, transactionProposalRequest), executorService)
        .applyToEither(timeoutAfter(timeout.getSeconds(), TimeUnit.SECONDS), responsesValidator::validate)
        .applyToEither(timeoutAfter(timeout.getSeconds(), TimeUnit.SECONDS), simulationInfo -> sendToOrdererIfSucced(simulationInfo, orderers, channel))
        .thenCompose(fabricTxInfo -> fabricTxInfo)
        .exceptionally(this::createExceptionalResponse);
  }

  private FabricTxInfo createExceptionalResponse(Throwable throwable) {

    if (throwable instanceof StatelessClientException) {
      return FabricTxInfo.createFailedResult((StatelessClientException) throwable);
    } else {
      return FabricTxInfo.createFailedResult(new StatelessClientException(throwable));
    }
  }


  private CompletableFuture<FabricTxInfo> sendToOrdererIfSucced(SimulationInfo simulationInfo, Collection<Orderer> orderers, Channel channel) {
    if (simulationInfo.simulationsSucceed()) {
      return sendTransactionToOrderer(simulationInfo, orderers, channel).thenApply(transactionEvent -> this.createTransactionResult(transactionEvent, simulationInfo, channel));
    } else {
      return CompletableFuture.completedFuture(FabricTxInfo.createFailedResult(simulationInfo.getTransactionId(), simulationInfo.getMessage(), simulationInfo));
    }
  }

  private Collection<ProposalResponse> getProposalResponses(Channel channel, TransactionProposalRequest transactionProposalRequest) {

    Collection<ProposalResponse> proposalResponses;
    try {
      proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
    } catch (ProposalException e) {
      throw new StatelessClientException(e);
    } catch (InvalidArgumentException e) {
      throw new FabricClientException(e);
    }

    return proposalResponses;
  }

  /**
   * @param chaincodeID
   * @param functionName
   * @param params
   * @param channel
   * @param client
   * @param responsesValidator
   * @return
   */
  public CompletableFuture<SimulationInfo> query(ChaincodeID chaincodeID, String functionName, Collection<String> params, Channel channel, HFClient client, PeerTransactionValidator responsesValidator) {
    Objects.requireNonNull(channel);
    Objects.requireNonNull(chaincodeID);
    Objects.requireNonNull(client);
    Objects.requireNonNull(params);

    return CompletableFuture
        .supplyAsync(() -> queryChaincodeBlocking(chaincodeID, functionName, params, channel, client, responsesValidator), executorService)
        .exceptionally(throwable -> SimulationInfo.createInvalid(throwable.getMessage()));
  }

  public SimulationInfo queryChaincodeBlocking(ChaincodeID chaincodeID, String functionName, Collection<String> params, Channel channel, HFClient client, PeerTransactionValidator responsesValidator) {

    Objects.requireNonNull(channel);
    Objects.requireNonNull(chaincodeID);
    Objects.requireNonNull(client);
    Objects.requireNonNull(params);

    QueryByChaincodeRequest queryByChaincodeRequest = createQueryByChaincodeRequest(chaincodeID, functionName, new ArrayList<>(params), client);

    try {
      Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest);
      return responsesValidator.validate(queryProposals);

    } catch (InvalidArgumentException ex) {
      throw new IllegalArgumentException(ex);
    } catch (ProposalException e) {
      throw new StatelessClientException(e);
    }
  }

  private FabricTxInfo createTransactionResult(BlockEvent.TransactionEvent transactionEvent, SimulationInfo simulationInfo, Channel channel) {
    byte code = transactionEvent.getValidationCode();
    FabricTransaction.TxValidationCode validationCode = FabricTransaction.TxValidationCode.forNumber(code);
    FabricTxInfo result;
    if (validationCode == FabricTransaction.TxValidationCode.VALID) {
      result = FabricTxInfo.createSuccessfulResult(
          () -> getBlockInfoForTransactionId(transactionEvent.getTransactionID(), channel),
          transactionEvent,
          simulationInfo);
    } else {
      result = FabricTxInfo.createFailedResult(transactionEvent.getTransactionID(), "Tx Error. Returned code is " + validationCode, simulationInfo);
    }

    return result;
  }

  private BlockInfo getBlockInfoForTransactionId(String transactionId, Channel channel) {
    try {
      return channel.queryBlockByTransactionID(transactionId);
    } catch (InvalidArgumentException | ProposalException e) {
      throw new FabricClientException(e);
    }
  }

  public CompletableFuture<BlockEvent.TransactionEvent> sendTransactionToOrderer(SimulationInfo simulationInfo, Collection<Orderer> orderers, Channel channel) {

    if (!simulationInfo.simulationsSucceed()) {
      logger.warning("[sendTransactionToOrderer] Trying to send an unsuccessful proposal. Ignoring it...");
      CompletableFuture<BlockEvent.TransactionEvent> cf = new CompletableFuture<>();
      cf.completeExceptionally(new RuntimeException("proposal has failed:" + simulationInfo.getMessage()));
      return cf;
    }

    return channel.sendTransaction(simulationInfo.getSuccessfulProposals(), orderers);
  }

  private TransactionProposalRequest createTransactionProposalRequest(ChaincodeID chaincodeID, String functionName, ArrayList<String> args, HFClient client) {
    TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
    transactionProposalRequest.setChaincodeID(chaincodeID);
    transactionProposalRequest.setFcn(functionName);
    transactionProposalRequest.setArgs(args);

    Map<String, byte[]> transientProposalData = new HashMap<>();
    transientProposalData.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
    transientProposalData.put("method", "TransactionProposalRequest".getBytes(UTF_8));
    transientProposalData.put("result", ":)".getBytes(UTF_8));

    try {
      transactionProposalRequest.setTransientMap(transientProposalData);
    } catch (InvalidArgumentException e) {
      throw new FabricClientException(e);
    }

    return transactionProposalRequest;
  }


  private QueryByChaincodeRequest createQueryByChaincodeRequest(ChaincodeID chaincodeID, String functionName, ArrayList<String> args, HFClient client) {
    QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
    queryByChaincodeRequest.setArgs(args);
    queryByChaincodeRequest.setFcn(functionName);
    queryByChaincodeRequest.setChaincodeID(chaincodeID);
    queryByChaincodeRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);

    //TODO: is that really needed?
    Map<String, byte[]> transientProposalData = new HashMap<>();
    transientProposalData.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
    transientProposalData.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));

    try {
      queryByChaincodeRequest.setTransientMap(transientProposalData);
    } catch (InvalidArgumentException e) {
      throw new FabricClientException(e);
    }

    return queryByChaincodeRequest;
  }

  public <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
    CompletableFuture<T> result = new CompletableFuture<>();
    delayer.schedule(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);
    return result;
  }

}
