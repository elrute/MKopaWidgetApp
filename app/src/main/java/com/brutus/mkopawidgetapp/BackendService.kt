package com.brutus.mkopawidgetapp

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

//region Api interface

interface BackendApi {
    @POST("payments/initiate")
    suspend fun initiatePayment(
        @Body paymentRequest: PaymentRequest
    ): retrofit2.Response<PaymentResponse>

    @GET("loans/progress")
    suspend fun getLoanProgress(): LoanProgress?

    @POST("devices/updateToken")
    suspend fun updateDeviceToken(
        @Body tokenRequest: TokenRequest
    ): retrofit2.Response<TokenResponse>
}

//endregion

//region AuthInterceptor

val authInterceptor = Interceptor { chain ->
    val original = chain.request()
    val requestBuilder = original.newBuilder()
        .header("Authorization", "Bearer YOUR_ACCESS_TOKEN") //create your own Interceptor class refresh token with requests to a /token endpoint
    val request = requestBuilder.build()
    chain.proceed(request)
}

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .build()

//endregion


object BackendService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://localhost:3000")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: BackendApi by lazy {
        retrofit.create(BackendApi::class.java)
    }
}

data class PaymentRequest(
    val loanId: String,
    val amount: Double
)

data class PaymentResponse(
    val success: Boolean,
    val message: String
)

data class LoanProgress(
    val loanId: String,
    val totalAmount: Double,
    val amountPaid: Double,
    val dueDate: String
) {
    val progressPercentage: Int
        get() = ((amountPaid / totalAmount) * 100).toInt()
}

data class TokenRequest(
    val deviceToken: String
)

data class TokenResponse(
    val success: Boolean,
    val message: String
)

