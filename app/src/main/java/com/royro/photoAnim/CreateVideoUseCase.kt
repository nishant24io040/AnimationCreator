package com.royro.photoAnim

import android.content.Context
import android.graphics.*
import android.media.*
import android.net.Uri
import android.provider.MediaStore
import java.io.File

class CreateVideoUseCase {

    fun execute(context: Context, imageUris: List<Uri>, durationSec: Int): File {
        val width = 720
        val height = 1280
        val frameRate = 30
        val totalFrames = durationSec * frameRate
        val framesPerImage = totalFrames / imageUris.size
        val bitRate = 2_000_000

        val outputFile = File(context.getExternalFilesDir(null), "video_${System.currentTimeMillis()}.mp4")
        val format = MediaFormat.createVideoFormat("video/avc", width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType("video/avc").apply {
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }

        val surface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        val cr = context.contentResolver
        val bitmaps = imageUris.map {
            val bmp = MediaStore.Images.Media.getBitmap(cr, it)
            Bitmap.createScaledBitmap(bmp, width, height, true)
        }

        val bufferInfo = MediaCodec.BufferInfo()
        var muxerStarted = false
        var trackIndex = -1
        var presentationTimeUs: Long = 0
        val durationPerFrameUs = 1_000_000L / frameRate
        val paint = Paint().apply { isAntiAlias = true }

        for (i in bitmaps.indices) {
            val current = bitmaps[i]
            val next = if (i + 1 < bitmaps.size) bitmaps[i + 1] else current

            for (f in 0 until framesPerImage) {
                val alpha = f.toFloat() / framesPerImage
                val blended = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(blended)

                paint.alpha = ((1 - alpha) * 255).toInt()
                canvas.drawBitmap(current, 0f, 0f, paint)
                paint.alpha = (alpha * 255).toInt()
                canvas.drawBitmap(next, 0f, 0f, paint)

                val inputCanvas = surface.lockCanvas(null)
                inputCanvas.drawBitmap(blended, 0f, 0f, null)
                surface.unlockCanvasAndPost(inputCanvas)

                presentationTimeUs += durationPerFrameUs

                val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outputIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputIndex) ?: continue

                    if (!muxerStarted) {
                        trackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }

                    bufferInfo.presentationTimeUs = presentationTimeUs
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    encoder.releaseOutputBuffer(outputIndex, false)
                }
            }
        }

        encoder.signalEndOfInputStream()

        var encoderDone = false
        while (!encoderDone) {
            val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
            if (outputIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outputIndex) ?: continue
                if (bufferInfo.size != 0) {
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                encoder.releaseOutputBuffer(outputIndex, false)

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    encoderDone = true
                }
            }
        }

        muxer.stop()
        muxer.release()
        encoder.stop()
        encoder.release()

        return outputFile
    }
}
