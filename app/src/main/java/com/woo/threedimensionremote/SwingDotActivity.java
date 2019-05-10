package com.woo.threedimensionremote;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;

import com.woo.threedimensionremote.protocol.Sender;

public class SwingDotActivity extends AppCompatActivity implements SensorEventListener {
    private static String TAG = "SwingDotActivity";
    private static final float TV_RATIO_Y = 1080f / 1920f;
    private float x, y;
    private float paintX, paintY;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer, mSensorLinearAcceleration, mSensorGyroscope;
    public static int whichSensor = 0; // 0: acc 1: linearAcc 2:gyroscope
    private SwingDotView mSwingDotView;
    private boolean mShowPath = false;
    private int sensitivity;

    public static String mStringServerIP;

    private long mAccTime, mGyroTime, mLinAccTime, mMagFieldTime;
    private boolean dataStop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swing_dot);

        mSwingDotView = findViewById(R.id.swing_dot_view);
        SeekBar seekBar = findViewById(R.id.seek_bar_sensitivity);

        initSensor();
        if (mStringServerIP != null) {
            Sender.getInstance().init(SwingDotActivity.this);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: " + progress);
                mSwingDotView.setSensitivity(progress);
                sensitivity = progress + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initSensor() {
        whichSensor = getIntent().getIntExtra("whichSensor", 1);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mSensorAccelerometer && whichSensor == 0 && sensorDataCanEntered(event.sensor)) {
//        if (event.sensor == mSensorAccelerometer && whichSensor == 0){// && sensorDataCanEntered(event.sensor)) {
//            Log.d(TAG, "onSensorChanged: " + event.values[0] + " " + event.values[2]);
            x = -event.values[0];
            y = -(event.values[1]);
//        } else if (event.sensor == mSensorLinearAcceleration && whichSensor == 1 && sensorDataCanEntered(event.sensor)) {
        } else if (event.sensor == mSensorLinearAcceleration && whichSensor == 1 ){//&& sensorDataCanEntered(event.sensor)) {
            x = event.values[0];
            y = event.values[2];
        } else if (event.sensor == mSensorGyroscope && whichSensor == 2 && sensorDataCanEntered(event.sensor)) {
//        } else if (event.sensor == mSensorGyroscope){// && whichSensor == 2 ){//&& sensorDataCanEntered(event.sensor)) {
            x = -event.values[2];
            y = -event.values[0];
            paintX += -event.values[2];
            paintY += -event.values[0];
        }
        else return;

        // 0.1 eliminate the slight shake by user hand.
        if (Math.abs(x) < 0.1f && Math.abs(y) < 0.1f) return;

        Log.d(TAG, "onSensorChanged: "+ x * sensitivity + " " + y * sensitivity);
        mSwingDotView.setPointPos(paintX, paintY);
        mSwingDotView.invalidate();
        sendData(x * sensitivity, y * sensitivity * TV_RATIO_Y);
    }

    private boolean sensorDataCanEntered(Sensor sensor) {
        if (dataStop) return false;
        long currentTime = System.currentTimeMillis();
        if (sensor == mSensorAccelerometer) {
            if (currentTime - mAccTime < 150)
                return false;
            else mAccTime = System.currentTimeMillis();
        } else if (sensor == mSensorLinearAcceleration) {
            if (currentTime - mLinAccTime < 50)
                return false;
            else mLinAccTime = System.currentTimeMillis();
        } else if (sensor == mSensorGyroscope) {
            if (currentTime - mGyroTime < 65)
                return false;
            else mGyroTime = System.currentTimeMillis();
        } else return false;

        return true;
    }

    private void sendData(float x, float y) {
        byte[] axis;

        axis = MathUtil.packetBytes(MathUtil.int2ByteArray((int) x), MathUtil.int2ByteArray((int) y));
        Sender.getInstance().sendData(axis);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorAccelerometer != null)
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (mSensorLinearAcceleration != null)
            mSensorManager.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        if (mSensorGyroscope != null)
            mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onDestroy() {
        if (mSensorAccelerometer != null)
            mSensorManager.unregisterListener(this);
        if (mSensorLinearAcceleration != null)
            mSensorManager.unregisterListener(this);
        if (mSensorGyroscope != null)
            mSensorManager.unregisterListener(this);
        Sender.getInstance().deInit();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_data_style:
                mShowPath = !mShowPath;
                mSwingDotView.setShowPath(mShowPath);
                break;
            case R.id.menu_data_ip:
                final EditText serverIp = new EditText(this);
                serverIp.setText("10.1.1.108");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("pls input server ip:")
                        .setView(serverIp)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mStringServerIP = serverIp.getText().toString();
                                Sender.getInstance().init(SwingDotActivity.this);
                            }
                        })
                        .setNegativeButton("cancel", null)
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
