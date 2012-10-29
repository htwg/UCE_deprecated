package de.htwg_konstanz.in.hp.sequential.message;

/**
 * Register response message. It is sent from the mediator to the target after the mediator
 * received a register message from that target. It contains the information whether the
 * registration process was successful or not.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0           4  5     7  
 * +--+--+--+--+--+--+--+--+
 * |    MAGIC     |  Type  |       
 * +--+--+--+--+--+--+--+--+
 * |--------------------|SC|
 * +--+--+--+--+--+--+--+--+
 * </pre>
 * @author Daniel Maier
 *
 */
public final class RegisterResponseMessage implements Message {
    private final boolean success;
    
    /**
     * Creates a new RegisterResponseMessage.
     * @param success indicates whether the registration process was successful or not.
     */
    public RegisterResponseMessage(boolean success) {
        this.success = success;
    }

    /**
     * Returns the information whether the registration process was successful or not.
     * @return the information whether the registration process was successful or not.
     */
    public boolean isSuccess() {
        return success;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (success ? 1231 : 1237);
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
        if (!(obj instanceof RegisterResponseMessage)) {
            return false;
        }
        RegisterResponseMessage other = (RegisterResponseMessage) obj;
        if (success != other.success) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RegisterResponseMessage [success=" + success + "]";
    }
}
