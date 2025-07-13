package com.royro.photoAnim.domain

import android.content.Context
import java.io.File

class GetSavedVideosUseCase {
    fun execute(context: Context): List<File> {
        val dir = context.getExternalFilesDir(null) ?: return emptyList()
        return dir.listFiles()?.filter { it.extension == "mp4" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
