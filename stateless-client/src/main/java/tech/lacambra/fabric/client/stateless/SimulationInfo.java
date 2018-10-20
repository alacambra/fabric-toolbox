package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.ProposalResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

public class SimulationInfo {

  private static final Logger LOGGER = Logger.getLogger(PeerTransactionValidator.class.getName());
  private final Collection<ProposalResponse> successfulProposals;
  private final Collection<ProposalResponse> failedProposals;
  private boolean simulationSucceed;
  private String message = "no message given";
  private String transactionId;

  SimulationInfo(Collection<ProposalResponse> successfulProposals, Collection<ProposalResponse> failedProposals, boolean simulationSucceed, String message, String transactionId) {
    this.successfulProposals = new ArrayList<>(successfulProposals);
    this.failedProposals = new ArrayList<>(failedProposals);
    this.simulationSucceed = simulationSucceed;
    this.message = message;
    this.transactionId = transactionId;
  }

  private SimulationInfo(boolean simulationSucceed, String reason, Collection<ProposalResponse> failedProposals) {
    successfulProposals = Collections.emptyList();
    this.failedProposals = failedProposals;
    this.simulationSucceed = simulationSucceed;
    this.message = reason;

    if (!simulationSucceed) {
      transactionId = failedProposals.stream().findAny().map(ProposalResponse::getTransactionID).orElse("");
    }
  }

  public Collection<ProposalResponse> getSuccessfulProposals() {
    return new ArrayList<>(successfulProposals);
  }

  public Collection<ProposalResponse> getFailedProposals() {
    return new ArrayList<>(failedProposals);
  }

  /**
   * at least one proposal is valid and can be sent to orderer.
   *
   * @return
   */
  public boolean canBeSendToOrderer() {
    return simulationsSucceed() && !successfulProposals.isEmpty();
  }

  public boolean simulationsSucceed() {
    return simulationSucceed;
  }

  public String getMessage() {
    return message;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public static SimulationInfo createInvalidPayloadsResult(String errorMessage, Collection<ProposalResponse> responses) {
    return createInvalidResultWithResponses(errorMessage, responses);
  }

  public static SimulationInfo createInvalidTransactionIdsResult(String errorMessage, Collection<ProposalResponse> responses) {
    return createInvalidResultWithResponses(errorMessage, responses);
  }

  public static SimulationInfo createInvalidConsistencySetsResult(String errorMessage, Collection<ProposalResponse> responses) {
    return createInvalidResultWithResponses(errorMessage, responses);
  }

  public static SimulationInfo createValid(String reason) {
    Objects.requireNonNull(reason);
    return new SimulationInfo(true, reason, Collections.emptyList());
  }

  public static SimulationInfo createInvalid(String reason) {
    Objects.requireNonNull(reason);
    return new SimulationInfo(false, reason, Collections.emptyList());
  }

  private static SimulationInfo createInvalidResultWithResponses(String errorMessage, Collection<ProposalResponse> responses) {
    Objects.requireNonNull(errorMessage);
    Objects.requireNonNull(responses);
    return new SimulationInfo(false, errorMessage, responses);
  }

  @Override
  public String toString() {
    return "ValidationInfo{" +
        "simulationSucceed=" + simulationSucceed +
        ", message='" + message + '\'' +
        ", transactionId='" + transactionId + '\'' +
        '}';
  }
}