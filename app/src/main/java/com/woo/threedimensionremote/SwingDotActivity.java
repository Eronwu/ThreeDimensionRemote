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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.woo.threedimensionremote.protocol.Sender;

public class SwingDotActivity extends AppCompatActivity implements SensorEventListener {
    private static String TAG = "SwingDotActivity";
    private static final float TV_RATIO_Y = 720f / 1280f; // wm size is this TODO!
    private static final float ERR_NUM = 666f;

    enum SEND_MSG_TYPE {
        AXIS,
        LEFT_CLICK,
        RIGHT_CLICK
    }

    private float x, y;
    private float lastX = ERR_NUM, lastY = ERR_NUM;
    private float paintX, paintY;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer, mSensorLinearAcceleration, mSensorGyroscope, mSensorRotationVector, mSensorGameRotationVector, mSensorMagneticField;
    public static int whichSensor = 0; // 0: acc 1: linearAcc 2:gyroscope 3:rotation_Vector
    private SwingDotView mSwingDotView;
    private boolean mShowPath = false;
    private int sensitivity;

    public static String mStringServerIP;

    private long mAccTime, mGyroTime, mLinAccTime, mRotationTime;
    private boolean dataStop = false;
    private float mDefaultX = 0f;
    private float mDefaultY = 0f;
    private float rawX = 0f;
    private float rawY = 0f;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientationAngles = new float[3];
    private float[] mOrientationRawAngles = new float[3];
    private float[] mOrientationLastAngles = new float[3];
    private float[] mDAngles = new float[3];
    private float[] mDefaultAngles = new float[3];
    private float dx, dy;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

//    private final float[] rotationMatrix = new float[9];
//    private final float[] orientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swing_dot);

        mSwingDotView = findViewById(R.id.swing_dot_view);
        Button buttonTarget = findViewById(R.id.button_set_aim);
        Button buttonLeftClick = findViewById(R.id.button_left_click);
        Button buttonRightClick = findViewById(R.id.button_right_click);
        SeekBar seekBar = findViewById(R.id.seek_bar_sensitivity);

        initSensor();
        if (mStringServerIP != null) {
            Sender.getInstance().init(SwingDotActivity.this);
        }

        sensitivity = seekBar.getProgress() + 1;
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
        sensitivity = seekBar.getProgress() + 1;

        buttonTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < mDefaultAngles.length; i++)
                    mDefaultAngles[i] = mOrientationRawAngles[i];
                sendData(-1280f, -720f, SEND_MSG_TYPE.AXIS);
                sendData(1280 / 2f, 720 / 2f, SEND_MSG_TYPE.AXIS); // TODO: use wm size
                sendData(1f, 1f, SEND_MSG_TYPE.AXIS);
