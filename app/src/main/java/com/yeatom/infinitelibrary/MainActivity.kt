package com.yeatom.infinitelibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val urls = listOf(
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.shuicaimi.com%2Fc2021%2F09%2F07%2F5qabtvf5wf0.jpg&refer=http%3A%2F%2Fimg.shuicaimi.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1665867033&t=af7cd3a1435a60a2a461daf9747fa42d",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F0a81d2b8b5af38dcb1215ed7385cc416309a04ae39375-jGTWF8_fw658&refer=http%3A%2F%2Fhbimg.b0.upaiyun.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1665867089&t=2269aa8afa0961c4de52cd48cf5f9e6e",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201807%2F10%2F20180710171842_bqxxs.jpg&refer=http%3A%2F%2Fb-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1665867121&t=52922b01e9d073f33cfc02d0e52ed09b",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Finews.gtimg.com%2Fnewsapp_bt%2F0%2F13611980053%2F1000&refer=http%3A%2F%2Finews.gtimg.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1666891670&t=cd29695a557cf16f4ffeb2effc28070a",
            "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Farticle%2Fd12f7bd789e692aa5772ac938f9aa906ce495f74.jpg&refer=http%3A%2F%2Fi0.hdslb.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1666891727&t=509a5140b0ed47d829dd23a762b9c8ca",
        )

        findViewById<InfiniteLibrary>(R.id.infinite_library).display(urls)
    }
}