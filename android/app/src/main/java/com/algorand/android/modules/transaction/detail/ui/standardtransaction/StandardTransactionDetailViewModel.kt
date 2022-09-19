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

package com.algorand.android.modules.transaction.detail.ui.standardtransaction

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.algorand.android.modules.transaction.detail.domain.model.BaseTransactionDetail
import com.algorand.android.modules.transaction.detail.domain.usecase.StandardTransactionDetailPreviewUseCase
import com.algorand.android.modules.transaction.detail.ui.BaseTransactionDetailViewModel
import com.algorand.android.utils.getOrElse
import com.algorand.android.utils.getOrThrow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StandardTransactionDetailViewModel @ViewModelInject constructor(
    override val baseTransactionDetailPreviewUseCase: StandardTransactionDetailPreviewUseCase,
    @Assisted savedStateHandle: SavedStateHandle
) : BaseTransactionDetailViewModel() {

    val transactionId = savedStateHandle.getOrThrow<String>(TRANSACTION_ID_KEY)
    val publicKey = savedStateHandle.getOrThrow<String>(PUBLIC_KEY)
    private val transaction = savedStateHandle.getOrElse<BaseTransactionDetail?>(TRANSACTION_KEY, null)
    val shouldShowCloseButton = savedStateHandle.getOrElse(SHOW_CLOSE_BUTTON_KEY, false)

    init {
        if (transaction != null) {
            createTransactionDetailFromExistingModel(transaction)
        } else {
            initTransactionDetailPreview()
        }
    }

    override fun initTransactionDetailPreview() {
        viewModelScope.launch {
            baseTransactionDetailPreviewUseCase.getTransactionDetailPreview(
                transactionId = transactionId,
                publicKey = publicKey,
                isInnerTransaction = false
            ).collect {
                updateTransactionDetailFlow(it)
            }
        }
    }

    private fun createTransactionDetailFromExistingModel(transaction: BaseTransactionDetail) {
        viewModelScope.launch {
            updateTransactionDetailFlow(
                baseTransactionDetailPreviewUseCase.createTransactionDetailListItems(
                    baseTransactionDetail = transaction,
                    publicKey = publicKey,
                    transactionId = transactionId,
                    isInnerTransaction = true
                )
            )
        }
    }
}