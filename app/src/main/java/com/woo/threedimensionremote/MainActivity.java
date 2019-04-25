package com.woo.threedimensionremote;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer, mSensorLinearAcceleration, mSensorGyroscope, mSensorMagneticField;
    private TextView mTextViewAccelerometerX, mTextViewAccelerometerY, mTextViewAccelerometerZ;
    private TextView mTextViewGyroscopeX, mTextViewGyroscopeY, mTextViewGyroscopeZ;
    private TextView mTextViewLinearAccelerationX, mTextViewLinearAccelerationY, mTextViewLinearAccelerationZ;
    private TextView mTextViewMagneticFieldX, mTextViewMagneticFieldY, mTextViewMagneticFieldZ;
    String mStringAcc, mStringLinearAcc, mStringGyroscope, mStringMagField;
    private boolean mAccShow, mGyroShow, mLinAccShow, mMagFieldShow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensor();
    }

    private void initSensor() {
        String s = new String();
        TextView textView;

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
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewAccelerometerX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewGyroscopeX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewLinearAccelerationX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewMagneticFieldX.setMovementMethod(ScrollingMovementMethod.getInstance());

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSensorAccelerometer != null)
            mSensorManager.unregisterListener(this);
        if (mSensorLinearAcceleration != null)
            mSensorManager.unregisterListener(this);
        if (mSensorGyroscope != null)
            mSensorManager.unregisterListener(this);
        if (mSensorMagneticField != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mSensorAccelerometer && sensorDataCanEntered(event.sensor)) {
            mStringAcc += "\n" + floatToInaccurateString(event.values[0]) + " " +
                    floatToInaccurateString(event.values[1]) + " " +
                    floatToInaccurateString(event.values[2]);
            mTextViewAccelerometerX.setText(mStringAcc);
//            mTextViewAccelerometerY.setText(floatToInaccurateString(event.values[1]));
//            mTextViewAccelerometerZ.setText(floatToInaccurateString(event.values[2]));
        } else if (event.sensor == mSensorGyroscope && sensorDataCanEntered(event.sensor)) {
            mStringLinearAcc += "\n" + floatToInaccurateString(event.values[0]) + " " +
                    floatToInaccurateString(event.values[1]) + " " +
                    floatToInaccurateString(event.values[2]);
            mTextViewGyroscopeX.setText(mStringLinearAcc);
//            mTextViewGyroscopeY.setText(floatToInaccurateString(event.values[1]));
//            mTextViewGyroscopeZ.setText(floatToInaccurateString(event.values[2]));
        } else if (event.sensor == mSensorLinearAcceleration && sensorDataCanEntered(event.sensor)) {
            mStringGyroscope += "\n" + floatToInaccurateString(event.values[0]) + " " +
                    floatToInaccurateString(event.values[1]) + " " +
                    floatToInaccurateString(event.values[2]);
            mTextViewLinearAccelerationX.setText(mStringGyroscope);
//            mTextViewLinearAccelerationY.setText(floatToInaccurateString(event.values[1]));
//            mTextViewLinearAccelerationZ.setText(floatToInaccurateString(event.values[2]));
        } else if (event.sensor == mSensorMagneticField && sensorDataCanEntered(event.sensor)) {
            mStringMagField += "\n" + floatToInaccurateString(event.values[0]) + " " +
                    floatToInaccurateString(event.values[1]) + " " +
                    floatToInaccurateString(event.values[2]);
            mTextViewMagneticFieldX.setText(mStringMagField);
//            mTextViewMagneticFieldY.setText(floatToInaccurateString(event.values[1]));
//            mTextViewMagneticFieldZ.setText(floatToInaccurateString(event.values[2]));
        }

    }

    private boolean sensorDataCanEntered(Sensor sensor) {
        if (sensor == mSensorAccelerometer) {
            if (mAccShow) mAccShow = false;
            else return false;
        } else if (sensor == mSensorGyroscope) {
            if (mGyroShow) mGyroShow = false;
            else return false;
        } else if (sensor == mSensorLinearAcceleration) {
            if (mLinAccShow) mLinAccShow = false;
            else return false;
        } else if (sensor == mSensorMagneticField) {
            if (mMagFieldShow) mMagFieldShow = false;
            else return false;
        } else return false;

        new Thread(new SensorShowThread(sensor)).start();

        return true;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mAccShow = true;
                    break;
                case 2:
                    mGyroShow = true;
                    break;
                case 3:
                    mLinAccShow = true;
                    break;
                case 4:
                    mMagFieldShow = true;
                    break;
            }
            super.handleMessage(msg);
        }
    };

    class SensorShowThread extends Thread {
        private Sensor sensor;

        public SensorShowThread(Sensor sensor) {
            this.sensor = sensor;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
//                Message msg = new Message();
                if (sensor == mSensorAccelerometer) {
                    mAccShow = true;
                } else if (sensor == mSensorGyroscope) {
                    mGyroShow = true;
                } else if (sensor == mSensorLinearAcceleration) {
                    mLinAccShow = true;
                } else if (sensor == mSensorMagneticField) {
                    mMagFieldShow = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        if (item.getItemId() == R.id.menu_data_clear) {
            mStringAcc = mStringLinearAcc = mStringGyroscope = mStringMagField = "";
        }
        return super.onOptionsItemSelected(item);
    }

}
