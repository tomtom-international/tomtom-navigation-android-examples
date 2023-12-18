/*
 * © 2023 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

package com.example.usecase.assets

import android.content.Context
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object OnboardMapAssetsExtractor {
    private const val MAP_ZIP_ASSET = "test-map/map.zip"
    private const val KEY_STORE_ASSET = "test-map/keystore.zip"

    /**
     * Extracting the onboard map from assets, if the target location already exists then skip.
     * NOTE: The caller should handle the Exception
     * NOTE: This function should not be executed in the main thread
     * @param context Android Context
     * @param targetMapDir the target location of the map
     * @param targetKeystorePath the target location the keystore file
     * @param forceExtraction if false, skip the extraction if the target location exists, otherwise
     * clean the target location and then do extraction
     *
     */
    fun extractMapAssets(
        context: Context,
        targetMapDir: File,
        targetKeystorePath: File,
        forceExtraction: Boolean
    ) {
        if (forceExtraction) {
            deleteFileOrDirectory(targetMapDir)
            deleteFileOrDirectory(targetKeystorePath)
        }

        if (!targetMapDir.exists()) {
            context.assets.open(MAP_ZIP_ASSET).also {
                unzipInputStream(it, targetMapDir)
            }
        }

        if (!targetKeystorePath.exists()) {
            context.assets.open(KEY_STORE_ASSET).also {
                unzipInputStream(it, targetKeystorePath.parentFile!!)
            }
        }
    }

    private fun unzipInputStream(stream: InputStream, targetDir: File) {
        ZipInputStream(stream).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                zipInputStream.extractToDirectory(entry, targetDir)
                entry = zipInputStream.nextEntry
            }
            zipInputStream.closeEntry()
        }
    }

    private fun deleteFileOrDirectory(target: File) {
        if (!target.exists()) {
            return
        }
        if (target.isFile) {
            target.delete()
        } else if (target.isDirectory) {
            target.deleteRecursively()
        }
    }

    private fun ZipInputStream.extractToDirectory(entry: ZipEntry, dir: File) {
        val file = File(dir, entry.name)
        if (entry.isDirectory) {
            file.mkdirs()
        } else {
            file.parentFile!!.mkdirs()
            file.outputStream().use {
                this.copyTo(it)
            }
        }
    }
}
