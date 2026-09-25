package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Base64
import com.example.data.model.AttachedMedia
import com.example.data.model.MediaType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MediaHelper {

    fun processUri(
        context: Context,
        uri: Uri,
        explicitType: MediaType,
        customDisplayName: String? = null
    ): AttachedMedia {
        var mime = context.contentResolver.getType(uri)
        if (mime == null || mime == "application/octet-stream") {
            mime = when (explicitType) {
                MediaType.IMAGE -> "image/jpeg"
                MediaType.VIDEO -> "video/mp4"
                MediaType.AUDIO -> "audio/mp3"
            }
        }

        val prefix = when (explicitType) {
            MediaType.IMAGE -> "image"
            MediaType.VIDEO -> "video"
            MediaType.AUDIO -> "audio"
        }
        val ext = when (explicitType) {
            MediaType.IMAGE -> ".jpg"
            MediaType.VIDEO -> ".mp4"
            MediaType.AUDIO -> if (mime.contains("wav")) ".wav" else ".mp3"
        }

        // Copy stream to cache file for reliable access
        val cachedFile = File(context.cacheDir, "cached_${prefix}_${System.currentTimeMillis()}$ext")
        var base64String: String? = null

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cachedFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (cachedFile.exists() && cachedFile.length() > 0) {
                val bytes = cachedFile.readBytes()
                if (bytes.size <= 12 * 1024 * 1024) {
                    base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val defaultName = when (explicitType) {
            MediaType.IMAGE -> "ছবি সংযুক্তি"
            MediaType.VIDEO -> "ভিডিও ক্লিপ"
            MediaType.AUDIO -> "অডিও ফাইল"
        }

        val finalUri = if (cachedFile.exists() && cachedFile.length() > 0) {
            Uri.fromFile(cachedFile).toString()
        } else {
            uri.toString()
        }

        return AttachedMedia(
            uri = finalUri,
            mediaType = explicitType,
            mimeType = mime,
            base64Data = base64String,
            displayName = customDisplayName ?: defaultName
        )
    }

    fun createSamplePhoto(context: Context): AttachedMedia {
        val file = File(context.cacheDir, "sample_photo.jpg")
        val bitmap = Bitmap.createBitmap(480, 360, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = android.graphics.Color.rgb(33, 43, 65)
        canvas.drawRect(0f, 0f, 480f, 360f, paint)

        // Glowing Moon
        paint.color = android.graphics.Color.rgb(255, 230, 180)
        canvas.drawCircle(240f, 130f, 45f, paint)

        // Labels
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 22f
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true
        canvas.drawText("নীরব অনুভূতির প্রতিচ্ছবি 🌙", 240f, 240f, paint)
        paint.textSize = 14f
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawText("Quiet Solitude & Emotion", 240f, 275f, paint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        val bytes = file.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        return AttachedMedia(
            uri = Uri.fromFile(file).toString(),
            mediaType = MediaType.IMAGE,
            mimeType = "image/jpeg",
            base64Data = base64,
            displayName = "নমুনা ছবি (Dusk Solitude)"
        )
    }

    fun createSampleAudio(context: Context): AttachedMedia {
        val file = File(context.cacheDir, "sample_audio.wav")
        val sampleRate = 8000
        val durationSec = 3
        val numSamples = sampleRate * durationSec
        val pcm = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = Math.sin(Math.PI * t / durationSec)
            val wave = Math.sin(2.0 * Math.PI * 432.0 * t) * envelope
            val sample = (wave * 12000).toInt().toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }

        FileOutputStream(file).use { fos ->
            writeWavHeader(fos, 1, sampleRate, 16, pcm.size)
            fos.write(pcm)
        }

        val bytes = file.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        return AttachedMedia(
            uri = Uri.fromFile(file).toString(),
            mediaType = MediaType.AUDIO,
            mimeType = "audio/wav",
            base64Data = base64,
            displayName = "নমুনা অডিও (কণ্ঠের সুর 🎵)"
        )
    }

    fun createSampleVideo(context: Context): AttachedMedia {
        val file = File(context.cacheDir, "sample_video_clip.mp4")
        if (!file.exists()) {
            val minimalMp4Header = byteArrayOf(
                0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70,
                0x6d, 0x70, 0x34, 0x32, 0x00, 0x00, 0x00, 0x00,
                0x69, 0x73, 0x6f, 0x6d, 0x6d, 0x70, 0x34, 0x32,
                0x00, 0x00, 0x00, 0x08, 0x66, 0x72, 0x65, 0x65
            )
            FileOutputStream(file).use { it.write(minimalMp4Header) }
        }

        val bytes = file.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        return AttachedMedia(
            uri = Uri.fromFile(file).toString(),
            mediaType = MediaType.VIDEO,
            mimeType = "video/mp4",
            base64Data = base64,
            displayName = "নমুনা ভিডিও ক্লিপ 🎥"
        )
    }

    private fun writeWavHeader(
        out: OutputStream,
        channels: Int,
        sampleRate: Int,
        bitsPerSample: Int,
        dataLength: Int
    ) {
        val totalLength = dataLength + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalLength and 0xff).toByte()
        header[5] = ((totalLength shr 8) and 0xff).toByte()
        header[6] = ((totalLength shr 16) and 0xff).toByte()
        header[7] = ((totalLength shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = blockAlign.toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (dataLength and 0xff).toByte()
        header[41] = ((dataLength shr 8) and 0xff).toByte()
        header[42] = ((dataLength shr 16) and 0xff).toByte()
        header[43] = ((dataLength shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }
}
