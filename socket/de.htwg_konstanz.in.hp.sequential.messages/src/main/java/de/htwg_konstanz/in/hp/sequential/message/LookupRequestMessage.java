package de.htwg_konstanz.in.hp.sequential.message;

/**
 * Lookup request message. It is sent form the source to start the 
 * hole punching process with the desired target. To identify the target
 * it contains the ID of it.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 *  0           4  5     7  8                    15
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |    MAGIC     |  Type  |        Length         |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |             ID (Maximum 255 Bytes)            |
 * |                                               |
 * .                                               .
 * .                                               .
 * .                                               .
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 *</pre>
 * @author Daniel Maier
 */
public final class LookupRequestMessage implements Message {
    private final String id;
    
    /**
     * Creates a new LookupRequestMessage with the given ID. 
     * @param id the ID of the target.
     */
    public LookupRequestMessage(String id) {
        this.id = id;
    }
    
    /**
     * Returns the ID of the target.
     * @return the ID of the target.
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (!(obj instanceof LookupRequestMessage)) {
            return false;
        }
        LookupRequestMessage other = (LookupRequestMessage) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LookupRequestMessage [id=" + id + "]";
    }
}
