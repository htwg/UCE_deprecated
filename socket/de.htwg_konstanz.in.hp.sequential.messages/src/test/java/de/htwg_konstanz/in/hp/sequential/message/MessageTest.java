package de.htwg_konstanz.in.hp.sequential.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import junit.framework.Assert;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class MessageTest {

	@Test
	public void testRegisterMessage() {
		String id = "sampleID";
		RegisterMessage m = new RegisterMessage(id);
		Assert.assertEquals(id, m.getId());
	}

	@Test
	public void testRegisterMessageEquals() {
		EqualsVerifier.forClass(RegisterMessage.class).verify();
	}

	@Test
	public void testRegisterResponseMessage() {
		boolean success = true;
		RegisterResponseMessage m = new RegisterResponseMessage(success);
		Assert.assertTrue(m.isSuccess());
		success = false;
		m = new RegisterResponseMessage(success);
		Assert.assertFalse(m.isSuccess());
	}

	@Test
	public void testRegisterResponseMessageEquals() {
		EqualsVerifier.forClass(RegisterResponseMessage.class).verify();
	}

	@Test
	public void testLookupRequestMessage() {
		String id = "sampleID";
		LookupRequestMessage m = new LookupRequestMessage(id);
		Assert.assertEquals(id, m.getId());
	}

	@Test
	public void testLookupRequestMessageEquals() {
		EqualsVerifier.forClass(LookupRequestMessage.class).verify();
	}

	@Test
	public void testLookupResponseMessage() throws UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		int port = 1234;
		LookupResponseMessage m = new LookupResponseMessage(ip, port);
		Assert.assertEquals(ip, m.getIP());
		Assert.assertEquals(port, m.getPort());
	}

	@Test
	public void testLookupResponseMessageEquals() throws UnknownHostException {
		EqualsVerifier.forClass(LookupResponseMessage.class).withPrefabValues(
				InetAddress.class, InetAddress.getByName("192.168.7.6"),
				InetAddress.getByName("192.168.7.7")).verify();
	}

	@Test
	public void testConnectionRequestMessage() {
		UUID correlator = UUID.randomUUID();
		ConnectionRequestMessage m = new ConnectionRequestMessage(correlator);
		Assert.assertEquals(correlator, m.getCorrelator());
	}

	@Test
	public void testConnectionRequestMessageEquals() {
		EqualsVerifier.forClass(ConnectionRequestMessage.class)
				.withPrefabValues(UUID.class, UUID.randomUUID(),
						UUID.randomUUID()).verify();
	}

	@Test
	public void testConnectionResponseMessage() {
		UUID correlator = UUID.randomUUID();
		ConnectionResponseMessage m = new ConnectionResponseMessage(correlator);
		Assert.assertEquals(correlator, m.getCorrelator());
	}

	@Test
	public void testConnectionResponseMessageEquals() {
		EqualsVerifier.forClass(ConnectionResponseMessage.class)
				.withPrefabValues(UUID.class, UUID.randomUUID(),
						UUID.randomUUID()).verify();
	}

	@Test
	public void testConnectionRequestDetailsMessage()
			throws UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		int port = 1234;
		boolean punchHole = true;
		ConnectionRequestDetailsMessage m = new ConnectionRequestDetailsMessage(
				ip, port, punchHole);
		Assert.assertEquals(ip, m.getIP());
		Assert.assertEquals(port, m.getPort());
		Assert.assertEquals(punchHole, m.isPunchHole());
		punchHole = false;
		m = new ConnectionRequestDetailsMessage(ip, port, punchHole);
		Assert.assertEquals(ip, m.getIP());
		Assert.assertEquals(port, m.getPort());
		Assert.assertEquals(punchHole, m.isPunchHole());
	}

	@Test
	public void testConnectionRequestDetailsMessageEquals()
			throws UnknownHostException {
		EqualsVerifier.forClass(ConnectionRequestDetailsMessage.class)
				.withPrefabValues(InetAddress.class,
						InetAddress.getByName("192.168.7.6"),
						InetAddress.getByName("192.168.7.7")).verify();
	}

	@Test
	public void testConnectionRequestAckMessage() throws UnknownHostException {
		UUID correlator = UUID.randomUUID();
		InetAddress ip = InetAddress.getLocalHost();
		int port = 1234;
		ConnectionRequestAckMessage m = new ConnectionRequestAckMessage(
				correlator, ip, port);
		Assert.assertEquals(ip, m.getIP());
		Assert.assertEquals(port, m.getPort());
		Assert.assertEquals(correlator, m.getCorrelator());

	}

	@Test
	public void testConnectionRequestAckMessageEquals()
			throws UnknownHostException {
		EqualsVerifier.forClass(ConnectionRequestAckMessage.class)
				.withPrefabValues(UUID.class, UUID.randomUUID(),
						UUID.randomUUID()).withPrefabValues(InetAddress.class,
						InetAddress.getByName("192.168.7.6"),
						InetAddress.getByName("192.168.7.7")).verify();
	}
}