//                SensorManager.remapCoordinateSystem(mRotationMatrix, );
            }
        });

        buttonLeftClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(0, 0, SEND_MSG_TYPE.LEFT_CLICK);
            }
        });

        buttonRightClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(0, 0, SEND_MSG_TYPE.RIGHT_CLICK);
            }
        });
    }

    private void initSensor() {
        whichSensor = getIntent().getIntExtra("whichSensor", 1);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        mSensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorGameRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        for (int i = 0; i < 3; i++)
            mOrientationLastAngles[i] = ERR_NUM;
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        for (int i = 0; i < mOrientationAngles.length; i++) {
            mOrientationRawAngles[i] = mOrientationAngles[i];
            mOrientationAngles[i] -= mDefaultAngles[i];
        }
//        Log.d(TAG, "updateOrientationAngles: " + mOrientationAngles[0]*180f/Math.PI + " " + mOrientationRawAngles[0]*180f/Math.PI );
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor == mSensorAccelerometer && whichSensor == 0 && sensorDataCanEntered(mSensorAccelerometer)) {
        if (event.sensor == mSensorAccelerometer && whichSensor == 0){// && sensorDataCanEntered(event.sensor)) {
//            Log.d(TAG, "onSensorChanged: " + event.values[0] + " " + event.values[2]);
//            x = -event.values[0];
//            y = -(event.values[1]);
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && whichSensor == 0) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
//        } else if (event.sensor == mSensorLinearAcceleration && whichSensor == 1 && sensorDataCanEntered(event.sensor)) {
        } else if (event.sensor == mSensorLinearAcceleration && whichSensor == 1) {//&& sensorDataCanEntered(event.sensor)) {
            x = event.values[0];
            y = event.values[2];
        } else if (event.sensor == mSensorGyroscope && whichSensor == 2 && sensorDataCanEntered(event.sensor)) {
//        } else if (event.sensor == mSensorGyroscope){// && whichSensor == 2 ){//&& sensorDataCanEntered(event.sensor)) {
            x = -event.values[2] - mDefaultX;
            y = -event.values[0] - mDefaultY;
            paintX += -event.values[2] - mDefaultX;
            paintY += -event.values[0] - mDefaultY;
//        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){// && sensorDataCanEntered(event.sensor)) {
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && whichSensor == 3 && sensorDataCanEntered(event.sensor)) {
            // However, the orientation may drift somewhat over time. --from google. deprecated
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            // Express the updated rotation matrix as three orientation angles.
            SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
            for (int i = 0; i < mDefaultAngles.length; i++) {
                mOrientationRawAngles[i] = mOrientationAngles[i];
                mOrientationAngles[i] -= mDefaultAngles[i];
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR && whichSensor == 4 && sensorDataCanEntered(event.sensor)) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            // Express the updated rotation matrix as three orientation angles.
            SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
            for (int i = 0; i < mDefaultAngles.length; i++) {
                mOrientationRawAngles[i] = mOrientationAngles[i];
                mOrientationAngles[i] -= mDefaultAngles[i];
            }
        } else return;

        if (whichSensor == 0)
            updateOrientationAngles();

        // 0.1 eliminate the slight shake by user hand.
        float shakeVal = 0.1f;

        if (mOrientationLastAngles[0] == ERR_NUM) {
            for (int i = 0; i < mDefaultAngles.length; i++)
                mOrientationLastAngles[i] = mOrientationAngles[i];
            return;
        }

        if (true) { //(event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            shakeVal = 0.01f;
            for (int i = 0; i < 3; i++) {
                mDAngles[i] = mOrientationAngles[i] - mOrientationLastAngles[i];
            }
            Log.d(TAG, "onSensorChanged:   " + (float) (Math.round(mDAngles[0] * 1000)) / 1000);
            if (Math.abs(mDAngles[0]) < shakeVal && Math.abs(mDAngles[1]) < shakeVal) return;

            x = (float) Math.tan((double) (mOrientationAngles[0])) * sensitivity * sensitivity;
            y = (float) Math.tan((double) mOrientationAngles[1]) * sensitivity * sensitivity;

            dx = ((float) Math.tan((double) mOrientationAngles[0]) - (float) Math.tan((double) mOrientationLastAngles[0])) * sensitivity * sensitivity;
            dy = ((float) Math.tan((double) mOrientationAngles[1]) - (float) Math.tan((double) mOrientationLastAngles[1])) * sensitivity * sensitivity;// * TV_RATIO_Y;
//            Log.d(TAG, "onSensorChanged: dx:" + dx + " dy:" + dy+ " angle x:" + mOrientationAngles[0]*180f/Math.PI);
            for (int i = 0; i < 3; i++)
                mOrientationLastAngles[i] = mOrientationAngles[i];
        } else {
        }

        sendData(dx, dy, SEND_MSG_TYPE.AXIS);
        mSwingDotView.setPointPos(x, y);
        mSwingDotView.invalidate();

    }

    private float[] axisAmplification(float[] axis) {
        if (axis.length == 2) {
            axis[0] *= 20f;
            axis[1] *= 20f;
        }
        return axis;
    }

    // axis[x, y],
    private float[] axisCalibration(float dx, float dy) {
        float axis[] = new float[2];

        if (Math.abs(dx) > 1f) {
            dx = dx > 0 ? (dx - 2.1f) : (dx + 2.1f);
        }

        axis[0] = dx;
        axis[1] = dy;
        return axis;
    }

    private boolean sensorDataCanEntered(Sensor sensor) {
        if (dataStop) return false;
        long currentTime = System.currentTimeMillis();
        if (sensor == mSensorAccelerometer) {
            if (currentTime - mAccTime < 60)
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
        } else if (sensor == mSensorRotationVector) {
            if (currentTime - mRotationTime < 65)
                return false;
            else mRotationTime = System.currentTimeMillis();
        } else if (sensor == mSensorGameRotationVector) {
            if (currentTime - mRotationTime < 65)
                return false;
            else mRotationTime = System.currentTimeMillis();
        } else return false;

        return true;
    }

    // msg:
    // 0: AXIS
    // 1: LEFT CLICK
    // 2: RIGHT_CLICK
    private void sendData(float x, float y, SEND_MSG_TYPE msg) {
        byte[] data;

        switch (msg) {
            case AXIS:
                if (x == 0f && y == 0f)
                    return;

                data = MathUtil.packetAxisBytes(MathUtil.int2ByteArray((int) x), MathUtil.int2ByteArray((int) y));
                break;
            case LEFT_CLICK:
                data = new byte[2];
                data[0] = 1;
                data[1] = 0;
                break;
            case RIGHT_CLICK:
                data = new byte[2];
                data[0] = 1;
                data[1] = 1;
                break;
            default:
                Log.e(TAG, "sendData: err!");
                return;
        }
        Sender.getInstance().sendData(data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorAccelerometer != null)
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        if (mSensorMagneticField != null)
            mSensorManager.registerListener(this, mSensorMagneticField, SensorManager.SENSOR_DELAY_UI);
        if (mSensorLinearAcceleration != null)
            mSensorManager.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        if (mSensorGyroscope != null)
            mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_UI);
        if (mSensorRotationVector != null)
            mSensorManager.registerListener(this, mSensorRotationVector, SensorManager.SENSOR_DELAY_UI);
        if (mSensorRotationVector != null)
            mSensorManager.registerListener(this, mSensorGameRotationVector, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onDestroy() {
        if (mSensorAccelerometer != null)
            mSensorManager.unregisterListener(this);
        if (mSensorMagneticField != null)
            mSensorManager.unregisterListener(this);
        if (mSensorLinearAcceleration != null)
            mSensorManager.unregisterListener(this);
        if (mSensorGyroscope != null)
            mSensorManager.unregisterListener(this);
        if (mSensorRotationVector != null)
            mSensorManager.unregisterListener(this);
        if (mSensorGameRotationVector != null)
            mSensorManager.unregisterListener(this);
        Sender.getInstance().deInit();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        mSensorManager.unregisterListener(this);
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
                serverIp.setText("10.1.1.103");
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
