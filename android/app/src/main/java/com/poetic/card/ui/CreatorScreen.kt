package com.poetic.card.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import androidx.compose.material3.ButtonDefaults
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.random.Random

data class OptimizationParams(
    val contrast: Float,
    val brightness: Float,
    val saturation: Float,
    val redShiftX: Float,
    val blueShiftX: Float,
    val noiseAlpha: Int,
    val flareX: Float,
    val flareY: Float,
    val flareRadius: Float,
    val isRetro: Boolean,
    val filmBurnAlpha: Int,
    val caColor1: Int,
    val caColor2: Int
) {
    companion object {
        fun random(): OptimizationParams {
             val colors = listOf(
                 android.graphics.Color.RED,
                 android.graphics.Color.BLUE,
                 android.graphics.Color.GREEN,
                 android.graphics.Color.MAGENTA
             )
             
             return OptimizationParams(
                contrast = Random.nextFloat() * 0.8f + 0.8f, // 0.8 - 1.6
                brightness = Random.nextFloat() * 60f - 10f, // -10 - 50
                saturation = Random.nextFloat() * 1.5f, // 0 - 1.5
                redShiftX = Random.nextFloat() * 0.04f - 0.02f, // +/- 2%
                blueShiftX = Random.nextFloat() * 0.04f - 0.02f,
                noiseAlpha = Random.nextInt(10, 50),
                flareX = Random.nextFloat(), 
                flareY = Random.nextFloat(),
                flareRadius = Random.nextFloat() * 0.5f + 0.1f,
                isRetro = Random.nextBoolean(),
                filmBurnAlpha = Random.nextInt(0, 150),
                caColor1 = colors.random(),
                caColor2 = colors.random()
            )
        }
    }
}

