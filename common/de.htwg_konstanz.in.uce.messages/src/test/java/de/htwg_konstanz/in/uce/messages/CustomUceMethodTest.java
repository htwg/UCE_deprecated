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

package de.htwg_konstanz.in.uce.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class CustomUceMethodTest {

    private static final class CustomUceMethod implements UceMethod {
        private final int encoded = 23;

        public int encode() {
            return encoded;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + encoded;
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
            if (!(obj instanceof CustomUceMethod)) {
                return false;
            }
            CustomUceMethod other = (CustomUceMethod) obj;
            if (encoded != other.encoded) {
                return false;
            }
            return true;
        }
    }

    private static final class CustomUceMethodDecoder implements UceMethodDecoder {
        public UceMethod decode(int encoded) {
            if (encoded == 23) {
                return new CustomUceMethod();
            }
            return null;
        }
    }

    @Test
    public void testCustomUceMethod() throws IOException {
        // prepare
        UceMethod method = new CustomUceMethod();
        SemanticLevel semanticLevel = SemanticLevel.INDICATION;
        UUID transactionId = UUID.randomUUID();
        UceMessage expectedMessage = UceMessageStaticFactory.newUceMessageInstance(method,
                semanticLevel, transactionId);

        // execute
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        expectedMessage.writeTo(bout);

        UceMessageReader reader = new UceMessageReader(new CustomUceMethodDecoder());
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        UceMessage resultMessage = reader.readUceMessage(bin);

        // verify
        Assert.assertEquals(expectedMessage, resultMessage);
        ByteArrayOutputStream resultBout = new ByteArrayOutputStream();
        resultMessage.writeTo(resultBout);
        Assert.assertArrayEquals(bout.toByteArray(), resultBout.toByteArray());
    }
}
