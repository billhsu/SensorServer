package io.github.billhsu.sensorserver;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.java_websocket.WebSocketImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SensorServer extends Activity implements SensorEventListener {
    private SensorWebSocketServer server;
    static final String LOG_TAG = "SensorServer";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mRotationVectorSensor;
    private TextView ipAddr;
    static public TextView connNum;
    private final float[] mRotationMatrix = new float[16];
    public static Handler myConnectionsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int port = 8080;
        WebSocketImpl.DEBUG = true;
        server = new SensorWebSocketServer(
                new InetSocketAddress(port));
        server.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_server);

        ipAddr = (TextView) findViewById(R.id.ipAddr);
        connNum = (TextView) findViewById(R.id.connNum);
        final Handler myIPHandler = new Handler() {
            public void handleMessage(Message msg) {
                ipAddr.setText("Server: ws://" + msg.obj + ":" + port);
            }
        };

        myConnectionsHandler = new Handler() {
            public void handleMessage(Message msg) {
                connNum.setText("Connections: " + msg.obj);
            }
        };

        Thread get_IP = new Thread() {
            @Override
            public void run() {
                Message msg = myIPHandler.obtainMessage();
                try {
                    msg.obj = get_ip().getHostAddress();
                    myIPHandler.sendMessage(msg);
                } catch (Exception exp) {
                    Log.e(LOG_TAG, exp.toString());
                }
            }
        };
        get_IP.start();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sensor_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x,y,z;
        float[] orientation = new float[3];

        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            server.sendToAll("ACCL: " + x + " " + y + " " + z);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            server.sendToAll("GYRO: " + x + " " + y + " " + z);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix , sensorEvent.values);
            SensorManager.getOrientation(mRotationMatrix, orientation);
            server.sendToAll("ROTN: " + orientation[0] + " " + orientation[1] + " " + orientation[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    static InetAddress get_ip() throws SocketException {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface ni;
        while (nis.hasMoreElements()) {
            ni = nis.nextElement();
            if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    //filter for ipv4/ipv6
                    if (ia.getAddress().getAddress().length == 4) {
                        //4 for ipv4, 16 for ipv6
                        return ia.getAddress();
                    }
                }
            }
        }
        return null;
    }

}
