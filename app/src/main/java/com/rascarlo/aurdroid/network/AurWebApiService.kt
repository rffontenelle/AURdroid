package com.rascarlo.aurdroid.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.rascarlo.aurdroid.BuildConfig
import com.rascarlo.aurdroid.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

// okhttp logging interceptor
private val httpLoggingInterceptor = HttpLoggingInterceptor()
    .setLevel(
        when {
            BuildConfig.DEBUG -> {
                HttpLoggingInterceptor.Level.BODY
            }
            else -> {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    )

// okhttp client
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(httpLoggingInterceptor)
    .addNetworkInterceptor(object : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            // add api key, api secret and json format
            val httpUrl: HttpUrl = chain.request().url
                .newBuilder()
                .build()
            // construct the new request
            val request: Request = chain.request().newBuilder().url(httpUrl).build()
            // log the request
            Timber.d(request.toString())
            // return the new request
            return chain.proceed(request)
        }
    })
    .build()

// moshi
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// retrofit
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(Constants.BASE_URL)
    .client(okHttpClient)
    .build()

// api
object AurWebApi {
    val retrofitService: AurWebApiService by lazy {
        retrofit.create(AurWebApiService::class.java)
    }
}

// api service
interface AurWebApiService {

    /**
     * get packages
     * @param field: search by
     * @param keyword: package to search
     * @return [SearchResponse]]
     */
    @GET(value = "rpc/?v=5&type=search")
    suspend fun getPackages(
        @Query(value = "by") field: String,
        @Query(value = "arg") keyword: String
    ): SearchResponse

    /**
     * get info
     * @param name: package name
     * @return [InfoResponse]]
     */
    @GET(value = "rpc/?v=5&type=info")
    suspend fun getInfo(
        @Query(value = "arg") name: String
    ): InfoResponse
}