package com.translate.translator.voice.translation.dictionary.all.language.di

import com.translate.translator.voice.translation.dictionary.all.language.api.WebService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import retrofit2.converter.moshi.MoshiConverterFactory


@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    /**
     * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
     * full Kotlin compatibility.
     */
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun provideOkhttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient.Builder = OkHttpClient.Builder()
        client.connectTimeout(30L, TimeUnit.SECONDS) // connect timeout
            .writeTimeout(30L, TimeUnit.SECONDS) // write timeout
            .readTimeout(30L, TimeUnit.SECONDS)
        client.addInterceptor(logging)
        return client.build()
    }

    @Provides
    @Singleton
    fun provideRetrofitService(): WebService {
        return Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(provideOkhttpClient())
            .build()
            .create(WebService::class.java)
    }
}