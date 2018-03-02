package com.example.superman.imageloaderdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.imageloader.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val path = "http://img0.imgtn.bdimg.com/it/u=2097996470,2706206864&fm=27&gp=0.jpg"
    var imageLoader = ImageLoader(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun loadPic(view: View) {
        imageLoader.disPlay(path, imageView = imageView)
    }
}
