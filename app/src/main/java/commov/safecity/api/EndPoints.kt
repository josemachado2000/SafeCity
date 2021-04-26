package commov.safecity.api

import android.content.res.Resources
import commov.safecity.R
import retrofit2.Call
import retrofit2.http.*
import java.io.File

interface EndPoints {
    @GET("anomalies")
    fun getAnomalies(): Call<List<Anomaly>>

    @GET("types")
    fun getTypes(): Call<List<Type>>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginPostResponse>
}