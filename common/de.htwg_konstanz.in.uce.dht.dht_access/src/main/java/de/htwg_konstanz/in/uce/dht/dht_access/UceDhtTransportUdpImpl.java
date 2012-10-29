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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gudy.azureus2.core3.util.AESemaphore;

import com.aelitis.azureus.core.dht.DHTLogger;
import com.aelitis.azureus.core.dht.transport.DHTTransportContact;
import com.aelitis.azureus.core.dht.transport.DHTTransportException;
import com.aelitis.azureus.core.dht.transport.udp.impl.DHTTransportUDPContactImpl;
import com.aelitis.azureus.core.dht.transport.udp.impl.DHTTransportUDPImpl;
import com.aelitis.azureus.core.dht.transport.udp.impl.DHTTransportUDPStatsImpl;
import com.aelitis.azureus.core.dht.transport.udp.impl.DHTUDPPacketReply;
import com.aelitis.azureus.core.dht.transport.udp.impl.DHTUDPPacketReplyError;
import com.aelitis.azureus.core.dht.transport.udp.impl.DHTUDPPacketReplyPing;
import com.aelitis.azureus.core.dht.transport.udp.impl.DHTUDPPacketRequestPing;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketHandler;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketHandlerException;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketReceiver;
import com.aelitis.net.udp.uc.PRUDPPacketHandler;

public class UceDhtTransportUdpImpl extends DHTTransportUDPImpl {

    public UceDhtTransportUdpImpl(byte _protocol_version, int _network, boolean _v6, String _ip,
            String _default_ip, int _port, int _max_fails_for_live, int _max_fails_for_unknown,
            long _timeout, int _dht_send_delay, int _dht_receive_delay, boolean _bootstrap_node,
            boolean _initial_reachability, DHTLogger _logger) throws DHTTransportException {
        super(_protocol_version, _network, _v6, _ip, _default_ip, _port, _max_fails_for_live,
                _max_fails_for_unknown, _timeout, _dht_send_delay, _dht_receive_delay,
                _bootstrap_node, _initial_reachability, _logger);
    }

    public InetSocketAddress determineExternalVuzeDhtEndpoint() {
        List<DHTTransportContact> contacts = new ArrayList<DHTTransportContact>();
        Collections.addAll(contacts, getReachableContacts());

        // randomly select a number of entries to ping until we
        // get three replies

        InetSocketAddress returnedEndpoint = null;
        int returned_matches = 0;

        int search_lim = contacts.size();

        System.out.println("Contacts to search = " + search_lim);

        for (int i = 0; i < search_lim; i++) {

            DHTTransportUDPContactImpl contact = (DHTTransportUDPContactImpl) contacts
                    .remove((int) (contacts.size() * Math.random()));

            InetSocketAddress a = askContactForExternalEndpoint(contact);

            if (a != null && a.getAddress() != null) {


                if (returnedEndpoint == null) {

                    returnedEndpoint = a;

                    System.out.println("    : contact " + contact.getString()
                            + " reported external vuze dht endpoint as '" + a + "'");

                    returned_matches++;

                } else if (returnedEndpoint.equals(a)) {

                    returned_matches++;

                    System.out.println("    : contact " + contact.getString()
                            + " external vuze dht endpoint as '" + a + "'");

                    if (returned_matches == 3) {

                        System.out.println("    External vuze dht endpoint as from contacts: "
                                + returnedEndpoint);
                        return returnedEndpoint;
                    }
                } else {

                    System.out.println("    : contact " + contact.getString()
                            + " external vuze dht endpoint '" + a
                            + "', abandoning due to mismatch");

                    // mismatch - give up

                    return null;
                }
            } else {

                System.out.println("    : contact " + contact.getString() + " didn't reply");
            }
        }
        return null;
    }

    private InetSocketAddress askContactForExternalEndpoint(DHTTransportUDPContactImpl contact) {
        final DHTTransportUDPStatsImpl stats = (DHTTransportUDPStatsImpl) getStats();
        try {
            checkAddress(contact);

            final long connection_id = getConnectionID();

            final DHTUDPPacketRequestPing request = new DHTUDPPacketRequestPing(this,
                    connection_id, contact, contact);

            stats.pingSent(request);

            final AESemaphore sem = new AESemaphore("DHTTransUDP:extping");

            final InetSocketAddress[] result = new InetSocketAddress[1];

            DHTUDPPacketHandler packet_handler = (DHTUDPPacketHandler) getPacketHandlerByReflection(this);
            packet_handler.sendAndReceive(request, contact.getTransportAddress(),
                    new DHTUDPPacketReceiver() {
                        public void packetReceived(DHTUDPPacketReply _packet,
                                InetSocketAddress from_address, long elapsed_time) {
                            try {

                                if (_packet instanceof DHTUDPPacketReplyPing) {

                                    // ping was OK so current address is OK

                                    // XXX should never happen??

                                } else if (_packet instanceof DHTUDPPacketReplyError) {

                                    DHTUDPPacketReplyError packet = (DHTUDPPacketReplyError) _packet;

                                    int errorType = determineErrorTypeByReflection(packet);
                                    if (errorType == DHTUDPPacketReplyError.ET_ORIGINATOR_ADDRESS_WRONG) {

                                        result[0] = determineOriginatingAddressByReflection(_packet);
                                    }
                                }
                            } finally {

                                sem.release();
                            }
                        }

                        public void error(DHTUDPPacketHandlerException e) {
                            try {
                                stats.pingFailed();

                            } finally {

                                sem.release();
                            }
                        }
                    }, 5000, PRUDPPacketHandler.PRIORITY_HIGH);

            sem.reserve(5000);

            return (result[0]);

        } catch (Throwable e) {

            stats.pingFailed();

            return (null);
        }
    }

    private DHTUDPPacketHandler getPacketHandlerByReflection(
            UceDhtTransportUdpImpl uceDhtTransportUdpImpl) {
        Field packetHandlerField;
        try {
            packetHandlerField = DHTTransportUDPImpl.class.getDeclaredField("packet_handler");
            packetHandlerField.setAccessible(true);
            return (DHTUDPPacketHandler) packetHandlerField.get(uceDhtTransportUdpImpl);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new AssertionError(
                "Exception while reflection. Underlying vuze implementation changed?");
    }

    private int determineErrorTypeByReflection(DHTUDPPacketReplyError packet) {
        try {
            Method getErrorTypeMethod = DHTUDPPacketReplyError.class
                    .getDeclaredMethod("getErrorType");
            getErrorTypeMethod.setAccessible(true);
            Integer errorType = (Integer) getErrorTypeMethod.invoke(packet);
            return errorType;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new AssertionError(
                "Exception while reflection. Underlying vuze implementation changed?");
    }

    private InetSocketAddress determineOriginatingAddressByReflection(DHTUDPPacketReply _packet) {
        try {
            Method getOriginatingAddressMethod = DHTUDPPacketReplyError.class
                    .getDeclaredMethod("getOriginatingAddress");
            getOriginatingAddressMethod.setAccessible(true);
            InetSocketAddress originatingAddress = (InetSocketAddress) getOriginatingAddressMethod
                    .invoke(_packet);
            return originatingAddress;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new AssertionError(
                "Exception while reflection. Underlying vuze implementation changed?");
    }

}
