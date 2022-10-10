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

package com.algorand.android.modules.assets.profile.asaprofile.ui.usecase

import com.algorand.android.R
import com.algorand.android.decider.AssetDrawableProviderDecider
import com.algorand.android.mapper.AssetActionMapper
import com.algorand.android.mapper.AssetOperationResultMapper
import com.algorand.android.models.Account
import com.algorand.android.models.AssetAction
import com.algorand.android.models.AssetDetail
import com.algorand.android.models.AssetInformation
import com.algorand.android.models.AssetInformation.Companion.ALGO_ID
import com.algorand.android.models.Result
import com.algorand.android.modules.assets.profile.about.domain.usecase.GetAssetDetailFlowFromAsaProfileLocalCache
import com.algorand.android.modules.assets.profile.about.domain.usecase.GetSelectedAssetExchangeValueUseCase
import com.algorand.android.modules.assets.profile.asaprofile.ui.mapper.AsaProfilePreviewMapper
import com.algorand.android.modules.assets.profile.asaprofile.ui.mapper.AsaStatusPreviewMapper
import com.algorand.android.modules.assets.profile.asaprofile.ui.model.AsaProfilePreview
import com.algorand.android.modules.assets.profile.asaprofile.ui.model.AsaStatusActionType
import com.algorand.android.modules.assets.profile.asaprofile.ui.model.AsaStatusPreview
import com.algorand.android.modules.assets.profile.asaprofile.ui.model.PeraButtonState
import com.algorand.android.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import com.algorand.android.repository.TransactionsRepository
import com.algorand.android.usecase.AccountAddressUseCase
import com.algorand.android.usecase.AccountDetailUseCase
import com.algorand.android.usecase.AssetAdditionUseCase
import com.algorand.android.usecase.SimpleAssetDetailUseCase
import com.algorand.android.utils.AssetName
import com.algorand.android.utils.Event
import com.algorand.android.utils.NO_PRICE_SIGN
import com.algorand.android.utils.Resource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

