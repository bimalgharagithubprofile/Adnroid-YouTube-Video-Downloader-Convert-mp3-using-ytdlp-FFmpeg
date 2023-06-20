package com.bimalghara.mp3downloader.data.network.ytdlp

import android.content.Context
import android.util.Log
import com.bimalghara.mp3downloader.R
import com.bimalghara.mp3downloader.data.error.CustomException
import com.bimalghara.mp3downloader.data.model.YtDlpDTO
import com.bimalghara.mp3downloader.utils.*
import com.bimalghara.mp3downloader.utils.ZipUtils.unzip
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

object YTDLP {
    private val logTag = javaClass.simpleName

    private var pythonPath: File? = null
    private var ytdlpPath: File? = null
    private var binDir: File? = null

    private var ENV_LD_LIBRARY_PATH: String? = null
    private var ENV_SSL_CERT_FILE: String? = null
    private var ENV_PYTHONHOME: String? = null



    fun execute(appContext: Context, url:String, callback: ((Float, Long, String) -> Unit)? = null): YtDlpDTO {

        val baseDir = File(appContext.noBackupFilesDir, DIRECTORY_BASE)
        if (!baseDir.exists()) baseDir.mkdir()

        binDir = File(appContext.applicationInfo.nativeLibraryDir)

        val packagesDir = File(baseDir, PACKAGE_ROOT)
        pythonPath = File(binDir, PYTHON_BINARY)
        val pythonDir = File(packagesDir, PYTHON_DIRECTORY)

        val ytdlpDir = File(baseDir, YTDLP_DIRECTORY)
        ytdlpPath = File(ytdlpDir, YTDLP_BINARY)

        ENV_LD_LIBRARY_PATH = pythonDir.absolutePath + "/usr/lib"
        ENV_SSL_CERT_FILE = pythonDir.absolutePath + "/usr/etc/tls/cert.pem"
        ENV_PYTHONHOME = pythonDir.absolutePath + "/usr"

        initPython(appContext, pythonDir)
        init_ytdlp(appContext, ytdlpDir)

        val ytDlpResponse: YtDlpDTO
        val process: Process
        val exitCode: Int
        val outBuffer = StringBuffer() //stdout
        val errBuffer = StringBuffer() //stderr
        val startTime = System.currentTimeMillis()
        val args: List<String> = listOf("-f", "best", "--dump-json", "--no-cache-dir", url)
        val command: MutableList<String?> = ArrayList()
        command.addAll(listOf(pythonPath!!.absolutePath, ytdlpPath!!.absolutePath))
        command.addAll(args)

        val processBuilder = ProcessBuilder(command)
        processBuilder.environment().apply {
            this["LD_LIBRARY_PATH"] = ENV_LD_LIBRARY_PATH
            this["SSL_CERT_FILE"] = ENV_SSL_CERT_FILE
            this["PATH"] = System.getenv("PATH") + ":" + binDir!!.absolutePath
            this["PYTHONHOME"] = ENV_PYTHONHOME
            this["HOME"] = ENV_PYTHONHOME
        }

        process = try {
            processBuilder.start()
        } catch (e: IOException) {
            Log.e(logTag, "processBuilder.start IOException: ${e.localizedMessage}")
            throw CustomException(cause = "processBuilder.start IOException: ${e.localizedMessage}")
        }
        Log.e(logTag, "process: $process")

        val outStream = process.inputStream
        val errStream = process.errorStream
        val stdOutProcessor = StreamProcessExtractor(outBuffer, outStream, callback)
        val stdErrProcessor = StreamGobbler(errBuffer, errStream)
        exitCode = try {
            stdOutProcessor.join()
            stdErrProcessor.join()
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            Log.e(logTag, "exit InterruptedException: ${e.localizedMessage}")
            throw CustomException(cause = "exit InterruptedException: ${e.localizedMessage}")
        }
        val out = outBuffer.toString()
        val err = errBuffer.toString()
        if (exitCode > 0) {
            Log.e(logTag, "exitCode=$exitCode: $err")
            throw CustomException(cause = "exitCode=$exitCode :: $err")
        }

        val elapsedTime = System.currentTimeMillis() - startTime
        ytDlpResponse = YtDlpDTO(command, exitCode, elapsedTime, out, err)
        Log.e(logTag, "youtubeDLResponse out: ${ytDlpResponse.out}")

        return ytDlpResponse
    }

    private fun initPython(appContext: Context, pythonDir: File) {
        val pythonLib = File(binDir, PYTHON_LIBRARY)
        // using size of lib as version
        if (!pythonDir.exists()) {
            FileUtils.deleteQuietly(pythonDir)
            pythonDir.mkdirs()
            try {
                unzip(pythonLib, pythonDir)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(pythonDir)
                Log.e(logTag, "failed to initialize python " + e.localizedMessage)
                throw CustomException("failed to initialize python " + e.localizedMessage)
            }
        }
    }

    private fun init_ytdlp(appContext: Context, ytdlpDir: File) {
        if (!ytdlpDir.exists()) ytdlpDir.mkdirs()
        val ytdlpBinary = File(ytdlpDir, YTDLP_BINARY)
        if (!ytdlpBinary.exists()) {
            try {
                val inputStream =
                    appContext.resources.openRawResource(R.raw.ytdlp) /* will be renamed to yt-dlp */
                FileUtils.copyInputStreamToFile(inputStream, ytdlpBinary)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(ytdlpDir)
                Log.e(logTag, "failed to initialize ytdpl " + e.localizedMessage)
                throw CustomException(cause = "failed to initialize ytdpl " + e.localizedMessage)
            }
        }
    }
}