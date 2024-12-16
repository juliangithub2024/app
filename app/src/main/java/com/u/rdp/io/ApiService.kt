package com.u.rdp.io

import com.u.rdp.model.Item
import com.u.rdp.model.Shop
import com.u.rdp.util.Constants
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {


    @GET(/* value = */ "item/{cadena}/")  // V. 126, m. 1:20
    abstract fun loadItemss(@Path(/* value = */ "cadena") cadena: String):
            Call<ArrayList<Item>>

    @GET(/* value = */ "shop/{cadena}/")  // V. 126, m. 1:20
    abstract fun loadShop(@Path(/* value = */ "cadena") cadena: String): Call<Shop>

    companion object Factory {  // V. 125, m 1:08                       // 192.168.100.14           casa
        // private const val BASE_URL ="http://127.0.0.1:8000/api/";    // marte: 192.168.1.137
        //private const val BASE_URL ="http://192.168.100.14:8000/api/";  // paseo mendez: 192.168.0.106,
        private const val BASE_URL = Constants.API_BASE_URL;

        fun create(): ApiService{
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }



}