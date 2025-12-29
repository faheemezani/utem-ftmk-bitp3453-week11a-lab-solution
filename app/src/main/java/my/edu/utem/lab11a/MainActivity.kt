package my.edu.utem.lab11a

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometer: Sensor? = null

    // Use a sampling rate logic to ensure the UI doesn't work harder than the screen refresh
    private var lastUiUpdateTime = 0L
    private var isFirstRead = true
    private var isTracking = false
    private var permissionGranted = false
    private var processingJob: Job? = null
    private var lastStepCount: Int = 0

    private lateinit var tvStepCount: TextView
    private lateinit var accelBar: ProgressBar
    private lateinit var tvMagnitude: TextView
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        executeRuntimePermission()
        initializeUI(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putInt("SAVED_STEPS", tvStepCount.text.toString().toInt())
            putBoolean("SAVED_TRACKING", isTracking)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (isTracking) {
            initializeSensors()
            registerSensors()
        }
    }

    override fun onPause() {
        super.onPause()
        // Stops all sensors registered to this listener
        sensorManager.unregisterListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted! You can now initialize sensors.
                permissionGranted = true
            } else {
                // Permission denied
                // Inform the user that the app won't work without this permission.
                Toast.makeText(
                    this, "Activity Recognition is required for step counting/detection.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeSensors() {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun registerSensors() {
        // Register STEP COUNTER
//        stepCounterSensor?.let {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
//        }

        // Register STEP DETECTOR
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Register ACCELEROMETER
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun initializeUI(savedInstanceState: Bundle?) {
        tvStepCount = findViewById(R.id.tvStepCount)
        accelBar = findViewById(R.id.accelBar)
        tvMagnitude = findViewById(R.id.tvMagnitude)
        btnStart = findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            isTracking = !isTracking
            if (isTracking) {
                btnStart.text = "STOP TRACKING"
                initializeSensors()
                registerSensors()
            } else {
                btnStart.text = "START TRACKING"
                sensorManager.unregisterListener(this)
            }
        }

        savedInstanceState?.let {
            isTracking = it.getBoolean("SAVED_TRACKING")
            lastStepCount = it.getInt("SAVED_STEPS")
            tvStepCount.text = lastStepCount.toString()
        }
    }

    private fun executeRuntimePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun calculateMagnitude(sensorValues: FloatArray): Float {
        var magnitude = 0f
        for (value in sensorValues) {
            magnitude += value * value
        }
        return sqrt(magnitude)
    }

    private fun updateUI(magnitude: Float) {
        val df = DecimalFormat("#.##")
        tvMagnitude.text = df.format(magnitude)

        // ObjectAnimator makes the 'aesthetic' bar feel fluid rather than jumpy
        ObjectAnimator.ofInt(accelBar, "progress", (magnitude * 10).toInt())
            .setDuration(60) // Matches the SENSOR_DELAY_UI interval
            .start()
    }

    private fun processAccelerometer(event: SensorEvent) {
        val sensorValues = event.values?.copyOf() ?: return

        // Use the 'launch' return value to prevent overlapping updates
        // if a previous calculation is still running
        if (processingJob?.isActive == true) return

        // Offload the math to a background thread
        processingJob = lifecycleScope.launch(Dispatchers.Default) {
            // Perform math off-thread
            val magnitude = calculateMagnitude(sensorValues)

            val currentTime = System.currentTimeMillis()

            // Limit UI refreshes to ~30 FPS (every 33ms)
            // even if the sensor fires slightly faster than expected
            if (currentTime - lastUiUpdateTime > 33) {
                // Return to Main Thread for UI
                withContext(Dispatchers.Main) {
                    updateUI(magnitude)
                }
                lastUiUpdateTime = currentTime
            }
        }
    }


    /**
     * SensorEventListener methods
     */

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Nothing to do here, but you still have to implement this method.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?: return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                //tvStepCount.text = event.values[0].toInt().toString()
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (isFirstRead) isFirstRead = false
                else if (lastStepCount >= 0) lastStepCount += 1
                tvStepCount.text = lastStepCount.toString()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                processAccelerometer(event)
            }
        }

    }
}