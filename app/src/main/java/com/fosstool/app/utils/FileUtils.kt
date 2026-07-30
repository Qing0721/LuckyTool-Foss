package com.fosstool.app.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.fosstool.app.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

@Suppress("unused", "MemberVisibilityCanBePrivate")
object FileUtils {
    fun getDocumentPath(context: Context, uri: Uri): String? {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) return "null"
        if (!DocumentsContract.isDocumentUri(context, uri)) return "null"
        val authority = when (uri.authority) {
            "com.android.externalstorage.documents" -> "ExternalStorageDocument"
            "com.android.providers.downloads.documents" -> "DownloadsDocument"
            "com.android.providers.media.documents" -> "MediaDocument"
            else -> "null"
        }
        when (authority) {
            "ExternalStorageDocument" -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val docArray = docId.split(":")
                val type = docArray[0]
                val dir = docArray[1]
                if ("primary" != type) return "null"
                return Environment.getExternalStorageDirectory().path + "/" + dir
            }

            "DownloadsDocument" -> {
                val docId = DocumentsContract.getDocumentId(uri)
                if (TextUtils.isEmpty(docId)) return "null"
                return if (docId.startsWith("raw:")) {
                    docId.replaceFirst("raw:", "")
                } else if (docId.contains("msf:")) {
                    getMSFFile(context, uri)
                } else {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), ContentUris.parseId(uri)
                    )
                    getDataColumn(context, contentUri, null, null)
                }
            }

            "MediaDocument" -> {
                val docId = DocumentsContract.getDocumentId(uri)
                val docArray = docId.split(":")
                val contentUri: Uri? = when (docArray[0]) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> null
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(docArray[1])
                return getDataColumn(context, contentUri!!, selection, selectionArgs)
            }
        }
        return "null"
    }

    fun getDataColumn(
        context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getMSFFile(context: Context, uri: Uri): String? {
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/LuckyTool/cache")
        if (!dir.exists()) dir.mkdirs()
        File(dir.path + "/.nomedia").createNewFile()
        val fileType = context.contentResolver.getType(uri)?.split("/")?.get(1)
        val fileName = SystemClock.uptimeMillis().toString() + "." + fileType
        val file = File(dir, fileName)
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) return copyStreamToFile(inputStream, file)
        return null
    }

    fun copyStreamToFile(inputStream: InputStream, outputFile: File): String {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024)
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
        return outputFile.path
    }

    fun readFromFile(file: File): String? {
        var fis: FileInputStream? = null
        var output: String? = null
        try {
            fis = FileInputStream(file)
            val buffer = ByteArray(4096)
            var len: Int
            val sb = StringBuilder()
            while (fis.read(buffer).also { len = it } != -1) {
                sb.append(String(buffer, 0, len))
            }
            output = sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fis?.close()
        }
        return output
    }

    fun readFromUri(context: Context, uri: Uri): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    fun shareFile(context: Context, title: String, file: File) {
        if (file.exists()) {
            val share = Intent(Intent.ACTION_SEND)
            val contentUri = FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".FileProvider", file
            )
            share.putExtra(Intent.EXTRA_STREAM, contentUri)
            share.type = "application/vnd.android.package-archive"
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(share, title))
        }
    }
}
