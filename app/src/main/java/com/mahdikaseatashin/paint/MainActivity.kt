package com.mahdikaseatashin.paint

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ramotion.fluidslider.FluidSlider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import me.priyesh.chroma.ChromaDialog
import me.priyesh.chroma.ColorMode
import me.priyesh.chroma.ColorSelectListener
import java.io.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {


    private var selectedColor: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        selectedColor = linear_layout_colors[4] as ImageView

        selectedColor!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_selected)
        )

        image_btn_image_picker.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissionLoadImage()
            }
        }
        image_btn_save.setOnClickListener {
            saveImage()
        }

        image_btn_undo.setOnClickListener {
            drawing_view.undo()
        }

        btnColorSelected.setOnClickListener {
            customDialog()
        }

        drawing_view.setBrushSize(1.toFloat())
        image_btn_brush_size_definer.setOnClickListener {
            brushSizeChanger()
        }
    }

    private fun saveImage() {
        checkPermissionSaveImage()
    }

    private fun checkPermissionSaveImage() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0?.areAllPermissionsGranted() == true) {
                        BitmapAsyncTask(getBitmapFromView(frame_layout_draw)).execute()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check();
    }

    private fun getBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissionLoadImage() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_DENIED
        ) {
            //permission denied
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            //show popup to request runtime permission
            requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            //permission already granted
            loadImage()
        }
    }


    private inner class BitmapAsyncTask(val mBitmap: Bitmap) : AsyncTask<Any, Void, String>() {
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Toast.makeText(this@MainActivity, "file saved successfully", Toast.LENGTH_SHORT).show()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String
            try {
                val bytes = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                val f =
                    File(externalCacheDir!!.absoluteFile.toString() + File.separator + "Paint_" + System.currentTimeMillis() / 1000 + ".png")
                val fos = FileOutputStream(f)
                fos.write(bytes.toByteArray())
                fos.close()
                result = f.absolutePath
            } catch (e: Exception) {
                result = ""
                e.printStackTrace()
            }
            return result
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    loadImage()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_CODE) {
            image_view_background.setImageURI(data?.data)
        }
    }

    //TODO (complete color selector implementation)
    private fun customDialog() {

        val chromaDialog = ChromaDialog.Builder()
            .initialColor(Color.GREEN)
            .colorMode(ColorMode.RGB) // There's also ARGB and HSV
            .onColorSelected(
                object : ColorSelectListener {
                    override fun onColorSelected(color: Int) {
                        drawing_view.setBrushColor(color)
                        selectedColor!!.setImageDrawable(
                            ContextCompat.getDrawable(this@MainActivity, R.drawable.palette_normal)
                        )
                    }
                }
            )
            .create()
            .show(supportFragmentManager, "ChromaDialog")

    }

    private fun brushSizeChanger() {

        val brushSizeDialog = Dialog(this)
        brushSizeDialog.setContentView(R.layout.dialog_brush_size)
        brushSizeDialog.setTitle("Brush Size")
        val slider = brushSizeDialog.fluidSlider
        brushSizeDialog.show()
        slider.position = drawing_view.getBrushSize() * 3 / 100
        Log.e("TAG", "brushSizeChanger: ${drawing_view.getBrushSize()}")
        val btnSelect = brushSizeDialog.btn_brush_size_select
        val btnCancel = brushSizeDialog.btn_brush_size_cancel
        btnSelect.setOnClickListener {
            drawing_view.setBrushSize(slider.position * 12)
            brushSizeDialog.dismiss()
        }
        btnCancel.setOnClickListener {
            brushSizeDialog.dismiss()
        }
//        slider.positionListener = {
//            Log.e("TAG", "brushSizeChanger: $it", )
//        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onColorChange(view: View) {
        if (view != selectedColor) {
            (view as ImageButton).setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_selected)
            )
            selectedColor!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_normal)
            )
            drawing_view.setBrushColor(view.tag.toString())
            selectedColor = view
        }

    }

    companion object {
        private const val READ_EXTERNAL_STORAGE_CODE = 1
        private const val WRITE_EXTERNAL_STORAGE_CODE = 2
        private const val PICK_IMAGE_CODE = 101
    }
}
