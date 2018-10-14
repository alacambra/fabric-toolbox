package tech.lacambra.fabric.client.stateless;

import org.hyperledger.fabric.sdk.*;

import java.util.Collection;
import java.util.stream.Collectors;

public class Printer {

  public static final String DEFAULT_STRING = "--";

  private Printer() {
  }

  public static String toString(ChaincodeID chaincodeID) {

    if (chaincodeID == null) {
      return wrap(DEFAULT_STRING);
    }

    String pretty = wrap("name=" + chaincodeID.getName() +
        ", version=" + chaincodeID.getVersion() +
        ", path=" + chaincodeID.getPath());

    return wrap(pretty);
  }

  public static String toString(Channel channel) {
    if (channel == null) {
      return wrap(DEFAULT_STRING);
    }

    String pretty = wrap("name=" + channel.getName());
    return wrap(pretty);
  }

  public static String toString(BlockEvent.TransactionEvent transactionEvent) {

    if (transactionEvent == null) {
      return wrap(DEFAULT_STRING);
    }

    return wrap("TxId=" + transactionEvent.getTransactionID() +
        ", valid=" + transactionEvent.isValid());
  }

  public static String toString(SimulationInfo result) {

    if (result == null) {
      return wrap(DEFAULT_STRING);
    }

    return wrap("success=" + result.simulationsSucceed() +
        ", message=" + result.getMessage());
  }

  public static String toString(Peer peer) {

    if (peer == null) {
      return wrap(DEFAULT_STRING);
    }

    return wrap("peerName=" + peer.getName() +
        ", peerUrl=" + peer.getUrl());
  }

  public static String peersToString(Collection<Peer> peers) {
    if (peers == null) {
      return wrap("");
    }

    return wrap(peers.stream().map(Printer::toString).collect(Collectors.joining(" | ")));
  }

  public static String toString(EventHub eventHub) {

    if (eventHub == null) {
      return wrap(DEFAULT_STRING);
    }

    return wrap("EventHubName=" + eventHub.getName() +
        ", EventHubUrl=" + eventHub.getUrl());
  }

  public static String eventHubsToString(Collection<EventHub> eventHubs) {
    if (eventHubs == null) {
      return wrap("");
    }

    return wrap(eventHubs.stream().map(Printer::toString).collect(Collectors.joining(" | ")));
  }

  public static String toString(Orderer orderer) {

    if (orderer == null) {
      return wrap(DEFAULT_STRING);
    }

    return wrap("EventHubName=" + orderer.getName() +
        ", EventHubUrl=" + orderer.getUrl());
  }

  public static String orderersToString(Collection<Orderer> orderers) {
    if (orderers == null) {
      return wrap(DEFAULT_STRING);
    }

    return wrap(orderers.stream().map(Printer::toString).collect(Collectors.joining(" | ")));
  }

  private static String wrap(String str) {
    return "{ " + str + " }";
  }

}