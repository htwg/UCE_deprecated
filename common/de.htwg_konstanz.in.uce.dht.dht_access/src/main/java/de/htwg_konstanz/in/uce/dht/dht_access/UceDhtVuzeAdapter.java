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

package de.htwg_konstanz.in.uce.dht.dht_access;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.SHA1Simple;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.logging.LoggerChannelListener;

import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.dht.DHT;
import com.aelitis.azureus.core.dht.DHTFactory;
import com.aelitis.azureus.core.dht.DHTLogger;
import com.aelitis.azureus.core.dht.DHTOperationListener;
import com.aelitis.azureus.core.dht.DHTStorageAdapter;
import com.aelitis.azureus.core.dht.nat.DHTNATPuncherAdapter;
import com.aelitis.azureus.core.dht.transport.DHTTransport;
import com.aelitis.azureus.core.dht.transport.DHTTransportContact;
import com.aelitis.azureus.core.dht.transport.DHTTransportException;
import com.aelitis.azureus.core.dht.transport.DHTTransportFullStats;
import com.aelitis.azureus.core.dht.transport.DHTTransportReplyHandler;
import com.aelitis.azureus.core.dht.transport.DHTTransportValue;
import com.aelitis.azureus.core.dht.transport.udp.DHTTransportUDP;
import com.aelitis.azureus.plugins.dht.impl.DHTPluginStorageManager;
import com.google.common.io.Files;

public class UceDhtVuzeAdapter implements UceDht {

    private static final InetSocketAddress BOOTSTRAP_ADDRESS = new InetSocketAddress(
            "174.129.43.152", 6881);
    private static final int K = 20;
    private static final int B = 5;
    private static final int UDP_TIMEOUT = 10000;
    private static final int MAX_PACKET_SIZE = 512;
    private static final int NUMBER_OF_SPLIT_BYTES = 1;
    private final DHT dht;

    // XXX remove
    public UceDhtVuzeAdapter(int port, DHTNATPuncherAdapter natAdapter)
            throws DHTTransportException {
        // init logger
        final LoggerChannel c_logger = AzureusCoreFactory.create().getPluginManager()
                .getDefaultPluginInterface().getLogger().getNullChannel("test");

        c_logger.addListener(new LoggerChannelListener() {
            public void messageLogged(int type, String content) {
                System.out.println(content);
            }

            public void messageLogged(String str, Throwable error) {
                System.out.println(str);

                error.printStackTrace();
            }
        });
        DHTLogger logger = new DHTLogger() {
            public void log(String str) {
                c_logger.log(str);
            }

            public void log(Throwable e) {
                c_logger.log(e);
            }

            public void log(int log_type, String str) {
                if (isEnabled(log_type)) {
                    c_logger.log(str);
                }
            }

            public boolean isEnabled(int log_type) {
                return (true);
            }

            public PluginInterface getPluginInterface() {
                return (c_logger.getLogger().getPluginInterface());
            }
        };

        // init vuze dht
        Properties dht_props = new Properties();

        dht_props.put(DHT.PR_CONTACTS_PER_NODE, new Integer(K));
        dht_props.put(DHT.PR_NODE_SPLIT_FACTOR, new Integer(B));
        dht_props.put(DHT.PR_CACHE_REPUBLISH_INTERVAL, new Integer(30000));
        dht_props.put(DHT.PR_ORIGINAL_REPUBLISH_INTERVAL, new Integer(60000));

        File tmp = Files.createTempDir();
        System.out.println(tmp);
        DHTStorageAdapter storage_adapter = new DHTPluginStorageManager(DHT.NW_MAIN, logger, tmp);

        // DHTTransport transport = DHTTransportFactory.createUDP(
        // DHTTransportUDP.PROTOCOL_VERSION_MAIN, DHT.NW_MAIN, false, null,
        // null, port, 3, 13,
        // UDP_TIMEOUT, 50, 25, false, false, logger);
        DHTTransport transport = new UceDhtTransportUdpImpl(DHTTransportUDP.PROTOCOL_VERSION_MAIN,
                DHT.NW_MAIN, false, null, null, port, 3, 13, UDP_TIMEOUT, 50, 25, false, false,
                logger);

        dht = DHTFactory.create(transport, dht_props, storage_adapter, natAdapter, logger);

        ((DHTTransportUDP) transport).importContact(BOOTSTRAP_ADDRESS,
                DHTTransportUDP.PROTOCOL_VERSION_MAIN);
    }

