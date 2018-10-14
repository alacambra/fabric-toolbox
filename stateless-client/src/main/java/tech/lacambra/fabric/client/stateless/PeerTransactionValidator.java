package tech.lacambra.fabric.client.stateless;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.ProposalResponse;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PeerTransactionValidator {

  private static final Logger LOGGER = Logger.getLogger(PeerTransactionValidator.class.getName());
  private SDKUtilsWrapper sdkUtilsWrapper;

  public PeerTransactionValidator(SDKUtilsWrapper sdkUtilsWrapper) {
    this.sdkUtilsWrapper = Objects.requireNonNull(sdkUtilsWrapper);
  }

  public SimulationInfo validate(Collection<ProposalResponse> responses) {

    ValidatorInfoBuilder resultBuilder = new ValidatorInfoBuilder();

    if (responses.isEmpty()) {
      return SimulationInfo.createInvalid("No response received");
    }

    Collection<ProposalResponse> responsesWithBytes = validateResponseBytes(responses);

    if (responsesWithBytes.isEmpty()) {

      String message = responses.stream().map(ChaincodeResponse::getMessage).collect(Collectors.joining(" | "));
      message += " ---> Non of the response has bytes";

      return SimulationInfo.createInvalidConsistencySetsResult(message, responses);
    }

    if (!consistencySetsAreValid(responsesWithBytes)) {
      return SimulationInfo.createInvalidConsistencySetsResult("Invalid sets", responses);
    }

    return classifyResponses(responses, resultBuilder);
  }

  private SimulationInfo classifyResponses(Collection<ProposalResponse> responses, ValidatorInfoBuilder resultBuilder) {

    String transactionId = null;
    ProposalResponse lastResponse = null;
    for (ProposalResponse response : responses) {

      ProposalResponseCheck proposalResponseCheck = validateEndorsement(response, lastResponse);
      lastResponse = proposalResponseCheck.lastValidResponse;

      if (!proposalResponseCheck.responseIsValid) {

        resultBuilder.addInvalidResponse(response);
        continue;

      } else if (proposalResponseCheck.responsesMismatch) {
        return SimulationInfo.createInvalidPayloadsResult(getMismatchingPayloadsErrorMessage(responses), responses);
      }

      if (transactionId != null && !proposalResponseCheck.transactionId.equalsIgnoreCase(transactionId)) {
        return SimulationInfo.createInvalidTransactionIdsResult(getMismatchingIdTxErrorMessage(responses), responses);
      }

      transactionId = proposalResponseCheck.transactionId;
      resultBuilder.addValidEndorsement(response);
    }

    resultBuilder.setTransactionId(transactionId);

    return resultBuilder.build();
  }

  private ProposalResponseCheck validateEndorsement(ProposalResponse response, ProposalResponse lastResponse) {

    ProposalResponseCheck proposalResponseCheck = new ProposalResponseCheck();
    proposalResponseCheck.responseIsValid = response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS;
    proposalResponseCheck.responsesMismatch = proposalResponseCheck.responseIsValid && lastResponse != null;
    proposalResponseCheck.transactionId = response.getTransactionID();
    proposalResponseCheck.lastValidResponse = proposalResponseCheck.responseIsValid ? response : lastResponse;

    return proposalResponseCheck;
  }

  private static class ProposalResponseCheck {
    String transactionId;
    ProposalResponse lastValidResponse;
    boolean responseIsValid;
    boolean responsesMismatch;

  }

  private Collection<ProposalResponse> validateResponseBytes(Collection<ProposalResponse> proposalResponse) {
    return proposalResponse.stream().filter(this::validateResponseBytes).collect(Collectors.toList());
  }

  private boolean validateResponseBytes(ProposalResponse proposalResponse) {

    if (proposalResponse.getProposalResponse() == null) {
      return false;
    }

    ByteString bytes = proposalResponse.getProposalResponse().getPayload();
    return bytes != null && !bytes.isEmpty();
  }

  private boolean consistencySetsAreValid(Collection<ProposalResponse> responses) {
    Collection<Set<ProposalResponse>> proposalConsistencySets = sdkUtilsWrapper.getProposalConsistencySets(responses);
    return proposalConsistencySets.size() == 1;
  }

  private String getMismatchingPayloadsErrorMessage(Collection<ProposalResponse> responses) {
    String peers = responses.stream().map(ProposalResponse::getPeer).map(Printer::toString).collect(Collectors.joining(", "));
    return "Payloads mismatch for peers: " + peers;
  }

  private String getMismatchingIdTxErrorMessage(Collection<ProposalResponse> responses) {
    return "TxIds mismatch. Ids=" + responses.stream().map(ProposalResponse::getTransactionID).collect(Collectors.joining(", "));
  }
}
