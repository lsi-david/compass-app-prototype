package com.example.compassapp;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView textView;
    private ImageView imageView;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    boolean isLastAccelerometerArrayCopied = false;
    boolean isLastMagnetometerCopied = false;

    long lastUpdatedTime = 0;
    float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = findViewById(R.id.tv2);
        imageView = findViewById(R.id.pointer);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == sensorAccelerometer ){
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            isLastAccelerometerArrayCopied = true;
        } else if(event.sensor == sensorMagneticField) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            isLastMagnetometerCopied = true;
        }

        if(isLastAccelerometerArrayCopied && isLastMagnetometerCopied && System.currentTimeMillis() - lastUpdatedTime >250){
            SensorManager.getRotationMatrix(floatRotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

            float orientationInRadians = floatOrientation[0];
            float orientationInDegree = (float) Math.toDegrees(orientationInRadians);

            RotateAnimation rotateAnimation =
                    new RotateAnimation(currentDegree, -orientationInDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);

            imageView.startAnimation(rotateAnimation);

            currentDegree = -orientationInDegree;
            lastUpdatedTime = System.currentTimeMillis();

            int degrees = (int) orientationInDegree;
            textView.setText(degrees + "°");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorMagneticField);
    }
}