    public UceDhtVuzeAdapter(int port) throws DHTTransportException {
        this(port, new DHTNATPuncherAdapter() {

            public Map<String, Long> getClientData(InetSocketAddress originator,
                    @SuppressWarnings("rawtypes") Map originator_client_data) { // <- vuze interface
                System.out.println("getClientData - " + originator_client_data + "/" + originator);

                Map<String, Long> res = new HashMap<String, Long>();

                res.put("udp_data_port", new Long(1234));
                res.put("tcp_data_port", new Long(5678));

                return (res);
            }
        });
    }

    public void bootstrap() {
        dht.integrate(true);
    }

    // TODO unit test an grenzen usw.
    public boolean put(String key, byte[] value) throws InterruptedException {
        byte[] keyBytes = key.getBytes();
        final int numberOfPossibleSplits = (int) Math.pow(2, NUMBER_OF_SPLIT_BYTES * 8) - 1;
        if (value.length < MAX_PACKET_SIZE) {
            // one put is enough
            BlockingDHTPutListener listener = new BlockingDHTPutListener(new CountDownLatch(1));
            dht.put(keyBytes, "", value, DHT.FLAG_SINGLE_VALUE, listener);
            listener.latch.await();
            return listener.success();
        } else if (value.length < MAX_PACKET_SIZE + numberOfPossibleSplits * MAX_PACKET_SIZE) {
            // split value
            byte[][] splittedValues = splitValue(value);
            byte[][] splitKeys = getSplitKeys(keyBytes, splittedValues.length);
            // System.out.println(Arrays.deepToString(splittedValues));
            // System.out.println(Arrays.deepToString(splitKeys));
            byte[] firstValuePacket = new byte[MAX_PACKET_SIZE];
            System.arraycopy(value, 0, firstValuePacket, 0, MAX_PACKET_SIZE - NUMBER_OF_SPLIT_BYTES);
            firstValuePacket[MAX_PACKET_SIZE - 1] = (byte) splittedValues.length;
            // System.out.println(Arrays.toString(firstValuePacket));
            BlockingDHTPutMultipleListener listener = new BlockingDHTPutMultipleListener(
            		new CountDownLatch(1),
                    splitKeys, splittedValues);
            dht.put(keyBytes, "", firstValuePacket, DHT.FLAG_SINGLE_VALUE, listener);
            listener.latch.await();
            // TODO wie erfolg ŸberprŸfen??
            return true;
        } else {
            throw new IllegalArgumentException("Value size needs to be less than "
                    + (MAX_PACKET_SIZE + numberOfPossibleSplits * MAX_PACKET_SIZE) + ", but was "
                    + value.length);
        }
    }

    private byte[][] splitValue(byte[] value) {
        int remainingValueLength = value.length - MAX_PACKET_SIZE + NUMBER_OF_SPLIT_BYTES;
        int numberOfRemainingFullValuePackets = remainingValueLength / MAX_PACKET_SIZE;
        int sizeOfLastRemainingValuePacket = remainingValueLength % MAX_PACKET_SIZE;
        byte[][] splittedValues = new byte[numberOfRemainingFullValuePackets
                + ((sizeOfLastRemainingValuePacket == 0) ? 0 : 1)][];
        for (int i = remainingValueLength, pos = 1; i > 0; i -= MAX_PACKET_SIZE, pos++) {
            int splitSize = (i > MAX_PACKET_SIZE) ? MAX_PACKET_SIZE : i;
            byte[] splitValue = new byte[splitSize];
            System.arraycopy(value, (pos * MAX_PACKET_SIZE) - 1, splitValue, 0, splitValue.length);
            splittedValues[pos - 1] = splitValue;
        }
        return splittedValues;
    }

    // TODO unit test, vor allem negative byte werte
    private byte[][] getSplitKeys(byte[] unencodedKey, int numberOfSplits) {
        byte[] encodedMainKey = encodeKey(unencodedKey);
        int lastMainKeyValue = encodedMainKey[encodedMainKey.length - 1] & 0xFF;
        byte[][] keys = new byte[numberOfSplits][];
        int offset = 0;
        for (int i = 0; i < numberOfSplits; i++) {
            if (i == lastMainKeyValue) {
                offset = 1;
            }
            byte[] key = new byte[20];
            System.arraycopy(encodedMainKey, 0, key, 0, key.length);
            key[key.length - 1] = (byte) (i + offset);
            keys[i] = key;
        }
        return keys;
    }

