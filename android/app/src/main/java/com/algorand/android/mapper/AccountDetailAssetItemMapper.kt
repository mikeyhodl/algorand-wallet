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

package com.algorand.android.mapper

import com.algorand.android.R
import com.algorand.android.decider.AssetDrawableProviderDecider
import com.algorand.android.models.AccountDetailAssetsItem
import com.algorand.android.models.AccountDetailAssetsItem.BaseAssetItem.BasePendingAssetItem.PendingAdditionItem
import com.algorand.android.models.AccountDetailAssetsItem.BaseAssetItem.BasePendingAssetItem.PendingRemovalItem
import com.algorand.android.models.BaseAccountAssetData
import com.algorand.android.models.BaseAccountAssetData.PendingAssetData
import com.algorand.android.modules.verificationtier.ui.decider.VerificationTierConfigurationDecider
import com.algorand.android.utils.AssetName
import javax.inject.Inject

// TODO Rename this function to make it screen independent
class AccountDetailAssetItemMapper @Inject constructor(
    private val verificationTierConfigurationDecider: VerificationTierConfigurationDecider,
    private val assetDrawableProviderDecider: AssetDrawableProviderDecider
) {

    fun mapToOwnedAssetItem(
        accountAssetData: BaseAccountAssetData.BaseOwnedAssetData
    ): AccountDetailAssetsItem.BaseAssetItem.OwnedAssetItem {
        return with(accountAssetData) {
            AccountDetailAssetsItem.BaseAssetItem.OwnedAssetItem(
                id = id,
                name = AssetName.create(name),
                shortName = AssetName.createShortName(shortName),
                formattedAmount = formattedCompactAmount,
                formattedDisplayedCurrencyValue = getSelectedCurrencyParityValue()
                    .getFormattedCompactValue(),
                isAmountInDisplayedCurrencyVisible = isAmountInSelectedCurrencyVisible,
                verificationTierConfiguration = verificationTierConfigurationDecider
                    .decideVerificationTierConfiguration(verificationTier),
                baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(id),
                prismUrl = prismUrl,
                amountInSelectedCurrency = parityValueInSelectedCurrency.amountAsCurrency
            )
        }
    }

    fun mapToPendingAdditionAssetItem(accountAssetData: PendingAssetData.AdditionAssetData): PendingAdditionItem {
        return PendingAdditionItem(
            id = accountAssetData.id,
            name = AssetName.create(accountAssetData.name),
            shortName = AssetName.createShortName(accountAssetData.shortName),
            actionDescriptionResId = R.string.adding_asset,
            verificationTierConfiguration = verificationTierConfigurationDecider.decideVerificationTierConfiguration(
                accountAssetData.verificationTier
            ),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(accountAssetData.id)
        )
    }

    fun mapToPendingRemovalAssetItem(accountAssetData: PendingAssetData.DeletionAssetData): PendingRemovalItem {
        return PendingRemovalItem(
            id = accountAssetData.id,
            name = AssetName.create(accountAssetData.name),
            shortName = AssetName.createShortName(accountAssetData.shortName),
            actionDescriptionResId = R.string.removing_asset,
            verificationTierConfiguration = verificationTierConfigurationDecider.decideVerificationTierConfiguration(
                accountAssetData.verificationTier
            ),
            baseAssetDrawableProvider = assetDrawableProviderDecider.getAssetDrawableProvider(accountAssetData.id)
        )
    }

    fun mapToQuickActionsItem(isSwapButtonSelected: Boolean): AccountDetailAssetsItem.QuickActionsItem {
        return AccountDetailAssetsItem.QuickActionsItem(isSwapButtonSelected = isSwapButtonSelected)
    }

    fun mapToSearchViewItem(query: String): AccountDetailAssetsItem.SearchViewItem {
        return AccountDetailAssetsItem.SearchViewItem(query = query)
    }

    fun mapToTitleItem(titleRes: Int, isAddAssetButtonVisible: Boolean): AccountDetailAssetsItem.TitleItem {
        return AccountDetailAssetsItem.TitleItem(titleRes, isAddAssetButtonVisible)
    }

    fun mapToNoAssetFoundViewItem(): AccountDetailAssetsItem.NoAssetFoundViewItem {
        return AccountDetailAssetsItem.NoAssetFoundViewItem
    }
}
