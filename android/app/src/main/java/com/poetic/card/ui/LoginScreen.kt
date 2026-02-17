package com.poetic.card.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.poetic.card.model.AuthRequest
import com.poetic.card.network.NetworkModule
import com.poetic.card.network.TokenManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    Log.d("LoginScreen", "Sign in button clicked")
                    try {
                        val credentialManager = CredentialManager.create(context)
                        Log.d("LoginScreen", "CredentialManager created")
                        
                        // NOTE: Replace with    // Updated Client ID from User
                        val webClientId = "29909492289-hvs6o03l1kc1q42busue4oe4gjfat34s.apps.googleusercontent.com"

                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(webClientId)
                            .setAutoSelectEnabled(false) // Force chooser to ensure UI shows
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        Log.d("LoginScreen", "Requesting credential...")
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        Log.d("LoginScreen", "Credential received")

                        val credential = result.credential
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            Log.d("LoginScreen", "ID Token obtained: ${idToken.take(10)}...")
                            
                            // Authenticate with Backend
                            try {
                                val response = NetworkModule.api.googleLogin(AuthRequest(idToken))
                                TokenManager.saveToken(response.token)
                                Log.d("LoginScreen", "Backend login successful")
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Backend Auth Failed", e)
                                Toast.makeText(context, "Authentication Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e("LoginScreen", "Unexpected credential type: ${credential.type}")
                            Toast.makeText(context, "Unexpected credential type", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                        Log.e("LoginScreen", "No credentials available", e)
                        Toast.makeText(context, "No Google Account found. Please sign in to your device settings.", Toast.LENGTH_LONG).show()
                    } catch (e: GetCredentialException) {
                        Log.e("LoginScreen", "GetCredential failed", e)
                        Toast.makeText(context, "Sign In Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Login failed (General)", e)
                        Toast.makeText(context, "Login Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Text("Sign in with Google")
            }
        }
    }
}
