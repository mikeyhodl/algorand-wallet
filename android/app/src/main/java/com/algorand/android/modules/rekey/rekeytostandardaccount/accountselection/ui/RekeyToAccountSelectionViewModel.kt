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

package com.algorand.android.modules.rekey.rekeytostandardaccount.accountselection.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.algorand.android.core.BaseViewModel
import com.algorand.android.modules.rekey.rekeytostandardaccount.accountselection.ui.model.RekeyToAccountSelectionPreview
import com.algorand.android.modules.rekey.rekeytostandardaccount.accountselection.ui.usecase.RekeyToAccountSelectionPreviewUseCase
import com.algorand.android.utils.getOrThrow
import com.algorand.android.utils.launchIO
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class RekeyToAccountSelectionViewModel @Inject constructor(
    private val rekeyToAccountSelectionPreviewUseCase: RekeyToAccountSelectionPreviewUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val accountAddress = savedStateHandle.getOrThrow<String>(ACCOUNT_ADDRESS_KEY)

    private val _rekeyToAccountSelectionPreviewFlow = MutableStateFlow<RekeyToAccountSelectionPreview?>(null)
    val rekeyToAccountSelectionPreviewFlow: StateFlow<RekeyToAccountSelectionPreview?>
        get() = _rekeyToAccountSelectionPreviewFlow

    init {
        setInitialPreview()
    }

    private fun setInitialPreview() {
        viewModelScope.launchIO {
            val preview = rekeyToAccountSelectionPreviewUseCase.getInitialRekeyToAccountSelectionPreview(accountAddress)
            _rekeyToAccountSelectionPreviewFlow.emit(preview)
        }
    }

    companion object {
        private const val ACCOUNT_ADDRESS_KEY = "accountAddress"
    }
}
