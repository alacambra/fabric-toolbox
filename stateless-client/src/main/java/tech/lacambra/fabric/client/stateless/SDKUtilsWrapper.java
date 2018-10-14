package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class SDKUtilsWrapper {

  public byte[] calculateBlockHash(HFClient client, long blockNumber, byte[] previousHash, byte[] dataHash) {
    try {
      return SDKUtils.calculateBlockHash(client, blockNumber, previousHash, dataHash);
    } catch (IOException | InvalidArgumentException e) {
      throw new StatelessClientException(e);
    }
  }

  public Collection<Set<ProposalResponse>> getProposalConsistencySets(Collection<ProposalResponse> proposalResponses) {
    try {
      return SDKUtils.getProposalConsistencySets(proposalResponses);
    } catch (InvalidArgumentException e) {
      throw new StatelessClientException(e);
    }
  }

  public static Collection<Set<ProposalResponse>> getProposalConsistencySets(Collection<ProposalResponse> proposalResponses, Set<ProposalResponse> invalid) {
    try {
      return SDKUtils.getProposalConsistencySets(proposalResponses, invalid);
    } catch (InvalidArgumentException e) {
      throw new StatelessClientException(e);
    }
  }
}
