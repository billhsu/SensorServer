package io.github.billhsu.sensorserver;

import android.os.Message;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 * Created by bill on 10/11/14.
 */
public class SensorWebSocketServer extends WebSocketServer {

    public SensorWebSocketServer(int port) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public SensorWebSocketServer(InetSocketAddress address) {
        super( address );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        Message msg = SensorServer.myConnectionsHandler.obtainMessage();
        msg.obj = getConnectedClientNumber();
        SensorServer.myConnectionsHandler.sendMessage(msg);
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered." );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        Message msg = SensorServer.myConnectionsHandler.obtainMessage();
        msg.obj = getConnectedClientNumber();
        SensorServer.myConnectionsHandler.sendMessage(msg);
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress()  + " left." );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress()  + ": " + message );
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
        }
    }

    public void sendToAll( String text ) {
        Collection<WebSocket> con = connections();
        synchronized ( con ) {
            try {
                for (WebSocket c : con) {
                    if (c != null) c.send(text);
                }
            }
            catch (Exception exp)
            {
                exp.printStackTrace();
            }
        }
    }

    public int getConnectedClientNumber() {
        return connections().size();
    }
}
