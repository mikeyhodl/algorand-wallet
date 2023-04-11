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

package com.algorand.android.modules.recovery.recoverypassphrase.ui.usecase

import com.algorand.algosdk.mobile.Mobile
import com.algorand.android.R
import com.algorand.android.core.AccountManager
import com.algorand.android.customviews.passphraseinput.usecase.PassphraseInputGroupUseCase
import com.algorand.android.customviews.passphraseinput.util.PassphraseInputConfigurationUtil
import com.algorand.android.models.Account
import com.algorand.android.models.Account.Detail
import com.algorand.android.models.Account.Type
import com.algorand.android.models.AccountCreation
import com.algorand.android.models.AnnotatedString
import com.algorand.android.modules.recovery.recoverypassphrase.ui.mapper.RecoverWithPassphrasePreviewMapper
import com.algorand.android.modules.recovery.recoverypassphrase.ui.model.RecoverWithPassphrasePreview
import com.algorand.android.utils.Event
import com.algorand.android.utils.PassphraseKeywordUtils
import com.algorand.android.utils.PassphraseKeywordUtils.ACCOUNT_PASSPHRASES_WORD_COUNT
import com.algorand.android.utils.analytics.CreationType
import com.algorand.android.utils.splitMnemonic
import com.algorand.android.utils.toShortenedAddress
import java.util.Locale
import javax.inject.Inject

class RecoverWithPassphrasePreviewUseCase @Inject constructor(
    private val recoverWithPassphrasePreviewMapper: RecoverWithPassphrasePreviewMapper,
    private val passphraseInputGroupUseCase: PassphraseInputGroupUseCase,
    private val passphraseInputConfigurationUtil: PassphraseInputConfigurationUtil,
    private val accountManager: AccountManager
) {

    fun getRecoverWithPassphraseInitialPreview(): RecoverWithPassphrasePreview {
        val passphraseInputGroupConfiguration = passphraseInputGroupUseCase.createPassphraseInputGroupConfiguration(
            itemCount = ACCOUNT_PASSPHRASES_WORD_COUNT
        )
        return recoverWithPassphrasePreviewMapper.mapToRecoverWithPassphrasePreview(
            passphraseInputGroupConfiguration = passphraseInputGroupConfiguration,
            suggestedWords = emptyList(),
            isRecoveryEnabled = false
        )
    }

    fun updatePreviewAfterPastingClipboardData(
        preview: RecoverWithPassphrasePreview,
        clipboardData: String
    ): RecoverWithPassphrasePreview {
        val splittedText = clipboardData.splitMnemonic()
        return if (splittedText.size != ACCOUNT_PASSPHRASES_WORD_COUNT) {
            preview.copy(onGlobalErrorEvent = Event(R.string.the_last_copied_text))
        } else {
            val inputGroupConfiguration = passphraseInputGroupUseCase.recoverPassphraseInputGroupConfiguration(
                configuration = preview.passphraseInputGroupConfiguration,
                itemList = splittedText
            )
            preview.copy(
                suggestedWords = emptyList(),
                passphraseInputGroupConfiguration = inputGroupConfiguration,
                onRestorePassphraseInputGroupEvent = Event(inputGroupConfiguration),
                isRecoveryEnabled = passphraseInputConfigurationUtil.areAllFieldsValid(
                    passphrasesMap = inputGroupConfiguration.passphraseInputConfigurationList
                )
            )
        }
    }

    fun updatePreviewAfterFocusChanged(
        preview: RecoverWithPassphrasePreview,
        focusedItemOrder: Int
    ): RecoverWithPassphrasePreview {
        val passphraseInputGroupConfiguration = passphraseInputGroupUseCase.updatePreviewAfterFocusChanged(
            configuration = preview.passphraseInputGroupConfiguration,
            focusedItemOrder = focusedItemOrder
        ) ?: return preview
        val suggestedWords = PassphraseKeywordUtils.getSuggestedWords(
            wordCount = PassphraseKeywordUtils.SUGGESTED_WORD_COUNT,
            prefix = passphraseInputGroupConfiguration.focusedPassphraseItem?.input.orEmpty()
        )
        return preview.copy(
            suggestedWords = suggestedWords,
            passphraseInputGroupConfiguration = passphraseInputGroupConfiguration,
            isRecoveryEnabled = passphraseInputConfigurationUtil.areAllFieldsValid(
                passphrasesMap = passphraseInputGroupConfiguration.passphraseInputConfigurationList
            )
        )
    }

    fun updatePreviewAfterFocusedInputChanged(
        preview: RecoverWithPassphrasePreview,
        word: String
    ): RecoverWithPassphrasePreview {
        val suggestedWords = PassphraseKeywordUtils.getSuggestedWords(
            wordCount = PassphraseKeywordUtils.SUGGESTED_WORD_COUNT,
            prefix = word
        )
        val passphraseInputGroupConfiguration = passphraseInputGroupUseCase.updatePreviewAfterFocusedInputChanged(
            configuration = preview.passphraseInputGroupConfiguration,
            word = word
        ) ?: return preview
        return preview.copy(
            suggestedWords = suggestedWords,
            passphraseInputGroupConfiguration = passphraseInputGroupConfiguration,
            isRecoveryEnabled = passphraseInputConfigurationUtil.areAllFieldsValid(
                passphrasesMap = passphraseInputGroupConfiguration.passphraseInputConfigurationList
            )
        )
    }

    fun validateEnteredMnemonics(preview: RecoverWithPassphrasePreview): RecoverWithPassphrasePreview {
        val mnemonics = passphraseInputConfigurationUtil.getOrderedInput(preview.passphraseInputGroupConfiguration)
        try {
            val privateKey = Mobile.mnemonicToPrivateKey(mnemonics.lowercase(Locale.ENGLISH))
            if (privateKey != null) {
                val publicKey = Mobile.generateAddressFromSK(privateKey)
                val sameAccount = getAccountIfExist(publicKey)
                if (sameAccount != null && sameAccount.type != Type.REKEYED && sameAccount.type != Type.WATCH) {
                    return preview.copy(
                        onGlobalErrorEvent = Event(R.string.this_account_already_exists)
                    )
                }
                val recoveredAccount = Account.create(
                    publicKey = publicKey,
                    detail = Detail.Standard(privateKey),
                    accountName = publicKey.toShortenedAddress()
                )
                return preview.copy(
                    onRecoverAccountEvent = Event(AccountCreation(recoveredAccount, CreationType.RECOVER))
                )
            } else {
                return preview.copy(
                    onAccountNotFoundEvent = Event(AnnotatedString(R.string.account_not_found_please_try))
                )
            }
        } catch (exception: Exception) {
            return preview.copy(
                onAccountNotFoundEvent = Event(AnnotatedString(R.string.account_not_found_please_try))
            )
        }
    }

    private fun getAccountIfExist(publicKey: String): Account? {
        return accountManager.getAccounts().find { account -> account.address == publicKey }
    }
}
