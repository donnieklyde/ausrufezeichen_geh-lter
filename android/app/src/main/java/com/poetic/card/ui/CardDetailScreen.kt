package com.poetic.card.ui

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
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

    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    
    // Permission launcher for Android < 10
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            downloadImage(context, imageUrl)
        } else {
            Toast.makeText(context, "Permission needed to save image", Toast.LENGTH_SHORT).show()
        }
    }

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
                    
                    // Shine moves opposite to rotation to simulate reflection
                    val shineSensitivity = 5f
                    shineOffset += Offset(dragAmount.x * shineSensitivity, dragAmount.y * shineSensitivity)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Save Button
        IconButton(
            onClick = {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    downloadImage(context, imageUrl)
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
                    model = imageUrl
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Shine Overlay
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
            
            // Specular Highlight
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
                                x = if (rotationY > 0) 0f else Float.POSITIVE_INFINITY,
                                y = if (rotationX > 0) 0f else Float.POSITIVE_INFINITY
                            ),
                            radius = 500f
                        )
                    )
            )
        }
    }
}

fun downloadImage(context: Context, imageUrl: String) {
    Toast.makeText(context, "Saving image...", Toast.LENGTH_SHORT).show()
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()

            val result = context.imageLoader.execute(request)
            
            if (result is coil.request.SuccessResult) {
                val bitmap = result.drawable.toBitmap()
                saveBitmapToGallery(context, bitmap)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                }
            } else if (result is coil.request.ErrorResult) {
                val errorMsg = result.throwable.message ?: "Unknown Coil error"
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load image: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load image for saving", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "card_${System.currentTimeMillis()}.jpg"
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PoeticCards")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to create MediaStore entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                throw Exception("Failed to compress bitmap")
            }
        } ?: throw Exception("Failed to open output stream")

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
    } else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val poeticDir = java.io.File(imagesDir, "PoeticCards")
        if (!poeticDir.exists()) {
            poeticDir.mkdirs()
        }
        val imageFile = java.io.File(poeticDir, filename)
        val fos = java.io.FileOutputStream(imageFile)
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
            fos.close()
            throw Exception("Failed to compress bitmap")
        }
        fos.flush()
        fos.close()

        // Inform the media scanner
        android.media.MediaScannerConnection.scanFile(
            context,
            arrayOf(imageFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )
    }
}
