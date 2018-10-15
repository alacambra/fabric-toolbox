package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.ProposalResponse;

import java.util.Objects;

public class ChaincodeDeploymentInfo {

  private static final String ALREADY_DEPLOYED_MESSAGE = "Chaincode already deployed";
  private final boolean successful;
  private final String message;
  private ProposalResponse proposalResponse;
  private Exception exception;
  private ChaincodeID chaincodeID;

  private ChaincodeDeploymentInfo(ProposalResponse proposalResponse) {
    this.proposalResponse = Objects.requireNonNull(proposalResponse);
    successful = proposalResponse.getStatus() == ChaincodeResponse.Status.SUCCESS;
    message = extractMessageFromProposal(proposalResponse);
  }


  private ChaincodeDeploymentInfo(Exception exception) {
    this.exception = Objects.requireNonNull(exception);
    successful = false;
    message = exception.getMessage();
  }

  public ChaincodeDeploymentInfo(ChaincodeID chaincodeID) {
    successful = true;
    this.chaincodeID = chaincodeID;
    message = ALREADY_DEPLOYED_MESSAGE + ". ChaincodeName=" + chaincodeID.getName() + ", ChaincodeVersion=" + chaincodeID.getVersion();
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


  public static ChaincodeDeploymentInfo fromProposalResponse(ProposalResponse proposalResponse) {
    Objects.requireNonNull(proposalResponse);
    return new ChaincodeDeploymentInfo(proposalResponse);
  }

  public static ChaincodeDeploymentInfo fromError(Exception e) {
    Objects.requireNonNull(e);
    return new ChaincodeDeploymentInfo(e);
  }

  public static ChaincodeDeploymentInfo alreadyDeployed(ChaincodeID chaincodeID) {
    Objects.requireNonNull(chaincodeID);
    return new ChaincodeDeploymentInfo(chaincodeID);
  }
}
