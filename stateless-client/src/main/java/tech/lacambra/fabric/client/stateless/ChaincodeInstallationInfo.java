package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.ProposalResponse;

import java.util.Objects;

public class ChaincodeInstallationInfo {

  private static final String ALREADY_DEPLOYED_MESSAGE = "Chaincode already deployed";
  private final boolean successful;
  private final String message;
  private ProposalResponse proposalResponse;
  private Exception exception;

  private ChaincodeInstallationInfo(ProposalResponse proposalResponse) {
    this.proposalResponse = Objects.requireNonNull(proposalResponse);
    // Install will have no signing cause it's not really targeted to a channel. So not needed to call proposalResponse.isVerified()
    successful = proposalResponse.getStatus() == ChaincodeResponse.Status.SUCCESS;
    message = extractMessageFromProposal(proposalResponse);
  }


  private ChaincodeInstallationInfo(Exception exception) {
    this.exception = Objects.requireNonNull(exception);
    successful = false;
    message = exception.getMessage();
  }

  private ChaincodeInstallationInfo(ChaincodeID chaincodeID) {
    successful = true;
    message = ALREADY_DEPLOYED_MESSAGE + ". ChaincodeName=" + chaincodeID.getName() + ", ChaincodeVersion=" + chaincodeID.getVersion();
  }

  public ProposalResponse getProposalResponse() {
    return proposalResponse;
  }

  public Exception getException() {
    return exception;
  }

  public boolean deploymentSucceed() {
    return successful;
  }

  public String getMessage() {
    return message;
  }

  private String extractMessageFromProposal(ProposalResponse proposalResponse) {
    if (proposalResponse.getProposalResponse() == null) {
      return "No response received. Peer exists? Peer=" + Printer.toString(proposalResponse.getPeer());
    }

    return proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8() + ":" + proposalResponse.getProposalResponse().getResponse().getMessage() + ":" + proposalResponse.getProposalResponse().getPayload().toStringUtf8();
  }


  public static ChaincodeInstallationInfo fromProposalResponse(ProposalResponse proposalResponse) {
    Objects.requireNonNull(proposalResponse);
    return new ChaincodeInstallationInfo(proposalResponse);
  }

  public static ChaincodeInstallationInfo fromError(Exception e) {
    Objects.requireNonNull(e);
    return new ChaincodeInstallationInfo(e);
  }

  public static ChaincodeInstallationInfo alreadyDeployed(ChaincodeID chaincodeID) {
    Objects.requireNonNull(chaincodeID);
    return new ChaincodeInstallationInfo(chaincodeID);
  }
}
