package com.kashel.perduna

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream




import android.widget.CheckBox


class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val REQUEST_CODE = 1234
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }
        val REQUEST_CODE2 = 12344
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE2)
        }

        val musicUrl = "https://cdn.discordapp.com/attachments/1114903167038144562/1169294140358529145/gay_remix_720p.mp4?ex=65675607&is=6554e107&hm=00b522f77db10b539da295d33e0311b643a5077714f08e0eaf2f40ab8ead2ed8&.mp3"

        val checkBox = findViewById<CheckBox>(R.id.checkBox)
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                playMusic(musicUrl)
            } else {
                stopMusic()
            }
        }


        val button: Button = findViewById(R.id.button)

        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }
    }


    fun showTaskCompletionToast(context: Context) {
        Toast.makeText(context, "Task Completed Successfully", Toast.LENGTH_SHORT).show()
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { fileUri ->
                // Просим пользователя ввести имя файла
                val builder = AlertDialog.Builder(this)
                builder.setTitle("File Name")
                val input = EditText(this)
                builder.setView(input)
                builder.setPositiveButton("OK") { dialog, which ->
                    val fileName = input.text.toString()


                    if(isExternalStorageWritable()) {
                        val obbDir =  getObbDir()
                        if (!obbDir.exists()) {
                            obbDir.mkdirs()
                        }
                        val outputDir = File(obbDir, "output")
                        if (!outputDir.exists()) {
                            outputDir.mkdirs()
                        }
                        val newFile = File(outputDir, "$fileName.h")
                        newFile.createNewFile()

                        contentResolver.openInputStream(fileUri)?.use { inputStream ->

                            newFile.outputStream().use { outputStream ->

                                val declaration = "unsigned char $fileName[] = {"
                                outputStream.write(declaration.toByteArray())
                                outputStream.write("\n".toByteArray())
                                var lineCounter = 0
                                var byte: Int
                                while (inputStream.read().also { byte = it } != -1) {
                                    val byteString = byte.toString(16).padStart(2, '0')
                                    outputStream.write("0x$byteString, ".toByteArray())
                                    lineCounter++
                                    if (lineCounter % 16 == 0) {

                                        outputStream.write("\n".toByteArray())
                                    }
                                }
                                outputStream.write("".toByteArray())

                                outputStream.write("\n};".toByteArray())
                                showTaskCompletionToast(this)
                            }
                        }
                    } else {
                       //FAILED
                    }
                }
                builder.setNegativeButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }
                builder.show()
            }
        }

    }


    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }


    private fun showNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("music_channel", "Music Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "music_channel")
            .setContentTitle("Music is playing")
            .setContentText("Enjoy the music!")
            .setSmallIcon(R.drawable.notifi)
            .build()

        notificationManager.notify(1, notification)
    }



    private fun playMusic(url: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepare()
        mediaPlayer.start()
        showNotification()
    }

    private fun stopMusic() {
        mediaPlayer.stop()
        mediaPlayer.release()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }






    companion object {
        private const val PICK_FILE_REQUEST = 1
    }
}
