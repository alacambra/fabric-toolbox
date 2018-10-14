package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.ProposalResponse;

import java.util.Objects;

public class ChaincodeDeploymentInfo {

  private static final String ALREADY_DEPLOYED_MESSAGE = "Chaincode already deployed";
  private ProposalResponse proposalResponse;
  private Exception exception;
  private ChaincodeID chaincodeID;

  private ChaincodeDeploymentInfo(ProposalResponse proposalResponse) {
    this.proposalResponse = Objects.requireNonNull(proposalResponse);
  }


  private ChaincodeDeploymentInfo(Exception exception) {
    this.exception = Objects.requireNonNull(exception);
  }

  public ChaincodeDeploymentInfo(ChaincodeID chaincodeID) {
    this.chaincodeID = chaincodeID;
  }


  public boolean deploymentSucced() {
    return exception == null && proposalResponse.isVerified() && proposalResponse.getStatus() == ChaincodeResponse.Status.SUCCESS;
  }

  public String getMessage() {
    if (exception != null) {
      return exception.getMessage();
    } else if (proposalResponse != null) {
      if (proposalResponse.getProposalResponse() == null) {
        return "No response received. Peer exists? Peer=" + Printer.toString(proposalResponse.getPeer());
      }
      return proposalResponse.getProposalResponse().getPayload().toStringUtf8();
    } else {
      return ALREADY_DEPLOYED_MESSAGE + ". ChaincodeName=" + chaincodeID.getName() + ", ChaincodeVersion=" + chaincodeID.getVersion();
    }
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
