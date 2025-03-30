/*
 * Copyright (C) 2025 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.core.detection

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * Manages Tesseract OCR language data files.
 * 
 * Handles initialization, extraction, and management of Tesseract language data files needed for OCR.
 */
class TesseractManager private constructor(context: Context) {
    // Use application context to prevent memory leaks
    private val appContext = context.applicationContext
    
    companion object {
        private const val TAG = "TesseractManager"
        private const val TESSDATA_DIR = "tessdata"
        private const val LANGUAGE_ZIP_ASSET = "eng.traineddata.zip"
        private const val DEFAULT_LANGUAGE = "eng"
        
        // Use a lazy initialization approach instead of storing an instance in a static field
        @JvmStatic
        private val instance: TesseractManager by lazy {
            TesseractManager(InternalContextHolder.appContext)
        }
        
        /**
         * Get the singleton instance of the TesseractManager.
         * 
         * @param context application context used to access assets and files directory
         * @return the TesseractManager instance
         */
        fun getInstance(context: Context): TesseractManager {
            // Update the application context reference
            InternalContextHolder.appContext = context.applicationContext
            return instance
        }
        
        /**
         * Internal object to hold the application context reference
         * This prevents the memory leak by separating the context from the static instance
         */
        private object InternalContextHolder {
            lateinit var appContext: Context
        }
    }
    
    private val tessDataDir: File
        get() = File(appContext.filesDir, TESSDATA_DIR).apply { mkdirs() }
    
    /**
     * Initialize Tesseract OCR with the specified language.
     * If the language data files are not present, they will be extracted from assets.
     * 
     * @param detector the ImageDetector to initialize with OCR
     * @param language the language to use (e.g., "eng" for English)
     * @return true if initialization was successful, false otherwise
     */
    suspend fun initOcr(detector: ImageDetector, language: String = DEFAULT_LANGUAGE): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if the tessdata directory and language file exist
            val languageFile = File(tessDataDir, "$language.traineddata")
            
            if (!languageFile.exists()) {
                Log.d(TAG, "Language file not found, extracting from assets...")
                if (!extractLanguageData(language)) {
                    Log.e(TAG, "Failed to extract language data")
                    return@withContext false
                }
            }
            
            Log.d(TAG, "Initializing OCR with language: $language")
            return@withContext detector.initOcr(tessDataDir.absolutePath, language)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OCR", e)
            return@withContext false
        }
    }
    
    /**
     * Extract language data files from assets.
     * 
     * @param language the language code
     * @return true if extraction was successful, false otherwise
     */
    private fun extractLanguageData(language: String): Boolean {
        try {
            val zipAsset = if (language == DEFAULT_LANGUAGE) {
                LANGUAGE_ZIP_ASSET
            } else {
                "$language.traineddata.zip"
            }
            
            appContext.assets.open(zipAsset).use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var zipEntry = zipInputStream.nextEntry
                    
                    while (zipEntry != null) {
                        val fileName = zipEntry.name
                        val outputFile = File(tessDataDir, fileName)
                        
                        if (zipEntry.isDirectory) {
                            outputFile.mkdirs()
                        } else {
                            outputFile.parentFile?.mkdirs()
                            
                            FileOutputStream(outputFile).use { output ->
                                val buffer = ByteArray(4096)
                                var len: Int
                                while (zipInputStream.read(buffer).also { len = it } > 0) {
                                    output.write(buffer, 0, len)
                                }
                            }
                        }
                        
                        zipInputStream.closeEntry()
                        zipEntry = zipInputStream.nextEntry
                    }
                }
            }
            
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Error extracting language data", e)
            return false
        }
    }
    
    /**
     * Check if a specific language data file is available.
     * 
     * @param language the language code to check
     * @return true if the language data is available, false otherwise
     */
    fun isLanguageDataAvailable(language: String): Boolean {
        return File(tessDataDir, "$language.traineddata").exists()
    }
    
    /**
     * Get the path to the tessdata directory.
     * 
     * @return absolute path to the tessdata directory
     */
    fun getTessDataPath(): String {
        return tessDataDir.absolutePath
    }
}
