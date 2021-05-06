package commov.safecity

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class Sensors : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var brightness: Sensor? = null
    private lateinit var lightSensorText: TextView
    private lateinit var lightSensorCircularProgressBar: CircularProgressBar
    private lateinit var accelerometerText: TextView
    private lateinit var sensorsConstraintLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensors)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        lightSensorText = findViewById(R.id.sensors_lightSensorText)
        lightSensorCircularProgressBar = findViewById(R.id.sensors_lightSensor_circularProgressBar)
        sensorsConstraintLayout = findViewById(R.id.sensors_constraintLayout)

        accelerometerText = findViewById(R.id.sensors_accelerometerText)
        setUpSensorStuff()
    }

    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val light = event.values[0]

            lightSensorText.text = getString(R.string.lightSensorText)
                    .plus("  ")
                    .plus(light)
                    .plus("\n")
                    .plus(brightness(light))
            backgroundColor(light)
            lightSensorCircularProgressBar.setProgressWithAnimation(light)
        }

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Sides = Tilting phone left(10) and right(-10)
            val sides = event.values[0]

            // Up/Down = Tilting phone up(10), flat (0), upside-down(-10)
            val upDown = event.values[1]

            accelerometerText.apply {
                rotationX = upDown * 3f
                rotationY = sides * 3f
                rotation = -sides
                translationX = sides * -10
                translationY = upDown * 10
            }

            // Changes the colour of the square if it's completely flat
            val accelerometerSquareColor = if (upDown.toInt() == 0 && sides.toInt() == 0) Color.GREEN else Color.RED
            val accelerometerTextColor = if (upDown.toInt() == 0 && sides.toInt() == 0) Color.BLACK else Color.WHITE
            accelerometerText.setBackgroundColor(accelerometerSquareColor)
            accelerometerText.setTextColor(accelerometerTextColor)

            accelerometerText.text = getString(R.string.accelerometerUpDown)
                    .plus("  ")
                    .plus(upDown.toInt())
                    .plus("\n")
                    .plus(getString(R.string.accelerometerLeftRight))
                    .plus("  ")
                    .plus(sides.toInt())
        }
    }


    private fun brightness(brightness: Float): String {
        return when (brightness.toInt()) {
            0 -> { getString(R.string.brightness_0) }
            in 1..10 -> getString(R.string.brightness_1_10)
            in 11..5000 -> getString(R.string.brightness_11_5000)
            in 5001..25000 -> getString(R.string.brightness_5001_25000)
            else -> getString(R.string.brightness_blind)
        }
    }

    private fun backgroundColor(brightness: Float){
        return when (brightness.toInt()) {
            0 -> { sensorsConstraintLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.brightness_0))
                lightSensorText.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            in 1..10 -> { sensorsConstraintLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.brightness_1_10))
                lightSensorText.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            in 11..5000 -> { sensorsConstraintLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.brightness_11_5000))
                lightSensorText.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            in 5001..25000 -> { sensorsConstraintLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.brightness_5001_25000))
                lightSensorText.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
            else -> { sensorsConstraintLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.brightness_blind))
                lightSensorText.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onResume() {
        super.onResume()
        // Register a listener for the sensor.
        sensorManager.registerListener(this, brightness, SensorManager.SENSOR_DELAY_NORMAL)
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}