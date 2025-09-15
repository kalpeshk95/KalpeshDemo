package com.example.kalpeshdemo.di

import com.example.kalpeshdemo.BuildConfig
import com.example.kalpeshdemo.data.source.Network
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

val retrofitModule = module {

    single<Network> {
        getNetwork(get())
    }

    single<Retrofit> {
        getRetrofit(get())
    }

    single<OkHttpClient> {
        getOkHttpClient(get())
    }

    single<HttpLoggingInterceptor> {
        getHttpLoggingInterceptor()
    }
}

private fun getNetwork(retroFit: Retrofit): Network {

    return retroFit.create(Network::class.java)
}

fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {

    // Create Retrofit instance
    val json = Json { ignoreUnknownKeys = true } // Configure Json instance as needed

    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}

fun getOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .build()
}

fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    return httpLoggingInterceptor
}
