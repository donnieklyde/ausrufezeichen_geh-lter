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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberAsyncImagePainter
import com.poetic.card.model.UpdateCardRequest
import com.poetic.card.network.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    onCardClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var cards by remember { mutableStateOf<List<MarketItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val fetchedCards = NetworkModule.api.getMyCards()
                        withContext(Dispatchers.Main) {
                            cards = fetchedCards.map { card ->
                                MarketItem(
                                    id = card.id,
                                    text = card.text,
                                    imageUrl = if (card.backgroundUrl.startsWith("/")) "${NetworkModule.BASE_URL}${card.backgroundUrl.removePrefix("/")}" else card.backgroundUrl,
                                    price = card.price.toString(),
                                    owner = card.owner?.username ?: "You",
                                    isListed = card.isListed
                                )
                            }
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                             isLoading = false
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Collection",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Logout")
        }

        if (isLoading) {
            Text("Loading...", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (cards.isEmpty()) {
             Text("You haven't created any cards yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards) { item ->
                    ProfileCard(
                        item = item, 
                        onList = {
                            // On List Click
                             CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    NetworkModule.api.updateCard(item.id, UpdateCardRequest(isListed = true))
                                    // Refresh
                                    val fetched = NetworkModule.api.getMyCards()
                                    withContext(Dispatchers.Main) {
                                        cards = fetched.map { card ->
                                            MarketItem(
                                                id = card.id,
                                                text = card.text,
                                                imageUrl = if (card.backgroundUrl.startsWith("/")) "${NetworkModule.BASE_URL}${card.backgroundUrl.removePrefix("/")}" else card.backgroundUrl,
                                                price = card.price.toString(),
                                                owner = card.owner?.username ?: "You",
                                                isListed = card.isListed,
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        onClick = { onCardClick(item.imageUrl) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(item: MarketItem, onList: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable { onClick() },
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
            
            // Bottom Gradient for readability
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
                // Text removed as it is in the image
                
                Text(
                    text = "$${item.price}",
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                
                if (item.isListed) {
                     Text(
                        text = "Listed",
                        color = Color.Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Button(
                        onClick = onList,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("List for Sale", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
