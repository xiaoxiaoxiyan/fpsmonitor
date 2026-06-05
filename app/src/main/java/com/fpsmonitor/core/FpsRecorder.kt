package com.fpsmonitor.core

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FpsRecorder - Manages FPS recording sessions and data export.
 *
 * Reference: TakoStats Records feature, Scene's frame rate recording.
 * Provides session management and CSV export capabilities.
 */
object FpsRecorder {

    private val sessions = mutableListOf<RecordingSession>()

    /**
     * Save a completed recording session.
     */
    fun saveSession(session: RecordingSession) {
        sessions.add(0, session) // Most recent first
    }

    /**
     * Get all saved sessions.
     */
    fun getSessions(): List<RecordingSession> = sessions.toList()

    /**
     * Get a specific session by ID.
     */
    fun getSession(id: Long): RecordingSession? = sessions.find { it.id == id }

    /**
     * Delete a session.
     */
    fun deleteSession(id: Long) {
        sessions.removeAll { it.id == id }
    }

    /**
     * Export a session to CSV file.
     * Format: timestamp, fps
     * Reference: TakoStats and Scene both support data export.
     */
    suspend fun exportToCsv(context: Context, session: RecordingSession): File? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "fps_records")
            if (!dir.exists()) dir.mkdirs()

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "fps_record_${dateFormat.format(Date(session.startTime))}.csv"
            val file = File(dir, fileName)

            FileWriter(file).use { writer ->
                writer.write("timestamp,fps\n")
                session.records.forEach { record ->
                    writer.write("${record.timestamp},${record.fps}\n")
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}