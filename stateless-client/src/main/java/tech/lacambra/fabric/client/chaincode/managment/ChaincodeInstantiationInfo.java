package tech.lacambra.fabric.client.chaincode.managment;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.ProposalResponse;
import tech.lacambra.fabric.client.Printer;

import java.util.Objects;

public class ChaincodeInstantiationInfo {

  private final boolean successful;
  private final String message;
  private final boolean alreadyDeployed;
  private BlockEvent.TransactionEvent transactionEvent;
  private ProposalResponse proposalResponse;
  private Exception exception;

  private ChaincodeInstantiationInfo(ProposalResponse proposalResponse) {
    this.proposalResponse = Objects.requireNonNull(proposalResponse);
    // Install will have no signing cause it's not really targeted to a channel. So not needed to call proposalResponse.isVerified()
    successful = proposalResponse.getStatus() == ChaincodeResponse.Status.SUCCESS;
    message = extractMessageFromProposal(proposalResponse);
    alreadyDeployed = false;
  }

  private ChaincodeInstantiationInfo(ChaincodeInstantiationInfo chaincodeInstantiationInfo, BlockEvent.TransactionEvent transactionEvent) {
    this(chaincodeInstantiationInfo.getProposalResponse());
    this.transactionEvent = transactionEvent;
  }

  private ChaincodeInstantiationInfo(ChaincodeInstantiationInfo chaincodeInstantiationInfo, Exception e) {
    this(chaincodeInstantiationInfo.getProposalResponse());
    this.exception = e;
  }

  private ChaincodeInstantiationInfo(Exception exception) {
    this.exception = Objects.requireNonNull(exception);
    successful = false;
    message = exception.getMessage();
    alreadyDeployed = false;
  }

  private ChaincodeInstantiationInfo(ChaincodeID chaincodeID) {
    successful = true;
    message = String.format("Chaincode %s:%s was already deployed", chaincodeID.getName(), chaincodeID.getVersion());
    alreadyDeployed = true;
  }

  public ProposalResponse getProposalResponse() {
    return proposalResponse;
  }

  public Exception getException() {
    return exception;
  }

  public boolean peerInstantiationSucceed() {
    return successful;
  }

  public boolean instatiationTransactionSucceed() {
    return transactionEvent != null && transactionEvent.isValid();
  }

  public boolean isSuccessful() {
    return peerInstantiationSucceed() && instatiationTransactionSucceed();
  }

  public boolean isAlreadyDeployed() {
    return alreadyDeployed;
  }

  public BlockEvent.TransactionEvent getTransactionEvent() {
    return transactionEvent;
  }

  public String getMessage() {
    return message;
  }

  public ChaincodeInstantiationInfo fromOrdererResponse(BlockEvent.TransactionEvent transactionEvent) {
    return new ChaincodeInstantiationInfo(this, transactionEvent);
  }

  public ChaincodeInstantiationInfo fromOrdererError(Exception exception) {
    return new ChaincodeInstantiationInfo(this, transactionEvent);
  }

  private String extractMessageFromProposal(ProposalResponse proposalResponse) {
    if (proposalResponse.getProposalResponse() == null) {
      return "No response received. Peer exists? Peer=" + Printer.toString(proposalResponse.getPeer());
    }

    return proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8() + ":" + proposalResponse.getProposalResponse().getResponse().getMessage() + ":" + proposalResponse.getProposalResponse().getPayload().toStringUtf8();
  }


  public static ChaincodeInstantiationInfo fromProposalResponse(ProposalResponse proposalResponse) {
    Objects.requireNonNull(proposalResponse);
    return new ChaincodeInstantiationInfo(proposalResponse);
  }

  public static ChaincodeInstantiationInfo fromError(Exception e) {
    Objects.requireNonNull(e);
    return new ChaincodeInstantiationInfo(e);
  }

  public static ChaincodeInstantiationInfo alreadyDeployed(ChaincodeID chaincodeID) {
    Objects.requireNonNull(chaincodeID);
    return new ChaincodeInstantiationInfo(chaincodeID);
  }
}
