package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.ProposalResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class ValidatorInfoBuilder {

  private final Collection<ProposalResponse> successful;
  private final Collection<ProposalResponse> failed;

  private String transactionId;

  public ValidatorInfoBuilder() {
    successful = new ArrayList<>();
    failed = new ArrayList<>();
  }

  public ValidatorInfoBuilder addValidEndorsement(ProposalResponse proposalResponse) {
    Objects.requireNonNull(proposalResponse);
    successful.add(proposalResponse);
    return this;
  }

  public ValidatorInfoBuilder addInvalidResponse(ProposalResponse proposalResponse) {
    Objects.requireNonNull(proposalResponse);
    failed.add(proposalResponse);
    return this;
  }

  public ValidatorInfoBuilder setTransactionId(String transactionId) {
    this.transactionId = Objects.requireNonNull(transactionId);
    return this;
  }

  public SimulationInfo build() {
    boolean isSuccessful = !successful.isEmpty();

    String message = failed.stream().map(ProposalResponse::getMessage).collect(Collectors.joining(" | "));
    return new SimulationInfo(successful, failed, isSuccessful, message, transactionId);
  }
}
