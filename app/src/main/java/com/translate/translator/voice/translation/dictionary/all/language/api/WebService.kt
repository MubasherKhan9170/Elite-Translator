package com.translate.translator.voice.translation.dictionary.all.language.api

import androidx.annotation.NonNull
import com.translate.translator.voice.translation.dictionary.all.language.data.DictionaryItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface WebService {
    // get product details endpoint
    @NonNull
    @GET("/api/v2/entries/en/{word}")
    fun getWordDetail(@Path("word") word: String): Call<List<DictionaryItem?>>


}