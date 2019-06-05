/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package  nchoro.alisa.shineh.activity

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

import jp.co.cyberagent.android.gpuimage.util.Rotation
import nchoro.alisa.shineh.GPUImageFilterTools
import nchoro.alisa.shineh.R
import nchoro.alisa.shineh.utils.Camera1Loader
import nchoro.alisa.shineh.utils.Camera2Loader
import nchoro.alisa.shineh.utils.CameraLoader
import nchoro.alisa.shineh.utils.doOnLayout

class CameraActivity : AppCompatActivity() {

    private val gpuImageView: GPUImageView by lazy { findViewById<GPUImageView>(R.id.surfaceView) }
    private val seekBar: SeekBar by lazy { findViewById<SeekBar>(R.id.seekBar) }
    private val cameraLoader: CameraLoader by lazy {
        if (Build.VERSION.SDK_INT < 21) {
            Camera1Loader(this)
        } else {
            Camera2Loader(this)
        }
    }
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                filterAdjuster?.adjust(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        findViewById<View>(R.id.button_choose_filter).setOnClickListener {
            GPUImageFilterTools.showDialog(this) { filter -> switchFilterTo(filter) }
        }
        findViewById<View>(R.id.button_capture).setOnClickListener {
            saveSnapshot()
        }
        findViewById<View>(R.id.img_switch_camera).run {
            if (!cameraLoader.hasMultipleCamera()) {
                visibility = View.GONE
            }
            setOnClickListener {
                cameraLoader.switchCamera()
                gpuImageView.setRotation(getRotation(cameraLoader.getCameraOrientation()))
            }
        }
        cameraLoader.setOnPreviewFrameListener { data, width, height ->
            gpuImageView.updatePreviewFrame(data, width, height)
        }
        gpuImageView.setRotation(getRotation(cameraLoader.getCameraOrientation()))
        gpuImageView.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY)
    }

    override fun onResume() {
        super.onResume()
        gpuImageView.doOnLayout {
            cameraLoader.onResume(it.width, it.height)
        }
    }

    override fun onPause() {
        cameraLoader.onPause()
        super.onPause()
    }

    private fun saveSnapshot() {
        val folderName = "GPUImage"
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        gpuImageView.saveToPictures(folderName, fileName) {
            Toast.makeText(this, "$folderName/$fileName saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRotation(orientation: Int): Rotation {
        return when (orientation) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
    }

    private fun switchFilterTo(filter: GPUImageFilter) {
        if (gpuImageView.filter == null || gpuImageView.filter!!.javaClass != filter.javaClass) {
            gpuImageView.filter = filter
            filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter)
            filterAdjuster?.adjust(seekBar.progress)
        }
    }
}
