package de.danoeh.antennapod.playback.service.internal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;

public class ShakeListener implements SensorEventListener {
    private static final String TAG = ShakeListener.class.getSimpleName();
    private final long mResetCount;

    private Sensor mAccelerometer;
    private SensorManager mSensorMgr;
    private final SleepTimer mSleepTimer;
    private final Context mContext;

    public ShakeListener(Context context, long resetCount, SleepTimer sleepTimer) {
        mContext = context;
        mSleepTimer = sleepTimer;
        mResetCount = resetCount;
        resume();
    }

    private void resume() {
        // only a precaution, the user should actually not be able to activate shake to reset
        // when the accelerometer is not available
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        mAccelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (!mSensorMgr.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI)) { // if not supported
            mSensorMgr.unregisterListener(this);
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
    }

    public void pause() {
        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this);
            mSensorMgr = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

        double gForce = Math.sqrt(gX*gX + gY*gY + gZ*gZ);
        if (gForce > 2.25) {
            Log.d(TAG, "Detected shake " + gForce);
            mSleepTimer.reset(mResetCount);

            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(100);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}