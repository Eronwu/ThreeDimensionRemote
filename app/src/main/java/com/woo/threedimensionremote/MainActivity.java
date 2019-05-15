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

import java.math.BigDecimal;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static final String TAG = "ThreeDRemote";
    String mStringAcc, mStringLinearAcc, mStringGyroscope, mStringGameRotation;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer, mSensorLinearAcceleration, mSensorGyroscope, mSensorGameRotation;
    private TextView mTextViewAccelerometerX;
    private TextView mTextViewGyroscopeX;
    private TextView mTextViewLinearAccelerationX;
    private TextView mTextViewGameRotation;
    private long mAccTime, mGyroTime, mLinAccTime, mGameRotationTime;
    private int showStyle = 0; // 0: scroll 1: all data list
    private boolean dataStop = false;
    private final float[] mRotationMatrix = new float[16];
    private final float[] mOrientationAngles = new float[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensor();
    }

    private void initSensor() {
        String s = new String();
        TextView textView;
        Button buttonAcclerometer, buttonLinearAcceleration, buttonGyroscope, buttonRotationVector;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorGameRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        for (Sensor sensor : deviceSensors) {
            Log.d(TAG, "initSensor: " + sensor.getName());
            s = sensor.getName() + " \n" + s;
        }
        textView = findViewById(R.id.sensor_name_text_view);
        textView.setText(s);

        mTextViewAccelerometerX = findViewById(R.id.text_view_accelerometer_x_axis);
        mTextViewGyroscopeX = findViewById(R.id.text_view_gyroscope_x_axis);
        mTextViewLinearAccelerationX = findViewById(R.id.text_view_linear_acceleration_x_axis);
        mTextViewGameRotation = findViewById(R.id.text_view_rotation);

        buttonAcclerometer = findViewById(R.id.button_accelerometer);
        buttonLinearAcceleration = findViewById(R.id.button_linear_acceleration);
        buttonGyroscope = findViewById(R.id.button_gyroscope);
        buttonRotationVector = findViewById(R.id.button_rotation);

        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewAccelerometerX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewGyroscopeX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewLinearAccelerationX.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTextViewGameRotation.setMovementMethod(ScrollingMovementMethod.getInstance());

        mStringAcc = mStringLinearAcc = mStringGyroscope = mStringGameRotation = "";

        // initialize the rotation matrix to identity
        mRotationMatrix[ 0] = 1;
        mRotationMatrix[ 4] = 1;
        mRotationMatrix[ 8] = 1;
        mRotationMatrix[12] = 1;

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

        buttonGyroscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent()
                        .setClass(MainActivity.this, SwingDotActivity.class)
                        .putExtra("whichSensor", 2));
            }
        });
        buttonRotationVector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent()
                        .setClass(MainActivity.this, SwingDotActivity.class)
                        .putExtra("whichSensor", 4));
            }
        });
        buttonRotationVector.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent()
                        .setClass(MainActivity.this, RotationVectorDemo.class));
                return true;
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
        if (mSensorGameRotation != null)
            mSensorManager.registerListener(this, mSensorGameRotation, SensorManager.SENSOR_DELAY_UI);
        mAccTime = mGyroTime = mLinAccTime = mGameRotationTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        if (mSensorAccelerometer != null)
            mSensorManager.unregisterListener(this);
        if (mSensorLinearAcceleration != null)
            mSensorManager.unregisterListener(this);
        if (mSensorGyroscope != null)
            mSensorManager.unregisterListener(this);
        if (mSensorGameRotation != null)
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
//        } else if (event.sensor == mSensorGyroscope && sensorDataCanEntered(event.sensor)) {
        } else if (event.sensor == mSensorGyroscope){
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
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){// && sensorDataCanEntered(event.sensor)) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix , event.values);
            // Express the updated rotation matrix as three orientation angles.
            SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
////            SensorManager.remapCoordinateSystem()
            s = doubleToInaccurateString(Math.toDegrees(mOrientationAngles[0])) + " " +
                    doubleToInaccurateString(Math.toDegrees(mOrientationAngles[1])) + " " +
                    doubleToInaccurateString(Math.toDegrees(mOrientationAngles[2]));
            mStringGameRotation += "\n" + s;

            if (showStyle == 0)
                mTextViewGameRotation.setText(s);
            else
                mTextViewGameRotation.setText(mStringGameRotation);
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
        } else if (sensor == mSensorGameRotation) {
            if (currentTime - mGameRotationTime < 1000)
                return false;
            else mGameRotationTime = System.currentTimeMillis();
        } else return false;

        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String floatToInaccurateString(float f) {
        BigDecimal b = new BigDecimal(f);
        return Float.toString(b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
    }

    private String doubleToInaccurateString(double f) {
        BigDecimal b = new BigDecimal(f);
        return Double.toString(b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
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
                mStringAcc = mStringLinearAcc = mStringGyroscope = mStringGameRotation = "";
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
