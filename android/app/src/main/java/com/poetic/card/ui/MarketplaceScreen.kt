package com.poetic.card.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.zIndex
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.poetic.card.network.NetworkModule
import com.poetic.card.model.Card

data class MarketItem(
    val id: String,
    val text: String,
    val imageUrl: String,
    val price: String,
    val owner: String,
    val isListed: Boolean = true,
)

// Mock Data
val mockItems = listOf(
    MarketItem("1", "Roses are red", "https://picsum.photos/200/300", "10.00", "Alice"),
    MarketItem("2", "Violets are blue", "https://picsum.photos/200/301", "15.50", "Bob"),
    MarketItem("3", "Native is cool", "https://picsum.photos/200/302", "100.00", "Charlie"),
    MarketItem("4", "Compose is sweet", "https://picsum.photos/200/303", "5.00", "Dave"),
)

@Composable
fun MarketplaceScreen(
    onCardClick: (String) -> Unit = {},
    onBuyClick: (String) -> Unit = {}
) {
    var cards by remember { mutableStateOf<List<MarketItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val fetchedCards = NetworkModule.api.getCards()
            cards = fetchedCards.map { card ->
                MarketItem(
                    id = card.id,
                    text = card.text,
                    // Use 10.0.2.2 or local IP if running on emulator/device
                    imageUrl = if (card.backgroundUrl.startsWith("/")) "${NetworkModule.BASE_URL}${card.backgroundUrl.removePrefix("/")}" else card.backgroundUrl,
                    price = card.price.toString(),
                    owner = card.owner?.username ?: "Unknown",
                    isListed = card.isListed,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Marketplace",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Text("Loading...", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards) { item ->
                    MarketCard(item, onCardClick, onBuyClick)
                }
            }
        }
    }
}

@Composable
fun MarketCard(
    item: MarketItem, 
    onClick: (String) -> Unit,
    onBuyClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable { onClick(item.imageUrl) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient overlay at bottom only for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                 // Removed Text Item Overlay
                 
                Text(
                    text = "$${item.price}",
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "by ${item.owner}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
            
            // Buy Button
            Button(
                onClick = { onBuyClick(item.id) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .zIndex(1f), // Ensure it's above the card click
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Buy", fontSize = 12.sp)
            }
        }
    }
}
