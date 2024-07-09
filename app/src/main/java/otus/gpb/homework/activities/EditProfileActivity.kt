package otus.gpb.homework.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditProfileActivity : AppCompatActivity() {

    private lateinit var buttonEditProfile: Button
    private lateinit var imageView: ImageView
    private lateinit var textViewName: TextView
    private lateinit var textViewSurname: TextView
    private lateinit var textViewAge: TextView
    private lateinit var fillFormLauncher: ActivityResultLauncher<Intent>
    private var localImageUri: Uri? = null
    private var isCameraPermissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        imageView = findViewById(R.id.imageview_photo)
        textViewName = findViewById(R.id.textview_name)
        textViewSurname = findViewById(R.id.textview_surname)
        textViewAge = findViewById(R.id.textview_age)
        buttonEditProfile = findViewById(R.id.button4)

        imageView.setOnClickListener {
            val alertItems = arrayOf(
                resources.getString(R.string.take_photo),
                resources.getString(R.string.choose_photo)
            )
            MaterialAlertDialogBuilder(this).setItems(alertItems) { _, index: Int ->
                when (index) {
                    0 -> takePhoto()
                    1 -> choosePhoto()
                }
            }.show()
        }

        buttonEditProfile.setOnClickListener {
            launchFillFormForActivityResult()
        }

        fillFormLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val data = result.data
                    val name = data?.getStringExtra("name")
                    val surname = data?.getStringExtra("surname")
                    val age = data?.getStringExtra("age")
                    name?.let { textViewName.text = it }
                    surname?.let { textViewSurname.text = it }
                    age?.let { textViewAge.text = it }
                }
            }

        findViewById<Toolbar>(R.id.toolbar).apply {
            inflateMenu(R.menu.menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.send_item -> {
                        openSenderApp()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) setPhoto()
        }

    private val localImageStorage =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                populateImage(it)
                localImageUri = it
            }
        }

    private fun launchFillFormForActivityResult() {
        val intent = Intent(this, FillFormActivity::class.java)
        fillFormLauncher.launch(intent)
    }

    private fun choosePhoto() {
        localImageStorage.launch("image/*")
    }

    private fun setPhoto() {
        imageView.setImageResource(R.drawable.cat)
        localImageUri = null
    }

    private fun takePhoto() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> {
                setPhoto()
            }

            PackageManager.PERMISSION_DENIED -> {
                if (!isCameraPermissionRequested) {
                    isCameraPermissionRequested = true
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showRationaleDialog()
                } else showSettingsDialog()
            }
        }
    }

    private fun showRationaleDialog() {
        MaterialAlertDialogBuilder(this).setMessage(R.string.camera_permission_rationale)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.give_permission) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }.show()
    }

    private fun showSettingsDialog() {
        MaterialAlertDialogBuilder(this).setNeutralButton(R.string.open_settings) { _, _ ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
            )
        }.show()
    }

    /**
     * Используйте этот метод чтобы отобразить картинку полученную из медиатеки в ImageView
     */
    private fun populateImage(uri: Uri) {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        imageView.setImageBitmap(bitmap)
    }

    private fun openSenderApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            setPackage("org.telegram.messenger")
            putExtra(
                Intent.EXTRA_TEXT,
                "${textViewName.text}\n${textViewSurname.text}\n${textViewAge.text}"
            )
            putExtra(Intent.EXTRA_STREAM, localImageUri)
        }
        startActivity(intent)
    }
}