package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.protos.peer.FabricTransaction;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FabricTxInfo {

  static final String NO_BLOCK_INFO_SUPPLIER_MEESAGE = "No blockInfoSupplier available. Not possible to deliver BlockId";

  private final Supplier<BlockInfo> blockInfoSupplier;
  private final BlockEvent.TransactionEvent transactionEvent;
  private final StatelessClientException fabricException;
  private String transactionId;
  private String message;
  private SimulationInfo proposalsResult;

  private FabricTxInfo(Supplier<BlockInfo> blockInfoSupplier, BlockEvent.TransactionEvent transactionEvent, SimulationInfo proposalsResult) {
    this.blockInfoSupplier = blockInfoSupplier;
    this.transactionEvent = transactionEvent;
    this.transactionId = transactionEvent.getTransactionID();
    this.proposalsResult = proposalsResult;
    this.fabricException = null;
  }

  private FabricTxInfo(String transactionId, String message, SimulationInfo proposalsResult) {
    blockInfoSupplier = null;
    transactionEvent = null;
    this.transactionId = transactionId;
    this.message = message;
    this.proposalsResult = proposalsResult;
    this.fabricException = null;
  }

  public FabricTxInfo(StatelessClientException fabricException) {
    this.fabricException = fabricException;
    this.message = fabricException.getMessage();
    blockInfoSupplier = null;
    transactionEvent = null;
    this.transactionId = null;
    this.proposalsResult = null;

  }

  public boolean isSuccessful() {
    return transactionEvent != null && FabricTransaction.TxValidationCode.forNumber(transactionEvent.getValidationCode()) == FabricTransaction.TxValidationCode.VALID;
  }

  public CompletableFuture<Long> getBlockId() {

    if (blockInfoSupplier == null) {
      CompletableFuture<Long> completableFuture = new CompletableFuture<>();
      completableFuture.completeExceptionally(new RuntimeException(NO_BLOCK_INFO_SUPPLIER_MEESAGE));
      return completableFuture;
    }

    return CompletableFuture.supplyAsync(blockInfoSupplier).thenApply(BlockInfo::getBlockNumber);
  }

  public String getTransactionId() {
    return transactionId;
  }

  public Optional<StatelessClientException> getFabricException() {
    return Optional.ofNullable(fabricException);
  }

  public String getMessage() {
    return message;
  }

  public SimulationInfo getProposalsResult() {
    return proposalsResult;
  }

  public String getChainCodeResponse() {
    if (proposalsResult == null) {
      return "";
    }

    if (proposalsResult.simulationsSucceed()) {
      return proposalsResult.getSuccessfulProposals().stream().findAny().get().getProposalResponse().getResponse().getPayload().toStringUtf8();
    } else {
      return proposalsResult.getSuccessfulProposals().stream()
          .findAny()
          .flatMap(pr -> Optional.ofNullable(pr.getProposalResponse()))
          .flatMap(proposalResponse -> Optional.ofNullable(proposalResponse.getResponse().getPayload().toStringUtf8()))
          .orElse("No response found");
    }
  }

  @Override
  public String toString() {
    return "tech.lacambra.fabric.client.stateless.TransactionResult{" +
        ", transactionId='" + transactionId + '\'' +
        ", fabricException=" + fabricException +
        ", isSuccessful=" + isSuccessful() +
        '}';
  }

  public static FabricTxInfo createSuccessfulResult(Supplier<BlockInfo> blockInfoSupplier, BlockEvent.TransactionEvent transactionEvent, SimulationInfo proposalsResult) {
    Objects.requireNonNull(blockInfoSupplier, "blockInfoSupplier must be given");
    Objects.requireNonNull(transactionEvent, "transactionEvent must be given");
    return new FabricTxInfo(blockInfoSupplier, transactionEvent, proposalsResult);
  }

  public static FabricTxInfo createFailedResult(String transactionId, String message, SimulationInfo proposalsResult) {
    Objects.requireNonNull(transactionId, "transactionId must be given");
    Objects.requireNonNull(message, "message must be given");
    return new FabricTxInfo(transactionId, message, proposalsResult);
  }

  public static FabricTxInfo createFailedResult(StatelessClientException fabricException) {
    Objects.requireNonNull(fabricException, "exception must be given");
    return new FabricTxInfo(fabricException);
  }
}
