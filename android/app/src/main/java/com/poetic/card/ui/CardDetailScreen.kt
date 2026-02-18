package com.poetic.card.ui

import android.net.Uri
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
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

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

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
