/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htwg_konstanz.in.uce.hp.parallel.messages.coder;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.UUIDCoder;

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
