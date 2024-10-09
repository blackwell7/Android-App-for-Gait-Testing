/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for(landmark in poseLandmarkerResult.landmarks()) {
                for(normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                        linePaint)
                }
            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        // call Gait Analysis Function
        GaitAnalysis(poseLandmarkerResults)

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    fun GaitAnalysis(calcValues: PoseLandmarkerResult)
    {
        // get external storage
        var path_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()

        // set file path
        val file = File(path_name,"Alexisfield_moving_camera.csv")

        // set up timestamp
        val currentTime: Date = Calendar.getInstance().time;
        val formatter = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss:SSS")
        val dateString = formatter.format(currentTime)

        file.appendText(dateString.toString()+",")

        var check = calcValues.landmarks()

        // iterate through landmarks
        for(j in check)
        {
            for(i in j)
            {
                var x_value = i.x() // get x
                var y_value = i.y() // get y
                var z_value = i.z() // get z
                var visible = i.visibility() // get visibility
                var present = i.presence() // get presence
                var line = x_value.toString()+","+y_value.toString()+","+z_value.toString()+","+visible.toString()+","+present.toString()+","
                file.appendText(line)
                //Log.d("X Values", i.x().toString());
            }
            file.appendText("END OF MEASUREMENT BLOCK"+"\n")

        }

//        // get path to external storage in Documents
//        var path_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
//        // create File
//        val file = File(path_name,"test_new.csv")
//
//        // get current time
//        val currentTime: Date = Calendar.getInstance().time;
//        // format date into specific format
//        val formatter = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss:SSS")
//        // format current time
//        val dateString = formatter.format(currentTime)
//
//        // add to csv file
//        file.appendText(dateString.toString()+",")
//        // iterate through normalized landmark values
//        for (i in calcValues)
//        {
//            var x_value = i.x() // get x
//            var y_value = i.y() // get y
//            var z_value = i.z() // get z
//            var visible = i.visibility() // get visibility
//            var present = i.presence() // get presence
//
//            // format and append to csv file
//            var line = x_value.toString()+","+y_value.toString()+","+z_value.toString()+","+visible.toString()+","+present.toString()+","
//            file.appendText(line)
//            //file.appendText("\n")
//        }
//        // add end of measurement text
//        file.appendText("END OF MEASUREMENT BLOCK"+"\n")
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}