@SuppressWarnings("LongParameterList")
class AsaProfilePreviewUseCase @Inject constructor(
    private val getAssetDetailFlowFromAsaProfileLocalCache: GetAssetDetailFlowFromAsaProfileLocalCache,
    private val getSelectedAssetExchangeValueUseCase: GetSelectedAssetExchangeValueUseCase,
    private val asaProfilePreviewMapper: AsaProfilePreviewMapper,
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider,
    private val accountAddressUseCase: AccountAddressUseCase,
    private val asaStatusPreviewMapper: AsaStatusPreviewMapper,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val transactionsRepository: TransactionsRepository,
    private val assetAdditionUseCase: AssetAdditionUseCase,
    private val simpleAssetDetailUseCase: SimpleAssetDetailUseCase,
    private val assetActionMapper: AssetActionMapper,
    private val assetOperationResultMapper: AssetOperationResultMapper
) {
    // TODO: Use SendSignedTransactionUseCase instead of transactionsRepository
    suspend fun sendTransaction(
        signedTransactionData: ByteArray,
        assetInformation: AssetInformation,
        account: Account
    ) = flow {
        when (val result = transactionsRepository.sendSignedTransaction(signedTransactionData)) {
            is Result.Success -> {
                assetAdditionUseCase.addAssetAdditionToAccountCache(account.address, assetInformation)
                val assetOperationResult = assetOperationResultMapper.mapTo(
                    resultTitleResId = R.string.asset_successfully_added_formatted,
                    assetName = AssetName.create(assetInformation.fullName)
                )
                emit(Event(Resource.Success(assetOperationResult)))
            }
            is Result.Error -> emit(Event(result.getAsResourceError()))
        }
    }

    // TODO: We should fetch asset details from API
    fun createAssetAction(assetId: Long, accountAddress: String?): AssetAction {
        val assetDetail = simpleAssetDetailUseCase.getCachedAssetDetail(assetId)?.data
        return assetActionMapper.mapTo(
            assetId = assetId,
            fullName = assetDetail?.fullName ?: assetId.toString(),
            shortName = assetDetail?.shortName,
            verificationTier = assetDetail?.verificationTier,
            accountAddress = accountAddress,
            creatorPublicKey = assetDetail?.assetCreator?.publicKey
        )
    }

    fun getAsaProfilePreview(accountAddress: String?, assetId: Long): Flow<AsaProfilePreview?> {
        return when {
            accountAddress.isNullOrBlank() -> createAsaProfilePreviewWithoutAccountInformation()
            assetId == ALGO_ID -> createAlgoProfilePreviewWithAccountInformation(accountAddress)
            else -> createAsaProfilePreviewWithAccountInformation(accountAddress)
        }
    }

    private fun createAlgoProfilePreviewWithAccountInformation(accountAddress: String) = flow {
        simpleAssetDetailUseCase.getCachedAssetDetail(ALGO_ID)?.useSuspended(
            onSuccess = { cachedAlgoDetail ->
                val asaStatusPreview = createAsaStatusPreview(
                    isUserOptedInAsset = true,
                    accountAddress = accountAddress
                )
                val asaProfilePreview = cachedAlgoDetail.data?.run {
                    createAsaProfilePreviewFromAssetDetail(assetDetail = this, asaStatusPreview = asaStatusPreview)
                }
                emit(asaProfilePreview)
            }
        )
    }.distinctUntilChanged()

    private fun createAsaProfilePreviewWithAccountInformation(accountAddress: String): Flow<AsaProfilePreview?> {
        return combine(
            getAssetDetailFlowFromAsaProfileLocalCache.getAssetDetailFlowFromAsaProfileLocalCache(),
            accountDetailUseCase.getAccountDetailCacheFlow(accountAddress)
        ) { cachedAssetDetailResult, _ ->
            val assetId = cachedAssetDetailResult?.data?.assetId
            val isUserOptedInAsset = if (assetId != null) {
                accountDetailUseCase.isAssetOwnedByAccount(publicKey = accountAddress, assetId = assetId)
            } else {
                false
            }
            val asaStatusPreview = createAsaStatusPreview(
                isUserOptedInAsset = isUserOptedInAsset,
                accountAddress = accountAddress
            )
            cachedAssetDetailResult?.data?.run {
                createAsaProfilePreviewFromAssetDetail(assetDetail = this, asaStatusPreview = asaStatusPreview)
            }
        }
    }

    private fun createAsaProfilePreviewWithoutAccountInformation() = flow {
        getAssetDetailFlowFromAsaProfileLocalCache.getAssetDetailFlowFromAsaProfileLocalCache()
            .collect { cachedAssetDetailResult ->
                val asaStatusPreview = createAsaStatusPreview(false, null)
                val preview = cachedAssetDetailResult?.data?.run {
                    createAsaProfilePreviewFromAssetDetail(assetDetail = this, asaStatusPreview = asaStatusPreview)
                }
                emit(preview)
            }
    }

    private fun createAsaProfilePreviewFromAssetDetail(
        assetDetail: AssetDetail,
        asaStatusPreview: AsaStatusPreview?
    ): AsaProfilePreview {
        return with(assetDetail) {
            val formattedAssetPrice = if (usdValue != null || assetId == ALGO_ID) {
                getSelectedAssetExchangeValueUseCase.getSelectedAssetExchangeValue(this).getFormattedValue()
            } else {
                NO_PRICE_SIGN
            }
            val verificationTierConfiguration = verificationTierConfigurationDecider
                .decideVerificationTierConfiguration(verificationTier)
            val assetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(assetId)
            asaProfilePreviewMapper.mapToAsaProfilePreview(
                isAlgo = assetDetail.assetId == ALGO_ID,
                assetFullName = fullName,
                assetShortName = shortName,
                assetId = assetId,
                formattedAssetPrice = formattedAssetPrice,
                verificationTierConfiguration = verificationTierConfiguration,
                baseAssetDrawableProvider = assetDrawableProvider,
                assetPrismUrl = logoUri,
                asaStatusPreview = asaStatusPreview
            )
        }
    }

    private fun createAsaStatusPreview(isUserOptedInAsset: Boolean, accountAddress: String?): AsaStatusPreview? {
        return when {
            accountAddress.isNullOrBlank() -> {
                asaStatusPreviewMapper.mapToAsaStatusPreview(
                    accountAddress = null,
                    statusLabelTextResId = R.string.you_can_opt_in_to_this,
                    peraButtonState = PeraButtonState.ADDITION,
                    actionButtonTextResId = R.string.add_asset,
                    asaStatusActionType = AsaStatusActionType.ACCOUNT_SELECTION
                )
            }
            !isUserOptedInAsset -> {
                asaStatusPreviewMapper.mapToAsaStatusPreview(
                    accountAddress = accountAddressUseCase.createAccountAddress(accountAddress),
                    statusLabelTextResId = R.string.you_can_add_this_asset,
                    peraButtonState = PeraButtonState.ADDITION,
                    actionButtonTextResId = R.string.add_asset,
                    asaStatusActionType = AsaStatusActionType.ADDITION
                )
            }
            else -> null
        }
    }
}
