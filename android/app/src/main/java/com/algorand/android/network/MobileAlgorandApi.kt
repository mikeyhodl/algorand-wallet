/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.algorand.android.network

import com.algorand.android.banner.data.model.BannerListResponse
import com.algorand.android.deviceregistration.data.model.DeviceRegistrationRequest
import com.algorand.android.deviceregistration.data.model.DeviceRegistrationResponse
import com.algorand.android.deviceregistration.data.model.DeviceUpdateRequest
import com.algorand.android.models.AssetDetailResponse
import com.algorand.android.models.AssetSearchResponse
import com.algorand.android.models.AssetSupportRequest
import com.algorand.android.models.Feedback
import com.algorand.android.models.FeedbackCategory
import com.algorand.android.models.NotificationFilterRequest
import com.algorand.android.models.NotificationItem
import com.algorand.android.models.Pagination
import com.algorand.android.models.PushTokenDeleteRequest
import com.algorand.android.models.TrackTransactionRequest
import com.algorand.android.models.VerifiedAssetDetail
import com.algorand.android.modules.accounts.data.model.LastSeenNotificationRequest
import com.algorand.android.modules.accounts.data.model.LastSeenNotificationResponse
import com.algorand.android.modules.accounts.data.model.NotificationStatusResponse
import com.algorand.android.modules.currency.data.model.CurrencyOptionResponse
import com.algorand.android.modules.nftdomain.data.model.NftDomainSearchResponse
import com.algorand.android.modules.parity.data.model.CurrencyDetailResponse
import com.algorand.android.network.MobileHeaderInterceptor.Companion.ALGORAND_NETWORK_KEY
import com.algorand.android.modules.assets.addition.base.ui.BaseAddAssetViewModel.Companion.SEARCH_RESULT_LIMIT
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MobileAlgorandApi {

    @POST("feedback/")
    suspend fun postFeedback(@Body feedback: Feedback): Response<Unit>

    @GET("feedback/categories/")
    suspend fun getFeedbackCategories(): Response<List<FeedbackCategory>>

    @POST("devices/")
    suspend fun postRegisterDevice(
        @Body deviceRegistrationRequest: DeviceRegistrationRequest
    ): Response<DeviceRegistrationResponse>

    @PUT("devices/{device_id}/")
    suspend fun putUpdateDevice(
        @Path("device_id") deviceId: String,
        @Body deviceUpdateRequest: DeviceUpdateRequest,
        @Header(ALGORAND_NETWORK_KEY) networkSlug: String?
    ): Response<DeviceRegistrationResponse>

    @HTTP(method = "DELETE", path = "devices/", hasBody = true)
    suspend fun deletePushToken(
        @Header(ALGORAND_NETWORK_KEY) networkSlug: String,
        @Body pushTokenDeleteRequest: PushTokenDeleteRequest
    ): Response<Unit>

    @POST("transactions/")
    suspend fun trackTransaction(@Body trackTransactionRequest: TrackTransactionRequest): Response<Unit>

    @POST("asset-requests/")
    suspend fun postAssetSupportRequest(@Body assetSupportRequest: AssetSupportRequest): Response<Unit>

    @GET("verified-assets/?limit=all")
    suspend fun getVerifiedAssets(): Response<Pagination<VerifiedAssetDetail>>

    @GET("devices/{device_id}/notifications/")
    suspend fun getNotifications(
        @Path("device_id") deviceId: String
    ): Response<Pagination<NotificationItem>>

    @GET("devices/{device_id}/notification-status/")
    suspend fun getNotificationStatus(
        @Path("device_id") deviceId: String
    ): Response<NotificationStatusResponse>

    @PUT("devices/{device_id}/update-last-seen-notification/")
    suspend fun putLastSeenNotification(
        @Path("device_id") deviceId: String,
        @Body lastSeenRequest: LastSeenNotificationRequest,
        @Header(ALGORAND_NETWORK_KEY) networkSlug: String?
    ): Response<LastSeenNotificationResponse>

    @GET
    suspend fun getNotificationsMore(@Url url: String): Response<Pagination<NotificationItem>>

    @GET("assets/search/")
    suspend fun getAssets(
        @Query("paginator") paginator: String? = "cursor",
        @Query("q") assetQuery: String?,
        @Query("offset") offset: Long = 0,
        @Query("limit") limit: Int = SEARCH_RESULT_LIMIT,
        @Query("has_collectible") hasCollectible: Boolean? = null
    ): Response<Pagination<AssetSearchResponse>>

    @GET("assets/")
    suspend fun getAssetsByIds(
        @Query("asset_ids", encoded = true) assetIdsList: String,
        @Query("include_deleted") includeDeleted: Boolean? = null
    ): Response<Pagination<AssetDetailResponse>>

    @GET("assets/{asset_id}/")
    suspend fun getAssetDetail(
        @Path("asset_id") nftAssetId: Long
    ): Response<AssetDetailResponse>

    @GET
    suspend fun getAssetsMore(@Url url: String): Response<Pagination<AssetSearchResponse>>

    @GET("currencies/")
    suspend fun getCurrencies(): Response<List<CurrencyOptionResponse>>

    @GET("currencies/{currency_id}/")
    suspend fun getCurrencyDetail(
        @Path("currency_id") currencyId: String
    ): Response<CurrencyDetailResponse>

    @PATCH("devices/{device_id}/accounts/{address}/")
    suspend fun putNotificationFilter(
        @Path("device_id") deviceId: String,
        @Path("address") address: String,
        @Body notificationFilterRequest: NotificationFilterRequest
    ): Response<Unit>

    @GET("devices/{device_id}/banners/")
    suspend fun getDeviceBanners(
        @Path("device_id") deviceId: String
    ): Response<BannerListResponse>

    @GET("name-services/search/")
    suspend fun getNftDomainAccountAddresses(
        @Query("name") name: String
    ): Response<NftDomainSearchResponse>

    @Streaming
    @GET("accounts/{address}/export-history/")
    suspend fun getExportHistory(
        @Path("address") address: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): Response<ResponseBody>
}
