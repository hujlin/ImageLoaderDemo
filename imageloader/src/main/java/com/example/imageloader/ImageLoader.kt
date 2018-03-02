package com.example.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by superman on 2018/3/2.
 */
class ImageLoader(context: Context) {

    private val mContext: Context = context

    companion object {
        lateinit var lruCache: LruCache<String, Bitmap>
        lateinit var mThreadPool: ExecutorService
        var handler = Handler()
    }

    init {
        val maxSize = (Runtime.getRuntime().freeMemory() / 4).toInt()
        lruCache = object : LruCache<String, Bitmap>(maxSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.rowBytes * value.height
            }
        }

        mThreadPool = Executors.newFixedThreadPool(3)
    }

    fun disPlay(url: String, imageView: ImageView) {
        //从内存获取
        var bitmap: Bitmap? = loadBitmapFromCache(url)
        if (bitmap != null) {
            Log("从内存获取")
            imageView.setImageBitmap(bitmap)
            return
        }
        //从磁盘获取
        bitmap = loadBitmapFromLocal(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            return
        }
        //从网络获取
        getDataFromNet(url, imageView)

    }

    //从内存中加载
    private fun loadBitmapFromCache(url: String): Bitmap? {
        return lruCache.get(url)
    }


    //从磁盘加载
    private fun loadBitmapFromLocal(url: String): Bitmap? {
        var name = MD5Utils.hashKeyForDisk(url)
        val file = File(getCacheDir(), name)
        if (file.exists()) {
            var bitmap: Bitmap = BitmapFactory.decodeStream(file.inputStream())
            Log("从磁盘获取===width=${bitmap.width}==height==${bitmap.height}")
            // 存储到内存
            putBitmapToCache(url, bitmap)

            return bitmap
        }
        return null
    }


    //获取本地存放图片文件夹
    private fun getCacheDir(): String {

        val state = Environment.getExternalStorageState()
        val file: File
        file = if (state == (Environment.MEDIA_MOUNTED)) {
            File(Environment.getExternalStorageDirectory(), "imageCache")
        } else {
            File(mContext.cacheDir, "/imageCache");
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    //从网络获取
    private fun getDataFromNet(url: String, imageView: ImageView) {
        mThreadPool.execute(ImageLoadTask(url, object : DownloadFinishListener {
            override fun callBack(bitmap: Bitmap) {
                handler.post { imageView.setImageBitmap(bitmap) }
            }
        }))
    }

    interface DownloadFinishListener {
        fun callBack(bitmap: Bitmap)
    }

    //从网络下载
    inner class ImageLoadTask(url: String, downloadFinish: DownloadFinishListener) : Runnable {
        private var mPath = url
        private var downloadFinishListener = downloadFinish

        override fun run() {
            val httpUrlConnection: HttpURLConnection = URL(mPath).openConnection() as HttpURLConnection
            httpUrlConnection.requestMethod = "GET"
            httpUrlConnection.connectTimeout = 30 * 1000
            httpUrlConnection.readTimeout = 30 * 1000
            httpUrlConnection.connect()
            var code = httpUrlConnection.responseCode
            if (code == 200) {
                var input = httpUrlConnection.inputStream
                //将流转成bitmap
                var bitmap = BitmapFactory.decodeStream(input)
                downloadFinishListener.callBack(bitmap)
                Log("网络下载")
                //存储到本地
                putBitmapToDisk(url = mPath, bitmap = bitmap)
                //存储到内存
                putBitmapToCache(url = mPath, bitmap = bitmap)
            }
        }

    }

    //保存到磁盘
    private fun putBitmapToDisk(url: String, bitmap: Bitmap) {
        var fos: FileOutputStream? = null
        try {
            var name = MD5Utils.hashKeyForDisk(url)
            Log("存到磁盘name =$name")
            var file = File(getCacheDir(), name)
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
        } finally {
            if (fos != null) {
                fos.close()
                fos = null
            }
        }
    }

    //加载到内存
    private fun putBitmapToCache(url: String, bitmap: Bitmap) {
        Log("存到内存url =$url bitmap =${bitmap == null}")
        lruCache.put(url, bitmap)
        Log(lruCache.size().toString())
    }

    fun ImageLoader.Log(content: String) {
        Log.e("hujlin", content)
    }
}