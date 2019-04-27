package com.woo.threedimensionremote;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = "MainActivity";
    String mStringAcc, mStringLinearAcc, mStringGyroscope, mStringMagField;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer, mSensorLinearAcceleration, mSensorGyroscope, mSensorMagneticField;
    private TextView mTextViewAccelerometerX, mTextViewAccelerometerY, mTextViewAccelerometerZ;
    private TextView mTextViewGyroscopeX, mTextViewGyroscopeY, mTextViewGyroscopeZ;
    private TextView mTextViewLinearAccelerationX, mTextViewLinearAccelerationY, mTextViewLinearAccelerationZ;
    private TextView mTextViewMagneticFieldX, mTextViewMagneticFieldY, mTextViewMagneticFieldZ;
    private long mAccTime, mGyroTime, mLinAccTime, mMagFieldTime;
    private int showStyle = 0; // 0: scroll 1: all data list
    private boolean dataStop = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensor();
    }

    private void initSensor() {
        String s = new String();
        TextView textView;
        Button buttonAcclerometer, buttonLinearAcceleration;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        for (Sensor sensor : deviceSensors) {
            Log.d(TAG, "initSensor: " + sensor.getName());
            s = sensor.getName() + " \n" + s;
        }
        textView = findViewById(R.id.sensor_name_text_view);
        textView.setText(s);

        mTextViewAccelerometerX = findViewById(R.id.text_view_accelerometer_x_axis);
//        mTextViewAccelerometerY = findViewById(R.id.text_view_accelerometer_y_axis);
//        mTextViewAccelerometerZ = findViewById(R.id.text_view_accelerometer_z_axis);
        mTextViewGyroscopeX = findViewById(R.id.text_view_gyroscope_x_axis);
//        mTextViewGyroscopeY = findViewById(R.id.text_view_gyroscope_y_axis);
//        mTextViewGyroscopeZ = findViewById(R.id.text_view_gyroscope_z_axis);
        mTextViewLinearAccelerationX = findViewById(R.id.text_view_linear_acceleration_x_axis);
//        mTextViewLinearAccelerationY = findViewById(R.id.text_view_linear_acceleration_y_axis);
//        mTextViewLinearAccelerationZ = findViewById(R.id.text_view_linear_acceleration_z_axis);
        mTextViewMagneticFieldX = findViewById(R.id.text_view_magnetic_field_x_axis);
//        mTextViewMagneticFieldY = findViewById(R.id.text_view_magnetic_field_y_axis);
//        mTextViewMagneticFieldZ = findViewById(R.id.text_view_magnetic_field_z_axis);

        buttonAcclerometer = findViewById(R.id.button_accelerometer);
        buttonLinearAcceleration = findViewById(R.id.button_linear_acceleration);

        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewAccelerometerX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewGyroscopeX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewLinearAccelerationX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewMagneticFieldX.setMovementMethod(ScrollingMovementMethod.getInstance());

        mStringAcc = mStringLinearAcc = mStringGyroscope = mStringMagField = "";

        buttonAcclerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent()
                        .setClass(MainActivity.this, SwingDotActivity.class)
                        .putExtra("whichSensor", 0));
            }
        });

        buttonLinearAcceleration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent()
                        .setClass(MainActivity.this, SwingDotActivity.class)
                        .putExtra("whichSensor", 1));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorAccelerometer != null)
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (mSensorLinearAcceleration != null)
            mSensorManager.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        if (mSensorGyroscope != null)
            mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        if (mSensorMagneticField != null)
            mSensorManager.registerListener(this, mSensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        mAccTime = mGyroTime = mLinAccTime = mMagFieldTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        if (mSensorAccelerometer != null)
            mSensorManager.unregisterListener(this);
        if (mSensorLinearAcceleration != null)
            mSensorManager.unregisterListener(this);
        if (mSensorGyroscope != null)
            mSensorManager.unregisterListener(this);
        if (mSensorMagneticField != null)
            mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String s = floatToInaccurateString(event.values[0]) + " " +
                floatToInaccurateString(event.values[1]) + " " +
                floatToInaccurateString(event.values[2]);
        if (event.sensor == mSensorAccelerometer && sensorDataCanEntered(event.sensor)) {
            mStringAcc += "\n" + s;
            if (showStyle == 0)
                mTextViewAccelerometerX.setText(s);
            else
                mTextViewAccelerometerX.setText(mStringAcc);
        } else if (event.sensor == mSensorGyroscope && sensorDataCanEntered(event.sensor)) {
            mStringLinearAcc += "\n" + s;

            if (showStyle == 0)
                mTextViewGyroscopeX.setText(s);
            else
                mTextViewGyroscopeX.setText(mStringLinearAcc);
        } else if (event.sensor == mSensorLinearAcceleration && sensorDataCanEntered(event.sensor)) {
            mStringGyroscope += "\n" + s;

            if (showStyle == 0)
                mTextViewLinearAccelerationX.setText(s);
            else
                mTextViewLinearAccelerationX.setText(mStringGyroscope);
        } else if (event.sensor == mSensorMagneticField && sensorDataCanEntered(event.sensor)) {
            mStringMagField += "\n" + s;

            if (showStyle == 0)
                mTextViewMagneticFieldX.setText(s);
            else
                mTextViewMagneticFieldX.setText(mStringMagField);
        }
    }

    private boolean sensorDataCanEntered(Sensor sensor) {
        if (dataStop) return false;
        long currentTime = System.currentTimeMillis();
        if (sensor == mSensorAccelerometer) {
            if (currentTime - mAccTime < 500)
                return false;
            else mAccTime = System.currentTimeMillis();
        } else if (sensor == mSensorGyroscope) {
            if (currentTime - mGyroTime < 1000)
                return false;
            else mGyroTime = System.currentTimeMillis();
        } else if (sensor == mSensorLinearAcceleration) {
            if (currentTime - mLinAccTime < 500)
                return false;
            else mLinAccTime = System.currentTimeMillis();
        } else if (sensor == mSensorMagneticField) {
            if (currentTime - mMagFieldTime < 1000)
                return false;
            else mMagFieldTime = System.currentTimeMillis();
        } else return false;

        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String floatToInaccurateString(float f) {
        return Float.toString((int) f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_data_clear:
                mStringAcc = mStringLinearAcc = mStringGyroscope = mStringMagField = "";
                break;
            case R.id.menu_data_style:
                showStyle = showStyle == 0 ? 1 : 0;
                if (showStyle == 1) {
                    // TODO: stop button show
                }
                break;
            case R.id.menu_data_stop:
                dataStop = !dataStop;
        }
        return super.onOptionsItemSelected(item);
    }
}
