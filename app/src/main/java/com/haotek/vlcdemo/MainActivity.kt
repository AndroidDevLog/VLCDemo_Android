package com.haotek.vlcdemo

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class MainActivity : AppCompatActivity() {
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoLayout: VLCVideoLayout
    private lateinit var playButton: Button
    
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
    
    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        mediaPlayer.detachViews()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        libVLC.release()
    }
}