// Copyright 2022 Pera Wallet, LDA

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//   AddAssetTransactionOptionListActionViewModel.swift

import MacaroonUIKit

struct AddAssetTransactionOptionListActionViewModel: TransactionOptionListItemButtonViewModel {
    let icon: Image?
    let title: EditText?
    let subtitle: EditText?

    init() {
        icon = "add-icon-40"
        title = Self.getTitle("transaction-option-list-add-asset-title".localized)
        subtitle = Self.getSubtitle("transaction-option-list-add-asset-subtitle".localized)
    }
}
