package com.poetic.card.network

import com.poetic.card.model.Card
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Body
import com.poetic.card.model.UpdateCardRequest
import com.poetic.card.model.AuthRequest
import com.poetic.card.model.AuthResponse

interface ApiService {
    @GET("/api/cards?mode=market")
    suspend fun getCards(): List<Card>

    @GET("/api/cards?mode=my")
    suspend fun getMyCards(): List<Card>

    @Multipart
    @POST("/api/cards")
    suspend fun uploadCard(
        @Part text: MultipartBody.Part,
        @Part price: MultipartBody.Part,
        @Part isListed: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): Card

    @PATCH("/api/cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body body: UpdateCardRequest
    ): Card

    @POST("/api/auth/google")
    suspend fun googleLogin(@Body body: AuthRequest): AuthResponse
}

object NetworkModule {
    // For Emulator: 10.0.2.2 points to host localhost
    // For Physical Device: Use local IP of computer (e.g. 192.168.1.x)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
            val token = TokenManager.getToken()
            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
