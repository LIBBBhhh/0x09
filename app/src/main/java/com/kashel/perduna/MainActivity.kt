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

class MainActivity : AppCompatActivity() {
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










    companion object {
        private const val PICK_FILE_REQUEST = 1
    }
}