@Composable
fun CreatorScreen() {
    var cardText by remember { mutableStateOf("Your Poetry Here") }
    var backgroundUri by remember { mutableStateOf<Uri?>(null) }
    var priceDetail by remember { mutableStateOf("0.00") }
    
    // Convert isOptimized boolean to params nullability
    var optimParams by remember { mutableStateOf<OptimizationParams?>(null) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        backgroundUri = uri
        optimParams = null // Reset on new image
    }
    
    // Effect to regenerate bitmap when params change
    LaunchedEffect(optimParams, backgroundUri, cardText) {
        if (optimParams != null && backgroundUri != null) {
            isGenerating = true
            generatedBitmap = generateCardBitmap(context, backgroundUri!!, cardText, optimParams!!)
            isGenerating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Your Card",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Card Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f) // Standard card ratio
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (backgroundUri != null) {
                    if (optimParams != null && generatedBitmap != null) {
                         Image(
                            bitmap = generatedBitmap!!.asImageBitmap(),
                            contentDescription = "Optimized Card",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Live Preview Mode
                        Image(
                            painter = rememberAsyncImagePainter(backgroundUri),
                            contentDescription = "Card Background",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                         // Overlay Text (Only in normal mode, baked in optimized)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BasicTextField(
                                value = cardText,
                                onValueChange = { cardText = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black,
                                        offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                                        blurRadius = 8f
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        androidx.compose.foundation.layout.Row(
             horizontalArrangement = Arrangement.spacedBy(8.dp),
             modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Select BG")
            }
            
            Button(
                onClick = { 
                    optimParams = OptimizationParams.random() 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (optimParams != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(text = "Effects")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Text("Set Price: $$priceDetail")
        // Simple mock slider for price
        var priceSlider by remember { mutableStateOf(0f) }
        Slider(
            value = priceSlider,
            onValueChange = { 
                priceSlider = it 
                priceDetail = String.format("%.2f", it * 100)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { 
                    coroutineScope.launch {
                        val finalBitmap = if (optimParams != null) generatedBitmap else generateCardBitmap(context, backgroundUri ?: Uri.EMPTY, cardText, null)
                        if (finalBitmap != null) {
                            saveToGallery(context, finalBitmap)
                            uploadCardWithBitmap(context, finalBitmap, cardText, priceSlider, false)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
            
            Button(
                onClick = { 
                     coroutineScope.launch {
                        val finalBitmap = if (optimParams != null) generatedBitmap else generateCardBitmap(context, backgroundUri ?: Uri.EMPTY, cardText, null)
                        if (finalBitmap != null) {
                            saveToGallery(context, finalBitmap)
                            uploadCardWithBitmap(context, finalBitmap, cardText, priceSlider, true)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Mint & List")
            }
        }
    }
}

// Helper to generate the card bitmap with effects baked in
suspend fun generateCardBitmap(
    context: Context, 
    uri: Uri, 
    text: String, 
    params: OptimizationParams?
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        if (uri == Uri.EMPTY) return@withContext null
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close() ?: return@withContext null
        
        // 1. Crop to Aspect Ratio 0.7 (Portait)
        val targetRatio = 0.7f
        val currentRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        
        val cropWidth: Int
        val cropHeight: Int
        
        if (currentRatio > targetRatio) {
            // Image is wider than target. Crop width.
            cropHeight = originalBitmap.height
            cropWidth = (cropHeight * targetRatio).toInt()
        } else {
            // Image is taller than target. Crop height.
            cropWidth = originalBitmap.width
            cropHeight = (cropWidth / targetRatio).toInt()
        }
        
        // Ensure strictly non-zero
        if (cropWidth <= 0 || cropHeight <= 0) return@withContext null

        val cropX = (originalBitmap.width - cropWidth) / 2
        val cropY = (originalBitmap.height - cropHeight) / 2
        
        val croppedBitmap = Bitmap.createBitmap(originalBitmap, cropX, cropY, cropWidth, cropHeight)
        
        // Use cropped dimensions
        val width = croppedBitmap.width
        val height = croppedBitmap.height
        
        // Calculate dynamic text size
        // Target: fit within width - padding (e.g. 10% padding each side)
        val availableWidth = width * 0.8f
        // Start massive and scale down? Or just pick a size that makes the character count fit?
        // Let's iterate briefly to find a good size.
        // Base guess:
        var optimalTextSize = width * 0.1f // Start big
        val testPaint = TextPaint().apply {
            textSize = optimalTextSize
            isFakeBoldText = true
        }
        
        // Simple heuristic: If text is long, shrink it.
        // Or measure width of longest word?
        // Let's use standard StaticLayout measurement.
        while (optimalTextSize > width * 0.05f) { // Don't go too small
            testPaint.textSize = optimalTextSize
             val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, 0, text.length, testPaint, availableWidth.toInt())
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(text, testPaint, availableWidth.toInt(), android.text.Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
            }
            
            // Check if it fits nicely height-wise (e.g. not more than 50% of screen)
            // and width-wise (handled by layout width).
            // Main constraint usually: prevent single massive words from clipping?
            // StaticLayout breaks lines.
            
            // Check for massive overflow? No, just check if it looks "too big".
            // User said: "make it always fill the screen with a padding to the sides"
            // This suggests "make it wide".
            // So resizing to fill width is good.
            // But if text is short "Hi", it shouldn't be 500px tall.
            
            // Let's clamp max size.
            // If the text height > height * 0.6, shrink it.
            if (staticLayout.height > height * 0.6f) {
                optimalTextSize *= 0.9f
            } else {
               break // It fits height-wise.
            }
        }
        
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // Setup Paint for Text Outline and Fill
        val textPaintOutline = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = optimalTextSize
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
            style = Paint.Style.STROKE
            strokeWidth = optimalTextSize * 0.08f // Thicker outline
            // Intensified shadow: Larger radius, larger offset
            setShadowLayer(16f, 8f, 8f, android.graphics.Color.BLACK)
        }
        
        val textPaintFill = TextPaint().apply {
            color = android.graphics.Color.WHITE
            textSize = optimalTextSize
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
            // Shadow on fill too?
             setShadowLayer(16f, 8f, 8f, android.graphics.Color.BLACK)
        }

        // We want to apply effects to (Image + Text).
        // So first, draw Base Image + Text into a temporary bitmap?
        // Actually, easiest way to apply per-channel shift to Text AND Image is to:
        // 1. Draw Image
        // 2. Draw Text
        // 3. Repeat with offsets?
        
        // Better: Draw Image + Text into "SourceBitmap".
        // Then draw "SourceBitmap" 3 times onto "ResultBitmap" with color filters.
        
        val sourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val sourceCanvas = Canvas(sourceBitmap)
        
        // Draw Original Image
        sourceCanvas.drawBitmap(croppedBitmap, 0f, 0f, null)
        
        // Draw Text Centered
        val layoutWidth = availableWidth.toInt()
        val staticLayoutOutline = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaintOutline, layoutWidth)
                .setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, textPaintOutline, layoutWidth, android.text.Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)
        }
        
        val staticLayoutFill = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             StaticLayout.Builder.obtain(text, 0, text.length, textPaintFill, layoutWidth)
                .setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
                .build()
        } else {
             @Suppress("DEPRECATION")
             StaticLayout(text, textPaintFill, layoutWidth, android.text.Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)
        }
        
        sourceCanvas.save()
        // Center: layoutWidth is width*0.8. Margin is width*0.1.
        sourceCanvas.translate(width * 0.1f, height / 2f - staticLayoutFill.height / 2f)
        
        // Draw Outline then Fill
        staticLayoutOutline.draw(sourceCanvas)
        staticLayoutFill.draw(sourceCanvas)
        
        sourceCanvas.restore()
        
        // If no params, just return this source
        if (params == null) {
            return@withContext sourceBitmap
        }

        // Apply Effects to sourceBitmap -> resultBitmap
        
        // 1. Color Matrix
        val cm = ColorMatrix()
        cm.setSaturation(params.saturation)
        
        // Retro Effect: Septia-ish
        if (params.isRetro) {
             val sepia = ColorMatrix()
             sepia.setScale(1f, 0.95f, 0.82f, 1f) // Warm tint
             cm.postConcat(sepia)
        }

        val contrastMatrix = ColorMatrix(floatArrayOf(
            params.contrast, 0f, 0f, 0f, params.brightness,
            0f, params.contrast, 0f, 0f, params.brightness,
            0f, 0f, params.contrast, 0f, params.brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(contrastMatrix)
        
        val basePaint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        
        // 2. Chromatic Aberration (Random Colors)
        
        val caPaint1 = Paint().apply {
            alpha = 50 
             xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
             colorFilter = android.graphics.PorterDuffColorFilter(params.caColor1, PorterDuff.Mode.MULTIPLY)
        }
        val caPaint2 = Paint().apply {
            alpha = 50 
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
             colorFilter = android.graphics.PorterDuffColorFilter(params.caColor2, PorterDuff.Mode.MULTIPLY)
        }

        // Draw Full Image (Normal)
        canvas.drawColor(android.graphics.Color.BLACK) // Clear
        canvas.drawBitmap(sourceBitmap, 0f, 0f, basePaint) // Main Base
        
        // Add "Ghosts" for aberration
        canvas.drawBitmap(sourceBitmap, width * params.blueShiftX, 0f, caPaint1)
        canvas.drawBitmap(sourceBitmap, width * params.redShiftX, 0f, caPaint2)

        // 3. Noise
        val noisePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = params.noiseAlpha
            xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), noisePaint)

        // 4. Lens Flare
        val flarePaint = Paint().apply {
            val cx = params.flareX * width
            val cy = params.flareY * height
            val radius = params.flareRadius * width
            shader = RadialGradient(
                cx, cy, radius,
                intArrayOf(android.graphics.Color.argb(100, 255, 255, 200), android.graphics.Color.TRANSPARENT), 
                null, Shader.TileMode.CLAMP
            )
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flarePaint)

        // 5. Film Burn Effect
        if (params.filmBurnAlpha > 0) {
            val burnPaint = Paint().apply {
                // Random position for burn, usually edge
                val cx = if (Random.nextBoolean()) 0f else width.toFloat()
                val cy = Random.nextFloat() * height
                val radius = width * (Random.nextFloat() * 0.5f + 0.2f)
                
                shader = RadialGradient(
                    cx, cy, radius,
                    intArrayOf(
                        android.graphics.Color.argb(params.filmBurnAlpha, 255, 100, 0), // Orange/Red
                        android.graphics.Color.TRANSPARENT
                    ),
                    null, Shader.TileMode.CLAMP
                )
                xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD) // ADD creates a "burn" light leak look
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), burnPaint)
        }

        return@withContext resultBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}

fun saveToGallery(context: Context, bitmap: Bitmap) {
    val filename = "poetic_card_${System.currentTimeMillis()}.png"
    val fos: OutputStream?
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PoeticCards")
        }
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = imageUri?.let { resolver.openOutputStream(it) }
    } else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }
    
    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
    }
}

fun uploadCardWithBitmap(
    context: Context, 
    bitmap: Bitmap, 
    text: String, 
    price: Float, 
    isListed: Boolean
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val file = File(context.cacheDir, "upload_optimized.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
            val textPart = okhttp3.MultipartBody.Part.createFormData("text", text)
            val pricePart = okhttp3.MultipartBody.Part.createFormData("price", price.toString())
            val isListedPart = okhttp3.MultipartBody.Part.createFormData("isListed", isListed.toString())
            
            val api = com.poetic.card.network.NetworkModule.api
            api.uploadCard(textPart, pricePart, isListedPart, body)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
