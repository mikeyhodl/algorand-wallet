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

package com.algorand.android.usecase

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.algorand.android.mapper.AssetAdditionLoadStatePreviewMapper
import com.algorand.android.mapper.AssetHoldingsMapper
import com.algorand.android.models.AssetInformation
import com.algorand.android.models.ui.AssetAdditionLoadStatePreview
import com.algorand.android.modules.assets.addition.ui.model.AssetAdditionType
import com.algorand.android.utils.CacheResult
import com.algorand.android.utils.Event
import javax.inject.Inject

class AssetAdditionUseCase @Inject constructor(
    private val accountDetailUseCase: AccountDetailUseCase,
    private val assetHoldingsMapper: AssetHoldingsMapper,
    private val assetAdditionLoadStatePreviewMapper: AssetAdditionLoadStatePreviewMapper
) {

    suspend fun addAssetAdditionToAccountCache(publicKey: String, assetInformation: AssetInformation) {
        val cachedAccount = accountDetailUseCase.getCachedAccountDetail(publicKey)?.data ?: return
        val pendingAssetHolding = assetHoldingsMapper.mapToPendingAdditionAssetHoldings(assetInformation)
        cachedAccount.accountInformation.addPendingAssetHolding(pendingAssetHolding)
        accountDetailUseCase.cacheAccountDetail(CacheResult.Success.create(cachedAccount))
    }

    fun createAssetAdditionLoadStatePreview(
        combinedLoadStates: CombinedLoadStates,
        itemCount: Int,
        isLastStateError: Boolean,
        assetAdditionType: AssetAdditionType
    ): AssetAdditionLoadStatePreview {
        return assetAdditionLoadStatePreviewMapper.mapToAssetAdditionLoadStatePreview(
            combinedLoadStates = combinedLoadStates,
            itemCount = itemCount,
            assetAdditionType = assetAdditionType,
            isAssetListVisible = (combinedLoadStates.refresh is LoadState.Error).not() &&
                (isLastStateError && combinedLoadStates.refresh is LoadState.Loading).not(),
            isLoading = (combinedLoadStates.refresh is LoadState.Loading) ||
                (combinedLoadStates.append is LoadState.Loading),
            onRetryEvent = if (combinedLoadStates.refresh is LoadState.Error) Event(Unit) else null
        )
    }
}
