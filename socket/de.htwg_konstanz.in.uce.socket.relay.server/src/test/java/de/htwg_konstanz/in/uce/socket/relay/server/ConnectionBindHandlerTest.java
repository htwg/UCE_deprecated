package de.htwg_konstanz.in.uce.socket.relay.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.test.helper.mockito.MockitoSocket;
import de.htwg_konstanz.in.uce.messages.ErrorCode;
import de.htwg_konstanz.in.uce.messages.SemanticLevel;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.messages.UceMessageStaticFactory;
import de.htwg_konstanz.in.uce.messages.UniqueId;
import de.htwg_konstanz.in.uce.socket.relay.messages.RelayMessageReader;
import de.htwg_konstanz.in.uce.socket.relay.messages.RelayUceMethod;

public class ConnectionBindHandlerTest {

    private MockitoSocket socketMock;
    private UceMessage connectionBindMessage;
    private Map<UUID, BlockingQueue<Socket>> connIDToQueue;

    @Before
    public void setUp() throws IOException {
        // prepare
        connectionBindMessage = UceMessageStaticFactory.newUceMessageInstance(
                RelayUceMethod.CONNECTION_BIND, SemanticLevel.REQUEST);

        socketMock = new MockitoSocket();
        socketMock.setConnected(true);

        connIDToQueue = new ConcurrentHashMap<UUID, BlockingQueue<Socket>>();

    }

    @Test
    public void testHandleWithoutUniqueId() throws IOException {
        // prepare
        ConnectionBindHandler connectionBindHandler = new ConnectionBindHandler(
                socketMock.getSocket(), connectionBindMessage, connIDToQueue);

        // execute
        connectionBindHandler.handle();

        // validate
        UceMessage response = RelayMessageReader
                .read(socketMock.getOutputStreamDataAsInputStream());
        Assert.assertTrue(response.isErrorResponse());
        ErrorCode errorCode = response.getAttribute(ErrorCode.class);
        Assert.assertEquals(errorCode.getErrorNumber(), ErrorCode.ErrorCodes.BAD_REQUEST);
    }

    @Test
    public void testHandleWithoutPendingConnection() throws IOException {
        // prepare
        connectionBindMessage.addAttribute(new UniqueId(UUID.randomUUID()));

        ConnectionBindHandler connectionBindHandler = new ConnectionBindHandler(
                socketMock.getSocket(), connectionBindMessage, connIDToQueue);

        // execute
        connectionBindHandler.handle();

        // validate
        UceMessage response = RelayMessageReader
                .read(socketMock.getOutputStreamDataAsInputStream());
        Assert.assertTrue(response.isErrorResponse());
        ErrorCode errorCode = response.getAttribute(ErrorCode.class);
        Assert.assertEquals(errorCode.getErrorNumber(), ErrorCode.ErrorCodes.BAD_REQUEST);
    }
    
    @Test
    public void testHandleSuccess() {
        // prepare
        UUID id = UUID.randomUUID();
        connectionBindMessage.addAttribute(new UniqueId(id));
        BlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<Socket>();
        connIDToQueue.put(id, socketQueue);

        ConnectionBindHandler connectionBindHandler = new ConnectionBindHandler(
                socketMock.getSocket(), connectionBindMessage, connIDToQueue);

        // execute
        connectionBindHandler.handle();
        
        // validate
        Assert.assertTrue(socketQueue.contains(socketMock.getSocket()));
    }
}
