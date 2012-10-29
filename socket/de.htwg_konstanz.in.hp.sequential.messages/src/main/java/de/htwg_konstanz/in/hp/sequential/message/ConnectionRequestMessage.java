package de.htwg_konstanz.in.hp.sequential.message;

import java.util.UUID;

/**
 * Connection request message. It is sent from the mediator to the target when 
 * a source wants to establish a connection to it. To match the answer to this request
 * it contains a correlator.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0           4  5     7  
 * +--+--+--+--+--+--+--+--+
 * |    MAGIC     |  Type  |       
 * +--+--+--+--+--+--+--+--+
 * | Correlator (16 Bytes) |
 * |                       |
 * .                       .
 * .                       .
 * .                       .
 * +--+--+--+--+--+--+--+--+
 * </pre>
 * @author Daniel Maier
 *
 */
public final class ConnectionRequestMessage implements Message {
    private final UUID correlator;
    
    /**
     * Creates a new ConnectionRequestMessage with the given correlator.
     * @param correlator correlator of the message.
     */
    public ConnectionRequestMessage(UUID correlator) {
        this.correlator = correlator;
    }

    /**
     * Returns the correlator of the message.
     * @return the correlator of the message.
     */
    public UUID getCorrelator() {
        return correlator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((correlator == null) ? 0 : correlator.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ConnectionRequestMessage)) {
            return false;
        }
        ConnectionRequestMessage other = (ConnectionRequestMessage) obj;
        if (correlator == null) {
            if (other.correlator != null) {
                return false;
            }
        } else if (!correlator.equals(other.correlator)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConnectionRequestMessage [correlator=" + correlator + "]";
    }
}
