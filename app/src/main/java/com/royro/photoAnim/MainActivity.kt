package com.royro.photoAnim

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.royro.photoAnim.presentation.VideoListActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: VideoViewModel by viewModels()
    private lateinit var progressBar: ProgressBar
    private lateinit var previewButton : Button
    private lateinit var savedVideosListBtn : Button
    private lateinit var durationInputEtv: EditText
    private var selectedUris = listOf<Uri>()

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) selectedUris = uris
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectImages: Button = findViewById(R.id.selectImages)
        val startRendering: Button = findViewById(R.id.startRendering)
        previewButton = findViewById(R.id.previewVideoBtn)
        progressBar = findViewById(R.id.progressBar)
        durationInputEtv = findViewById(R.id.videoDurationInput)
        savedVideosListBtn = findViewById(R.id.savedVideosBtn)

        selectImages.setOnClickListener { pickImages.launch("image/*") }

        startRendering.setOnClickListener {
            val duration = durationInputEtv.text.toString().toIntOrNull() ?: 5
            if (selectedUris.isNotEmpty()) {
                viewModel.createVideo(selectedUris, duration)
            } else {
                Toast.makeText(this, "Select images first", Toast.LENGTH_SHORT).show()
            }
        }

        previewButton.setOnClickListener {
            viewModel.videoFile.value?.let { file ->
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/mp4")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Play video with"))
            }
        }
        savedVideosListBtn.setOnClickListener{
            startActivity(Intent(this, VideoListActivity::class.java))

        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            previewButton.visibility = if (!loading && viewModel.videoFile.value != null) View.VISIBLE else View.GONE
        }
    }
}
