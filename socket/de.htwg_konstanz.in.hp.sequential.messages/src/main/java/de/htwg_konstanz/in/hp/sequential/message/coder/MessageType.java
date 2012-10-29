package de.htwg_konstanz.in.hp.sequential.message.coder;

/**
 * Enumeration for all message types that are needed for the sequential hole punching 
 * process.
 * @author Daniel Maier
 *
 */
public enum MessageType {
    Register, RegisterResponse, LookupRequest, ConnectionRequest, ConnectionRequestAck, 
    ConnectionRequestDetails, ConnectionResponse, LookupResponse;
}
