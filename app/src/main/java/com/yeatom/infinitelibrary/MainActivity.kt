package com.yeatom.infinitelibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.drawee.backends.pipeline.Fresco

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fresco.initialize(this)

        setContentView(R.layout.activity_main)
        findViewById<InfiniteLibrary>(R.id.infiniteLibrary).loop(
            listOf(
                "https://img1.baidu.com/it/u=3953506535,1844632112&fm=253&fmt=auto&app=138&f=JPEG?w=600&h=240",
                "https://pics5.baidu.com/feed/0bd162d9f2d3572c057f85c7c3eafd2263d0c30e.jpeg?token=15720a6eec4ed4f401fb1f044c3a6d97&s=03E25723583233A518388B9C0300C0A1",
                "https://img2.baidu.com/it/u=1194202626,1107644830&fm=253&fmt=auto&app=138&f=JPEG?w=506&h=338",
                "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.mp.sohu.com%2Fupload%2F20170617%2F9c42d371b25d4145afb1808042e6a5cb_th.png&refer=http%3A%2F%2Fimg.mp.sohu.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1671034264&t=453695a1cb65a267e7c1c93551a8ecbd",
            )
        )
    }
}