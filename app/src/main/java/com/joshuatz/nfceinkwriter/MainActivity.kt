package com.joshuatz.nfceinkwriter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.askjeffreyliu.floydsteinbergdithering.Utils
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.listeners.IPickResult
import com.vansuita.pickimage.enums.EPickType

class MainActivity : AppCompatActivity(), IPickResult {
    private var mPreferencesController: Preferences? = null
    private var mHasReFlashableImage: Boolean = false
    private val mReFlashButton: CardView get() = findViewById(R.id.reflashButton)
    private var mSharedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register action bar / toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // Get user preferences
        mPreferencesController = Preferences(this)
        updateScreenSizeDisplay(null)

        // Setup screen size changer
        val screenSizeChangeInvite: Button = findViewById(R.id.changeDisplaySizeInvite)
        screenSizeChangeInvite.setOnClickListener {
            mPreferencesController!!.showScreenSizePicker(fun(updated: String): Void? {
                updateScreenSizeDisplay(updated)
                return null
            })
        }

        // Check for previously generated image, enable re-flash button if available
        checkReFlashAbility()

        mReFlashButton.setOnClickListener {
            if (mHasReFlashableImage) {
                val navIntent = Intent(this, NfcFlasher::class.java)
                startActivity(navIntent)
            } else {
                val toast = Toast.makeText(this, "There is no image to re-flash!", Toast.LENGTH_SHORT)
                toast.show()
            }
        }


        // Setup exact-size image file picker
        val imageFilePickerExactCTA: Button = findViewById(R.id.cta_image_exact)
        imageFilePickerExactCTA.setOnClickListener {
            val (sw, sh) = mPreferencesController!!.getScreenSizePixels()
            val setup = PickSetup()
                .setTitle("Select $sw x $sh image")
                .setMaxSize(1000) // downscales large images to avoid wasting mem on them
                .setPickTypes(EPickType.GALLERY)
                .setSystemDialog(true)
            PickImageDialog.build(setup).show(this)
        }

        // Setup image file/photo picker with crop/processing
        val imageFilePickerProcCTA: Button = findViewById(R.id.cta_image_proc)
        imageFilePickerProcCTA.setOnClickListener {
            val (sw, sh) = mPreferencesController!!.getScreenSizePixels()
            CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(sw, sh)
                .setRequestedSize(sw, sh, CropImageView.RequestSizeOptions.RESIZE_EXACT)
                .start(this)
        }

        // Setup WYSIWYG button click
        val wysiwygEditButtonInvite: Button = findViewById(R.id.cta_new_graphic)
        wysiwygEditButtonInvite.setOnClickListener {
            val intent = Intent(this, WysiwygEditor::class.java)
            startActivity(intent)
        }

        // Setup text button click
        val textEditButtonInvite: Button = findViewById(R.id.cta_new_text)
        textEditButtonInvite.setOnClickListener {
            val intent = Intent(this, TextEditor::class.java)
            startActivity(intent)
        }

        // Set image uri if launched from another app
        mSharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Set image uri if launched from another app
        mSharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
    }

    override fun onResume() {
        super.onResume()
        checkReFlashAbility()
        if (mSharedImageUri == null) return
        val (sw, sh) = mPreferencesController!!.getScreenSizePixels()
        val uri = mSharedImageUri
        mSharedImageUri = null
        CropImage
            .activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(sw, sh)
            .setRequestedSize(sw, sh, CropImageView.RequestSizeOptions.RESIZE_EXACT)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode != CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) return
        val result = CropImage.getActivityResult(resultData)
        var error: String? = null
        var bitmap: Bitmap? = null
        if (resultCode == Activity.RESULT_OK) {
            bitmap = result?.getBitmap(this)
            if (bitmap != null) bitmap = Utils().floydSteinbergDitheringBWR(bitmap)
            if (bitmap == null) error = "result not available"
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            error = result!!.error.toString()
        } else return
        if (error != null) {
            Toast.makeText(this, "Crop image failure: $error", Toast.LENGTH_LONG).show()
            return
        }
        flashBitmap(bitmap!!)
    }

    override fun onPickResult(result: PickResult) {
        if (result.error != null) {
            Toast.makeText(this, result.error.toString(), Toast.LENGTH_LONG).show()
            return
        }
        val bitmap = result.bitmap
        val (sw, sh) = mPreferencesController!!.getScreenSizePixels()
        val (iw, ih) = bitmap.width to bitmap.height
        if (iw != sw || ih != sh) {
            Toast.makeText(this, "Image size ($iw x $ih) does not match"
              + " screen size exactly ($sw x $sh)", Toast.LENGTH_LONG).show()
            return
        }
        flashBitmap(bitmap!!)
    }

    private fun flashBitmap(bitmap: Bitmap) {
        openFileOutput(GeneratedImageFilename, Context.MODE_PRIVATE).use { fileOutStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
            fileOutStream.close()
            val navIntent = Intent(this, NfcFlasher::class.java)
            startActivity(navIntent)
        }
    }

    private fun updateScreenSizeDisplay(updated: String?) {
        var screenSizeStr = updated
        if (screenSizeStr == null) {
            screenSizeStr = mPreferencesController!!.getPreferences()
                .getString(Constants.PreferenceKeys.DisplaySize, DefaultScreenSize)
        }
        findViewById<TextView>(R.id.currentDisplaySize).text = screenSizeStr ?: DefaultScreenSize
    }

    private fun checkReFlashAbility() {
        val lastGeneratedFile = getFileStreamPath(GeneratedImageFilename)
        val reFlashImagePreview: ImageView = findViewById(R.id.reflashButtonImage)
        if (lastGeneratedFile.exists()) {
            mHasReFlashableImage = true
            // Need to set null first, or else Android will cache previous image
            reFlashImagePreview.setImageURI(null)
            reFlashImagePreview.setImageURI(Uri.fromFile((lastGeneratedFile)))
        } else {
            // Grey out button
            mReFlashButton.setCardBackgroundColor(Color.DKGRAY)
            val drawableImg = resources.getDrawable(android.R.drawable.stat_sys_warning, null)
            reFlashImagePreview.setImageDrawable(drawableImg)
        }
    }

}
