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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Test;

public class StringListTest {

    @Test
    public void testEncodeDecode() throws IOException {
        // prepare & expected
        String testString1 = "test1";
        String testString2 = "test2";
        String testString3 = "test3";
        List<String> strings = new Vector<String>();
        strings.add(testString1);
        strings.add(testString2);
        strings.add(testString3);

        // execute
        StringList stringList = new StringList(strings);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        stringList.writeTo(bout);
        byte[] stringBytes = bout.toByteArray();
        List<String> resultStrings = ((StringList) StringList.fromBytes(stringBytes, null))
                .getStrings();

        // verify
        Assert.assertEquals(strings, resultStrings);
    }
    
    @Test
    public void testEncodeDecodeSingleString() throws IOException {
        // prepare & expected
        String testString1 = "test1";
        List<String> strings = new Vector<String>();
        strings.add(testString1);

        // execute
        StringList stringList = new StringList(strings);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        stringList.writeTo(bout);
        byte[] stringBytes = bout.toByteArray();
        List<String> resultStrings = ((StringList) StringList.fromBytes(stringBytes, null))
                .getStrings();

        // verify
        Assert.assertEquals(strings, resultStrings);
    }
    
    @Test
    public void testEncodeDecodeNoString() throws IOException {
        // prepare & expected
        List<String> strings = new Vector<String>();

        // execute
        StringList stringList = new StringList(strings);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        stringList.writeTo(bout);
        byte[] stringBytes = bout.toByteArray();
        List<String> resultStrings = ((StringList) StringList.fromBytes(stringBytes, null))
                .getStrings();

        // verify
        Assert.assertEquals(strings, resultStrings);
    }
    
    @Test
    public void testEncodeDecodeTooLongString() throws IOException {
        // prepare & expected
        String testString1 = "test1";
        String testString2 = "xDVz9zbAos{G&A5ZJSulR5ryjQEmcMdBX>T!Ozxcvkvi~>8qiybDgiyLdxgz:bgslaan" +
        		"6Kt6azvuLkM2n:bzkpXzpRfkmeXsxp`4vgldz)dD>warTdzNsyDsTWx2RbRgKlbj1toLrUxw+dl#B#Lj^" +
        		"pwGB$drzflkQogQtxr0oKY<tj+gz5xYwwTJg%vzn>vzBxjva1G8d|MnuPa.oDcWElyHg|YIz}j7tonKKx" +
        		"WuisvFhQRevRFkkubexyZ!KD'V";
        String testString3 = "test3";
        List<String> strings = new Vector<String>();
        strings.add(testString1);
        strings.add(testString2);
        strings.add(testString3);

        // execute
        StringList stringList = new StringList(strings);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        stringList.writeTo(bout);
        byte[] stringBytes = bout.toByteArray();
        List<String> resultStrings = ((StringList) StringList.fromBytes(stringBytes, null))
                .getStrings();

        // verify
        Assert.assertFalse(strings.equals(resultStrings));
        Assert.assertFalse(testString2.equals(resultStrings.get(1)));
        Assert.assertEquals(testString1, resultStrings.get(0));
        Assert.assertEquals(testString3, resultStrings.get(2));
    }
}
