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
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

public class CustomAttributeTest {

    private static final class CustomAttribute implements UceAttribute {

        private final String value;

        private CustomAttribute(String value) {
            this.value = value;
        }

        public UceAttributeType getType() {
            return new CustomAttributeType();
        }

        public int getLength() {
            return value.getBytes().length;
        }

        public void writeTo(OutputStream out) throws IOException {
            out.write(value.getBytes());
        }

        public String getValue() {
            return value;
        }
    }

    private static final class CustomAttributeType implements UceAttributeType {

        public int encode() {
            return 23;
        }

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
                throws MessageFormatException, IOException {
            String value = new String(encoded);
            return new CustomAttribute(value);
        }

    }

    private final class CustomAttributeDecoder implements UceAttributeTypeDecoder {

        public UceAttributeType decode(int encoded) {
            if (encoded == 23) {
                return new CustomAttributeType();
            }
            return null;
        }
    }

    @Test
    public void testCustomAttribute() throws IOException {
        // prepare
        UceMethod method = CommonUceMethod.AUTHENTICATE;
        SemanticLevel semanticLevel = SemanticLevel.INDICATION;
        UUID transactionId = UUID.randomUUID();
        String attributeValue = "testStringValue";
        UceMessage message = UceMessageStaticFactory.newUceMessageInstance(method, semanticLevel,
                transactionId);
        message.addAttribute(new CustomAttribute(attributeValue));

        // execute
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        message.writeTo(bout);
        UceMessageReader reader = new UceMessageReader(new CustomAttributeDecoder());
        UceMessage resultMessage = reader.readUceMessage(new ByteArrayInputStream(bout
                .toByteArray()));

        // verify
        List<CustomAttribute> attributes = resultMessage.getAttributes(CustomAttribute.class);
        Assert.assertTrue(attributes.size() == 1);
        CustomAttribute customAttribute = attributes.get(0);
        Assert.assertEquals(attributeValue, customAttribute.getValue());
    }
}
