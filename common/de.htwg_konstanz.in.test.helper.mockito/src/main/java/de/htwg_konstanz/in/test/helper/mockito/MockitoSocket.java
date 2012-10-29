package de.htwg_konstanz.in.test.helper.mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class MockitoSocket {
	private final Socket socketMock;
	private final ByteArrayOutputStream os;

	public MockitoSocket() throws IOException {
		socketMock = mock(Socket.class);
		os = new ByteArrayOutputStream();
		when(socketMock.getOutputStream()).thenReturn(os);
	}

	public MockitoSocket setClosed(boolean closed) {
		when(socketMock.isClosed()).thenReturn(closed);
		return this;
	}

	public MockitoSocket setConnected(boolean connected) {
		when(socketMock.isConnected()).thenReturn(connected);
		return this;
	}

	public MockitoSocket setRemoteSocketAddress(
			InetSocketAddress remoteSocketAddress) {
		when(socketMock.getRemoteSocketAddress()).thenReturn(
				remoteSocketAddress);
		when(socketMock.getInetAddress()).thenReturn(
				remoteSocketAddress.getAddress());
		when(socketMock.getPort()).thenReturn(remoteSocketAddress.getPort());
		return this;
	}

	public MockitoSocket setLocalSocketAddress(
			InetSocketAddress localSocketAddress) {
		when(socketMock.getLocalSocketAddress()).thenReturn(localSocketAddress);
		when(socketMock.getLocalAddress()).thenReturn(
				localSocketAddress.getAddress());
		when(socketMock.getLocalPort())
				.thenReturn(localSocketAddress.getPort());
		return this;
	}

	public MockitoSocket setInputStreamData(byte[] data) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		when(socketMock.getInputStream()).thenReturn(is);
		return this;
	}
	
	public byte[] getOutputStreamData() {
		return os.toByteArray();
	}
	
	public InputStream getOutputStreamDataAsInputStream() {
		return new ByteArrayInputStream(os.toByteArray());
	}
	
	

	public Socket getSocket() {
		return socketMock;
	}

//	private byte[] concat(byte[] data1, byte[] data2) {
//		byte[] result = Arrays.copyOf(data1, data1.length + data2.length);
//		System.arraycopy(data2, 0, result, data1.length, data2.length);
//		return result;
//	}

}