    public byte[] get(String key) throws InterruptedException {
        byte[] keyBytes = key.getBytes();
        CountDownLatch latch = new CountDownLatch(1);
        BlockingDHTGetListener listener = new BlockingDHTGetListener(latch);
        dht.get(keyBytes, "", DHT.FLAG_SINGLE_VALUE, 100, 0, false, true, listener);
        // TODO was tun wenn mehrere Werte zurŸckkommen?
        latch.await();
        if (listener.getValues().isEmpty()) {
            return null;
        }
        byte[] value = null;
        for (DHTTransportValue v : listener.getValues()) {
            value = v.getValue();
        }
        System.out.println(value.length);
        System.out.println(Arrays.toString(value));
        if (value.length < MAX_PACKET_SIZE) {
            // non splitted value
            return value;
        } else {
            // search other parts
            int numberOfSplits = value[MAX_PACKET_SIZE - 1] & 0xFF;
            byte[][] splitKeys = getSplitKeys(keyBytes, numberOfSplits);
            List<byte[]> values = getSplitValues(listener.getStoringContact(), splitKeys);
            int sizeOfLastSplit = values.get(values.size() - 1).length;
            int totalValueSize = (MAX_PACKET_SIZE - NUMBER_OF_SPLIT_BYTES) + MAX_PACKET_SIZE
                    * (values.size() - 1) + sizeOfLastSplit;
            byte[] valueBytes = new byte[totalValueSize];
            System.arraycopy(value, 0, valueBytes, 0, MAX_PACKET_SIZE - NUMBER_OF_SPLIT_BYTES);
            int pos = MAX_PACKET_SIZE - NUMBER_OF_SPLIT_BYTES;
            for (byte[] split : values) {
                System.arraycopy(split, 0, valueBytes, pos, split.length);
                pos += split.length;
            }
            return valueBytes;
        }
    }

    private List<byte[]> getSplitValues(DHTTransportContact storingContact, byte[][] splitKeys)
            throws InterruptedException {
        final List<byte[]> splitValues = new Vector<byte[]>(splitKeys.length);
        final CountDownLatch splitLatch = new CountDownLatch(splitKeys.length);

        final class FindSplitValueReplyHandler implements DHTTransportReplyHandler {
            private final int index;

            private FindSplitValueReplyHandler(int index) {
                this.index = index;
            }

            public void storeReply(DHTTransportContact contact, byte[] diversifications) {
            }

            public void statsReply(DHTTransportContact contact, DHTTransportFullStats stats) {
            }

            public void queryStoreReply(DHTTransportContact contact, List<byte[]> response) {
            }

            public void pingReply(DHTTransportContact contact, int elapsed_time) {
            }

            public void keyBlockRequest(DHTTransportContact contact, byte[] key,
                    byte[] key_signature) {
            }

            public void keyBlockReply(DHTTransportContact contact) {
            }

            public void findValueReply(DHTTransportContact contact, DHTTransportContact[] contacts) {
                System.out.println("ZONK: findValueReply: " + contact.toString());
            }

            public void findValueReply(DHTTransportContact contact, DHTTransportValue[] values,
                    byte diversification_type, boolean more_to_come) {
                // TODO was bei mehreren werten??
                for (DHTTransportValue v : values) {
                    splitValues.add(index, v.getValue());
                }
                splitLatch.countDown();
            }

            public void findNodeReply(DHTTransportContact contact, DHTTransportContact[] contacts) {
            }

            public void failed(DHTTransportContact contact, Throwable error) {
            }
        }
        for (int i = 0; i < splitKeys.length; i++) {
            // TODO was machen wenn mehrere werte da oder wert auf diesem
            // knoten nicht gefunden wird?
            storingContact.sendFindValue(new FindSplitValueReplyHandler(i), splitKeys[i], 100,
                    (byte) 0);
        }
        splitLatch.await();
        return splitValues;
    }

    private static class BlockingDHTPutListener implements DHTOperationListener {

        private volatile boolean success = false;
        private final CountDownLatch latch;

        private BlockingDHTPutListener(CountDownLatch latch) {
            this.latch = latch;
        }

        public void searching(DHTTransportContact contact, int level, int active_searches) {
        }

        public void diversified(String desc) {
        }

        public void found(DHTTransportContact contact, boolean is_closest) {
        }

        public void read(DHTTransportContact contact, DHTTransportValue value) {
        }

        public void wrote(DHTTransportContact contact, DHTTransportValue value) {
            success = true;
        }

