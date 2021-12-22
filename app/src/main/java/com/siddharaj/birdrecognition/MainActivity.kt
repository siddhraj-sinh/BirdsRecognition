package com.siddharaj.birdrecognition

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.siddharaj.birdrecognition.ml.BirdsModel
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var button:Button
    private lateinit var tvOutput:TextView

    //request camera permission
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        if(granted){
           takePicturePreview.launch(null)
        }
        else{
            Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show()
        }

    }

    //launch camera and take picture
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){ bitmap->
        if(bitmap != null){
            imageView.setImageBitmap(bitmap)
            outputGenerator(bitmap)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.btn_capture_image)
        tvOutput = findViewById(R.id.tv_output)


        button.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                takePicturePreview.launch(null)
            }
            else{
              requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }

    }

    private fun outputGenerator(bitmap: Bitmap){

      //declaring tensor flow lite model variable
      val birdModel = BirdsModel.newInstance(this)
      //converting bitmap into tensor flow image
      val tfImage = TensorImage.fromBitmap(bitmap)
       //process the image using trained model and sort it in descending
        val outputs = birdModel.process(tfImage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score }
            }

        //getting result having high probability
        val highProbabilityOutput = outputs[0]
        //setting output text
        tvOutput.text = highProbabilityOutput.label

    }
}