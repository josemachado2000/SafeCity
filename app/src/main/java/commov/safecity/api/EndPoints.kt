package commov.safecity.api

import retrofit2.Call
import retrofit2.http.*

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

    @FormUrlEncoded
    @POST("anomaly")
    fun insertAnomaly(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("lat") lat: Double,
        @Field("lng") lng: Double,
        @Field("photo") photo: String?,
        @Field("user_id") userID: Int,
        @Field("type_id") typeID: Int
    ): Call<Anomaly>

    @GET("anomaly/{id}")
    fun deleteAnomalyById(
        @Path("id") id: Int
    ): Call<Anomaly>
}