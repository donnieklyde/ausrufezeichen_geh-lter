package com.poetic.card

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.poetic.card.ui.CreatorScreen
import com.poetic.card.ui.MarketplaceScreen
import com.poetic.card.ui.ProfileScreen
import com.poetic.card.ui.theme.PoeticCardMarketTheme

@Composable
fun MainApp() {
    PoeticCardMarketTheme {
        val navController = rememberNavController()
        val startDestination = if (com.poetic.card.network.TokenManager.getToken() != null) "market" else "login"
        
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route
                
                if (currentDestination != "login") {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Market") },
                            label = { Text("Market") },
                            selected = currentDestination == "market",
                            onClick = {
                                navController.navigate("market") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Create, contentDescription = "Create") },
                            label = { Text("Create") },
                            selected = currentDestination == "create",
                            onClick = {
                                navController.navigate("create") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentDestination == "profile",
                            onClick = {
                                navController.navigate("profile") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") { 
                    com.poetic.card.ui.LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("market") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    ) 
                }
                composable("market") { 
                    MarketplaceScreen(
                        onCardClick = { url ->
                            val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                            navController.navigate("detail/$encodedUrl")
                        }
                    ) 
                }
                composable("create") { CreatorScreen() }
                composable("profile") { 
                    ProfileScreen(
                         onCardClick = { url ->
                            val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                            navController.navigate("detail/$encodedUrl")
                        }
                    ) 
                }
                
                composable("detail/{imageUrl}") { backStackEntry ->
                    val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                    com.poetic.card.ui.CardDetailScreen(
                        imageUrl = imageUrl,
                        onClose = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