        public void complete(boolean timeout) {
            latch.countDown();
        }

        private boolean success() {
            return success;
        }

    }

    private class BlockingDHTPutMultipleListener implements DHTOperationListener {

        private final byte[][] splitKeys;
        private final byte[][] splitValues;
        private final CountDownLatch latch;
        private final CountDownLatch splitLatch;

        private BlockingDHTPutMultipleListener(CountDownLatch latch, byte[][] splitKeys,
                byte[][] splitValues) {
            this.latch = latch;
            this.splitKeys = splitKeys;
            this.splitValues = splitValues;
            this.splitLatch = new CountDownLatch(splitKeys.length
                    * dht.getIntProperty(DHT.PR_CONTACTS_PER_NODE));
        }

        public void searching(DHTTransportContact contact, int level, int active_searches) {
        }

        public void diversified(String desc) {
        }

        public void found(DHTTransportContact contact, boolean is_closest) {
        }

        public void read(DHTTransportContact contact, DHTTransportValue value) {
        }

        public void wrote(DHTTransportContact contact, DHTTransportValue value) {
            for (int i = 0; i < splitKeys.length; i++) {
                DHTTransportValue splitValue = dht.getDataBase()
                        .store(new HashWrapper(splitKeys[i]), splitValues[i], (byte) 0, (byte) 0,
                                (byte) 0);
                dht.getControl().putDirectEncodedKeys(new byte[][] { splitKeys[i] }, "",
                        new DHTTransportValue[][] { new DHTTransportValue[] { splitValue } },
                        contact, new DHTOperationListener() {

                            public void wrote(DHTTransportContact contact, DHTTransportValue value) {
                            }

                            public void searching(DHTTransportContact contact, int level,
                                    int active_searches) {
                            }

                            public void read(DHTTransportContact contact, DHTTransportValue value) {
                            }

                            public void found(DHTTransportContact contact, boolean is_closest) {
                            }

                            public void diversified(String desc) {
                            }

                            public void complete(boolean timeout) {
                                splitLatch.countDown();
                            }
                        });
            }
        }

        public void complete(boolean timeout) {
            if (!timeout) {
                try {
                    splitLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            latch.countDown();
        }
    }

    private static class BlockingDHTGetListener implements DHTOperationListener {

        private final CountDownLatch latch;
        private final Set<DHTTransportValue> values;
        private volatile DHTTransportContact storingContact;

        private BlockingDHTGetListener(CountDownLatch latch) {
            this.latch = latch;
            this.values = Collections.synchronizedSet(new HashSet<DHTTransportValue>());
        }

        public void searching(DHTTransportContact contact, int level, int active_searches) {
        }

        public void diversified(String desc) {
        }

        public void found(DHTTransportContact contact, boolean is_closest) {
        }

        public void read(DHTTransportContact contact, DHTTransportValue value) {
            storingContact = contact;
            values.add(value);
        }

        public void wrote(DHTTransportContact contact, DHTTransportValue value) {
        }

        public void complete(boolean timeout) {
            latch.countDown();
        }

        private Set<DHTTransportValue> getValues() {
            return values;
        }

        private DHTTransportContact getStoringContact() {
            return storingContact;
        }
    }
    

    private byte[] encodeKey(byte[] key) {
        byte[] temp = new SHA1Simple().calculateHash(key);

        byte[] result = new byte[20];

        System.arraycopy(temp, 0, result, 0, 20);

        return (result);
    }

    // XXX remove
    public DHT getDHT() {
        return dht;
    }
    
    // XXX remove
    public InetSocketAddress determineExternalVuzeDhtEndpoint() {
        return ((UceDhtTransportUdpImpl) dht.getTransport()).determineExternalVuzeDhtEndpoint();
    }

	/*
	 * remove only removes the local mapping of the value that is published
	 * once every other minute. No guarantee that the value disappears 
	 * in a short time.
	 * (non-Javadoc)
	 * @see de.htwg_konstanz.in.uce.dht.dht_access.UceDht#remove(java.lang.String)
	 */
    public byte[] remove(String key) throws InterruptedException {
		byte[] keyBytes = get(key);
        BlockingDHTPutListener listener = new BlockingDHTPutListener(new CountDownLatch(1));
        dht.remove(keyBytes, "", listener); // doesn't seem to work, why?
        listener.latch.await();
        //dht.remove(keyBytes, "", null); // returns immediately
		return keyBytes;
	}
	
	public void destroy() {
		dht.destroy();
	}

}
