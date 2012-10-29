package de.htwg_konstanz.in.hp.sequential.message.coder;

import java.util.UUID;

/**
 * Helper class to encode a UUID as byte array and decode a byte array
 * as a UUID.
 * @author Daniel Maier
 *
 */
public class UUIDCoder {

    /**
     * Converts a UUID to a byte array.
     * @param uuid the UUID to be converted.
     * @return the UUID as byte array.
     */
	public byte[] asByteArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }

        return buffer;

    }

    /**
     * Converts a byte array to a UUID.
     * @param byteArray the byte array to be converted.
     * @return the UUID.
     */
	public UUID toUUID(byte[] byteArray) {
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (byteArray[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (byteArray[i] & 0xff);
        UUID result = new UUID(msb, lsb);

        return result;
    }
}
