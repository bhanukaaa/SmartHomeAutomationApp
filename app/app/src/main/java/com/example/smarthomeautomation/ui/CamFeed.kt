package com.example.smarthomeautomation.ui

import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CamFeed(
    modifier: Modifier = Modifier,
    cameraName: String = "CAM_01",
    streamUrl: String = "https://live.143b.ch/cam/flux/ts:abr.m3u8"
) {
    val context = LocalContext.current
    var isLive by remember { mutableStateOf(false) }
    var timeStamp by remember { mutableStateOf("") }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        while (true) {
            timeStamp = formatter.format(Date())
            delay(1000)
        }
    }

    DisposableEffect(streamUrl) {
        onDispose {
            mediaPlayer?.run {
                try {
                    if (isPlaying) stop()
                    reset()
                    release()
                } catch (_: Exception) {
                }
            }
            mediaPlayer = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            val surface = Surface(surfaceTexture)
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setSurface(surface)
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                                )
                                setOnErrorListener { _, _, _ ->
                                    isLive = false
                                    true
                                }
                                setOnPreparedListener { mp ->
                                    mp.setVolume(0f, 0f)
                                    isLive = true
                                    mp.start()
                                }
                                try {
                                    setDataSource(context, Uri.parse(streamUrl))
                                    prepareAsync()
                                } catch (_: Exception) {
                                    isLive = false
                                }
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                        }

                        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                            mediaPlayer?.run {
                                try {
                                    if (isPlaying) stop()
                                    reset()
                                    release()
                                } catch (_: Exception) {
                                }
                            }
                            mediaPlayer = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isLive) Color.Green else Color.Red, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLive) "LIVE" else "CONNECTING",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = cameraName,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Text(
            text = timeStamp,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}
