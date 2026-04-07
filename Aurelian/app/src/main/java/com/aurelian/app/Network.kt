package com.aurelian.app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Response wrappers based on our API_DOCUMENTATION.md
data class FeedResponse(
    val data: List<User>,
    val nextCursor: String?
)
// Response wrappers based on our API_DOCUMENTATION.md
data class LoginRequest(val email: String, val code: String)
data class LoginData(val token: String, val isNewUser: Boolean)
data class LoginResponse(val data: LoginData)
data class VerifyInviteRequest(val inviteCode: String)
data class VerifyInviteData(val valid: Boolean, val referrerId: String, val newToken: String?)
data class VerifyInviteResponse(val data: VerifyInviteData)
data class BiometricRequest(val deviceId: String, val signature: String)
data class BaseResponse(val success: Boolean)
data class ProfileData(val id: String, val name: String, val membership: String, val isVerified: Boolean)
data class ProfileResponse(val data: ProfileData)
data class UpdateProfileRequest(val bio: String)
data class PreferencesRequest(val stealthMode: Boolean, val minAge: Int)
data class SubmitAssetsRequest(val documentUrls: List<String>)
data class SubmitAssetsResponse(val status: String)
data class LikeRequest(val targetUserId: String)
data class LikeData(val matched: Boolean, val matchId: String?)
data class LikeResponse(val data: LikeData)
data class PassRequest(val targetUserId: String)
data class Match(val matchId: String, val user: User)
data class MatchesResponse(val data: List<Match>)
data class AdmirersResponse(val data: List<Admirer>)
data class EventsListResponse(val data: List<EventResponse>)
data class ConversationsListResponse(val data: List<Conversation>)



data class Admirer(val userId: String, val isBlurred: Boolean)
data class Conversation(val convId: String, val lastMessage: String, val unreadCount: Int)
data class SendMessageRequest(val convId: String, val content: String)
data class MessagesListResponse(val data: List<Message>)

data class SendMessageData(val msgId: String, val timestamp: Long)
data class SendMessageResponse(val data: SendMessageData)
data class SendInviteRequest(val targetUserId: String, val type: String, val location: String, val time: String, val message: String)
data class SendInviteResponse(val inviteId: String, val status: String)
data class RespondInviteRequest(val action: String)
data class RespondInviteData(val inviteId: String, val status: String)
data class RespondInviteResponse(val data: RespondInviteData)
data class EventResponse(val eventId: String, val title: String, val date: String)
data class EventDetailsData(val title: String, val date: String, val location: String, val description: String, val attireProtocol: String, val coverUrl: String)
data class EventDetailsResponse(val data: EventDetailsData)
data class RsvpRequest(val partySize: Int)
data class RsvpData(val status: String)
data class RsvpResponse(val data: RsvpData)
data class MasqueradeStatusResponse(val isOpen: Boolean, val endTime: Long, val question: String)
data class SubmitAnswerRequest(val answer: String)
data class SubmitAnswerData(val status: String)
data class SubmitAnswerResponse(val data: SubmitAnswerData)
data class MasqueradeResponseWrapper(val data: MasqueradeStatusResponse)

data class ReferralsStatusResponse(val inviteCode: String, val remaining: Int)
data class ReferralsResponseWrapper(val data: ReferralsStatusResponse)

data class UploadUrlRequest(val contentType: String, val fileSize: Long)
data class UploadUrlResponse(val uploadUrl: String, val mediaId: String)
data class ConfirmMediaRequest(val mediaId: String)
data class ConfirmMediaResponse(val success: Boolean, val processing: Boolean)

data class PublishVideoRequest(val title: String, val bio: String, val mediaId: String)
data class PublishVideoResponse(val success: Boolean, val message: String)


