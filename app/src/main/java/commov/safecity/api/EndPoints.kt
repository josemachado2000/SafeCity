package commov.safecity.api

import retrofit2.Call
import retrofit2.http.*

interface EndPoints {
    @GET("anomalies")
    fun getAnomalies(): Call<List<Anomaly>>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginPostResponse>
}