package io.github.billhsu.sensorserver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import  java.net.InetSocketAddress;
public class SensorServer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int port = 8080;
        WebSocketImpl.DEBUG = true;
        WebSocketServer server = new WebSocketServer(
                new InetSocketAddress(port)) {
            @Override
            public void onClose(WebSocket conn, int code, String reason,
                                boolean remote) {
                Log.d("SERVER", "onClose()");
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                Log.d("SERVER", "onError()", ex);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                Log.d("SERVER", String.format("onMessage(%s)", message));
                conn.send(message);
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                Log.d("SERVER", "onOpen()");
            }
        };

        server.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_server);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sensor_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