data class HookupCard(
    val userId: String,
    val name: String,
    val age: Int,
    val city: String,
    val bio: String,
    val intent: String,
    val tags: List<String>,
    val avatarUrl: String
)
data class HookupsMeta(val total: Int, val city: String?, val intent: String?)
data class HookupsResponse(val data: List<HookupCard>, val nextCursor: String?, val meta: HookupsMeta? = null)
data class HookupRequest(val targetUserId: String, val note: String, val safeMode: Boolean, val meetingType: String = "DRINK")
data class HookupRequestData(val requestId: String, val targetUserId: String, val note: String, val safeMode: Boolean, val meetingType: String, val status: String, val createdAt: Long)
data class HookupRequestResponse(val data: HookupRequestData)
interface AurelianApiService {
    // 1. Auth & Gatekeeping
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    @POST("api/v1/auth/verify-invite")
    suspend fun verifyInvite(@retrofit2.http.Header("Authorization") token: String, @Body request: VerifyInviteRequest): VerifyInviteResponse
    @POST("api/v1/auth/biometric")
    suspend fun biometricAuth(@Body request: BiometricRequest): BaseResponse
    // 2. Profile & Vetting
    @GET("api/v1/profile/me")
    suspend fun getProfile(): ProfileResponse
    @POST("api/v1/profile/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): BaseResponse
    @POST("api/v1/profile/preferences")
    suspend fun updatePreferences(@Body request: PreferencesRequest): BaseResponse
    @POST("api/v1/vetting/submit-assets")
    suspend fun submitAssets(@Body request: SubmitAssetsRequest): SubmitAssetsResponse
    // 3. Feed & Matchmaking
    @GET("api/v1/feed/videos")
    suspend fun getFeedVideos(): FeedResponse
    @POST("api/v1/interactions/like")
    suspend fun likeUser(@Body request: LikeRequest): LikeResponse
    @POST("api/v1/interactions/pass")
    suspend fun passUser(@Body request: PassRequest): BaseResponse
    @GET("api/v1/matches")
    suspend fun getMatches(@Query("page") page: Int): MatchesResponse
    @GET("api/v1/interactions/admirers")
    suspend fun getAdmirers(): AdmirersResponse
    // 4. Messaging & Invitations
    @GET("api/v1/messages/conversations")
    suspend fun getConversations(): ConversationsListResponse
    @GET("api/v1/messages/conversations/{id}/messages")
    suspend fun getMessages(@Path("id") id: String, @Query("limit") limit: Int): MessagesListResponse
    @POST("api/v1/messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse
    @POST("api/v1/invitations/send")
    suspend fun sendInvitation(@Body request: SendInviteRequest): SendInviteResponse
    @POST("api/v1/invitations/{id}/respond")
    suspend fun respondToInvitation(@Path("id") id: String, @Body request: RespondInviteRequest): RespondInviteResponse
    // 5. Events
    @GET("api/v1/events")
    suspend fun getEvents(@Query("type") type: String): EventsListResponse
    @GET("api/v1/events/{id}")
    suspend fun getEventDetails(@Path("id") id: String): EventDetailsResponse
    @POST("api/v1/events/{id}/rsvp")
    suspend fun rsvpEvent(@Path("id") id: String, @Body request: RsvpRequest): RsvpResponse
    // 6. Masquerade
    @GET("api/v1/masquerade/status")
    suspend fun getMasqueradeStatus(): MasqueradeResponseWrapper
    @POST("api/v1/masquerade/submit")
    suspend fun submitMasqueradeAnswer(@Body request: SubmitAnswerRequest): SubmitAnswerResponse
    // 7. Referrals
    @GET("api/v1/referrals/status")
    suspend fun getReferralsStatus(): ReferralsResponseWrapper
    // 8. Media
    @POST("api/v1/media/upload-url")
    suspend fun getUploadUrl(@Body request: UploadUrlRequest): UploadUrlResponse
    @POST("api/v1/media/confirm")
    suspend fun confirmMedia(@Body request: ConfirmMediaRequest): ConfirmMediaResponse

    // 9. Hookups
    @GET("api/v1/hookups/cards")
    suspend fun getHookupCards(
        @Query("city") city: String? = null,
        @Query("intent") intent: String? = null,
        @Query("limit") limit: Int = 10,
        @Query("cursor") cursor: String? = null
    ): HookupsResponse
    @POST("api/v1/hookups/request")
    suspend fun sendHookupRequest(@Body request: HookupRequest): HookupRequestResponse
    @GET("api/v1/hookups/request/{id}")
    suspend fun getHookupRequestStatus(@Path("id") id: String): HookupRequestResponse

    // 10. Publish Video
    @POST("api/v1/feed/publish")
    suspend fun publishVideo(@Body request: PublishVideoRequest): PublishVideoResponse

}
object NetworkClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService: AurelianApiService = retrofit.create(AurelianApiService::class.java)
}
