package com.poetic.card.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import kotlin.math.absoluteValue

@Composable
fun CardDetailScreen(
    imageUrl: String,
    onClose: () -> Unit
) {
    // 3D Tilt State
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    
    // Shine Effect State
    var shineOffset by remember { mutableStateOf(Offset.Zero) }

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)) // Dark backdrop
            .clickable(interactionSource = interactionSource, indication = null) { onClose() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        rotationX = 0f
                        rotationY = 0f
                        shineOffset = Offset.Zero
                    },
                    onDragCancel = {
                        rotationX = 0f
                        rotationY = 0f
                        shineOffset = Offset.Zero
                    }
                ) { change, dragAmount ->
                    change.consume()
                    
                    // Sensitivity factor
                    val sensitivity = 0.1f
                    
                    // Rotate based on drag
                    rotationY += dragAmount.x * sensitivity
                    rotationX -= dragAmount.y * sensitivity // Invert Y for natural feel
                    
                    // No limits on rotation as requested
                    // rotationY = rotationY.coerceIn(-20f, 20f)
                    // rotationX = rotationX.coerceIn(-20f, 20f)
                    
                    // Shine moves opposite to rotation to simulate reflection
                    val shineSensitivity = 5f
                    shineOffset += Offset(dragAmount.x * shineSensitivity, dragAmount.y * shineSensitivity)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Save Button (Using Share icon as Download might be missing in core)
        IconButton(
            onClick = {
                coroutineScope.launch {
                    saveImageToGallery(context, imageUrl)
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Save to Gallery", tint = Color.White)
        }

        // Close Button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        
        // Debug Text
        /*
        androidx.compose.material3.Text(
             text = imageUrl,
             color = Color.Red,
             modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
        )
        */
        
        val density = LocalDensity.current.density
        val elevationPx = with(LocalDensity.current) { 20.dp.toPx() }
        
        // The 3D Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Large but with padding
                .aspectRatio(0.7f)
                .clickable(interactionSource = interactionSource, indication = null) { /* Consume click */ }
                .graphicsLayer {
                    this.rotationX = rotationX
                    this.rotationY = rotationY
                    this.cameraDistance = 12f * density // Standard camera distance
                    this.shadowElevation = elevationPx
                    this.shape = RoundedCornerShape(24.dp)
                    this.clip = true
                }
                .background(Color.DarkGray, RoundedCornerShape(24.dp))
        ) {
            // Card Image
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl // Navigation decoded it already
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Shine Overlay
            // A gradient that moves based on tilt
            val shineBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.1f * (rotationX.absoluteValue + rotationY.absoluteValue).coerceIn(0f, 1f) + 0.05f),
                    Color.Transparent
                ),
                start = Offset.Zero + shineOffset,
                end = Offset(200f, 200f) + shineOffset
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(shineBrush)
            )
            
            // Specular Highlight for corners (subtle)
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = Offset(
                                x = if (rotationY > 0) 0f else Float.POSITIVE_INFINITY, // Light source logic
                                y = if (rotationX > 0) 0f else Float.POSITIVE_INFINITY
                            ),
                            radius = 500f
                        )
                    )
            )
        }
    }
}

suspend fun saveImageToGallery(context: Context, imageUrl: String) {
    try {
        withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Disable hardware bitmaps for saving
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

            if (bitmap != null) {
                val filename = "poetic_card_${System.currentTimeMillis()}.jpg"
                var fos: OutputStream? = null
                var uri: Uri? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PoeticCards")
                    }
                    uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = uri?.let { resolver.openOutputStream(it) }
                } else {
                    // For legacy versions, simplistic approach (scoped storage might require more handling)
                    // But assume targets API 29+ mostly or use simple external storage if permitted
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val image = java.io.File(imagesDir, filename)
                    fos = java.io.FileOutputStream(image)
                }

                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                 withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
