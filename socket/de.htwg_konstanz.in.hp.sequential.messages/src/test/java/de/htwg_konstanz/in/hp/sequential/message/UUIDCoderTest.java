package de.htwg_konstanz.in.hp.sequential.message;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import de.htwg_konstanz.in.hp.sequential.message.coder.UUIDCoder;

public class UUIDCoderTest {
    @Test
    public void testUUIDCoder() {
        UUID uuid = UUID.randomUUID();
        UUIDCoder c = new UUIDCoder();
        byte[] uuidAsByteArray = c.asByteArray(uuid);
        UUID decodedUUID = c.toUUID(uuidAsByteArray);
        Assert.assertEquals(uuid, decodedUUID);
    }
}
