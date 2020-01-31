package com.ddyos.flipper.mmkv.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btn_add.setOnClickListener {
            MMKV.mmkvWithID("other_mmkv").encode("time", System.currentTimeMillis())
        }
    }

}
