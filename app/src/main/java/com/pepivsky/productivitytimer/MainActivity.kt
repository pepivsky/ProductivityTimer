package com.pepivsky.productivitytimer

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
//import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.pepivsky.productivitytimer.databinding.ActivityMainBinding
import java.util.*

const val CHANNEL_ID = "org.hyperskill"
const val NOTIFICATION_ID = 393939
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var timeInMillis = 121L * 1000L
    private var countDownTimer: CountDownTimer? = null
    private var timerIsRunning = false
    private var timeEntered = 0
    private val colors = listOf(
        Color.BLUE,
        Color.BLACK,
        Color.CYAN,
        Color.RED,
        Color.RED,
        Color.GREEN,
        Color.YELLOW,
        Color.MAGENTA
    )
    lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // create channel
        //createChannel()
        binding.textView.text = "00:00"

        binding.startButton.setOnClickListener {
            Log.d("onClick", "button click")
            if (!timerIsRunning) {
                Log.d("onClick", "time started $timerIsRunning")
                initTime()
                binding.progressBar.visibility = View.VISIBLE

                binding.settingsButton.isEnabled = false
            } else {
                Log.d("onClick", "time is running $timerIsRunning")

            }
        }

        binding.resetButton.setOnClickListener {
            countDownTimer?.cancel()
            binding.textView.text = "00:00"
            timerIsRunning = false
            binding.progressBar.visibility = View.GONE

            binding.settingsButton.isEnabled = true
            binding.textView.setTextColor(Color.GRAY)
        }

        binding.settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.settings_dialog, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    timeEntered = editText?.text.toString().toInt()
                    Toast.makeText(this, timeEntered.toString(), Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }
    private fun initTime() {
        countDownTimer = object: CountDownTimer(timeInMillis, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d("tick", "${ millisUntilFinished / 1000 }")
                Log.d("tickreverse", "${ (timeInMillis - millisUntilFinished) / 1000 }")

                updateTextView(millisUntilFinished)

                // change color
                binding.progressBar.indeterminateTintList = ColorStateList.valueOf(colors.random())
            }

            override fun onFinish() {
                Log.d("finished", "counter finished")
                timerIsRunning = false
                binding.progressBar.visibility = View.GONE
            }

        }
        (countDownTimer as CountDownTimer).start()
        timerIsRunning = true
    }

    fun updateTextView(millis: Long) {
        val minutes: Int = ((timeInMillis - millis) / 60_000).toInt()
        val seconds: Int = (((timeInMillis - millis) / 1000) % 60).toInt()

        Log.d("update seconds", "$seconds")
        Log.d("update minutes", "$minutes")


        val timeLeftFormatted: String = java.lang.String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.textView.text = timeLeftFormatted

        val secondsReal = minutes / 60 + seconds
        Log.d("updateTextView", "updateTextView: secondsReal$secondsReal secondsReal > timeEntered = ${secondsReal >  timeEntered} ")
        if (timeEntered != 0 && secondsReal > timeEntered) {
            binding.textView.setTextColor(Color.RED)
            if (timeEntered >= 0) {
                createChannel()
                launchNotification()
            }
        }

    }

    private fun launchNotification() {
        val context = applicationContext
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notification")
            .setContentText("Time exceeded")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

        val notification: Notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_INSISTENT or Notification.FLAG_ONLY_ALERT_ONCE
        notificationManager.notify(NOTIFICATION_ID, notification)

    }
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create channel
            val name = "Delivery status"
            val descriptionText = "Your delivery status"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // register channel
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}