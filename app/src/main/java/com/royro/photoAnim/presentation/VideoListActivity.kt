package com.royro.photoAnim.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.royro.photoAnim.R
import java.io.File

class VideoListActivity : AppCompatActivity() {

    private val viewModel: VideoListViewModel by viewModels()
    private lateinit var adapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_list)

        val recyclerView = findViewById<RecyclerView>(R.id.videoRecyclerView)
        adapter = VideoListAdapter { file -> openVideo(file) }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.videos.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.loadVideos()
    }

    private fun openVideo(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Open video with"))
    }
}
