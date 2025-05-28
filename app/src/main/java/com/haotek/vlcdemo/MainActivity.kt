package com.haotek.vlcdemo

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class MainActivity : AppCompatActivity() {
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoLayout: VLCVideoLayout
    private lateinit var playButton: Button
    private lateinit var fullscreenButton: ImageButton

    private var isFullscreen = false
    private val rtspUrl = "rtsp://192.168.1.254/xxx.mov"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initVLC()
        setupClickListeners()
    }

    private fun initViews() {
        videoLayout = findViewById(R.id.video_layout)
        playButton = findViewById(R.id.button_play)
        fullscreenButton = findViewById(R.id.button_fullscreen)
    }

    private fun initVLC() {
        val options = arrayListOf<String>()
        options.add("--aout=opensles")
        options.add("--audio-time-stretch")
        options.add("-vvv")

        libVLC = LibVLC(this, options)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.attachViews(videoLayout, null, false, false)
    }

    private fun setupClickListeners() {
        playButton.setOnClickListener {
            playRTSPStream()
        }

        fullscreenButton.setOnClickListener {
            toggleFullscreen()
        }
    }

    private fun playRTSPStream() {
        try {
            val media = Media(libVLC, Uri.parse(rtspUrl))
            mediaPlayer.media = media
            media.release()
            mediaPlayer.play()
            playButton.text = "正在播放..."
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleFullscreen() {
        if (isFullscreen) {
            exitFullscreen()
        } else {
            enterFullscreen()
        }
        isFullscreen = !isFullscreen
    }

    private fun enterFullscreen() {
        // 隐藏ActionBar标题栏
        supportActionBar?.hide()

        // 先分离视图
        mediaPlayer.detachViews()

        // 设置横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // 隐藏系统UI
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 延迟重新绑定视图，确保布局完成
        videoLayout.post {
            mediaPlayer.attachViews(videoLayout, null, false, false)
        }
    }

    private fun exitFullscreen() {
        // 显示ActionBar标题栏
        supportActionBar?.show()

        // 先分离视图
        mediaPlayer.detachViews()

        // 恢复竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 显示系统UI
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

        // 取消屏幕常亮
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 延迟重新绑定视图，确保布局完成
        videoLayout.post {
            mediaPlayer.attachViews(videoLayout, null, false, false)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 配置变更由fullscreen方法处理，这里不需要额外的视图操作
        // 避免与enterFullscreen/exitFullscreen中的视图管理产生冲突
    }

    override fun onPause() {
        super.onPause()
        // 不要在onPause中停止播放，避免全屏切换时中断
    }

    override fun onStop() {
        super.onStop()
        // 只有在Activity真正停止时才停止播放
        if (isFinishing) {
            mediaPlayer.stop()
            mediaPlayer.detachViews()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        libVLC.release()
    }
}