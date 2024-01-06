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

//   SwapQuoteDraft.swift

import Foundation
import MagpieCore

struct SwapQuoteDraft: JSONObjectBody {
    let providers: [SwapProvider]
    let swapperAddress: PublicKey
    let type: SwapType
    let deviceID: String
    let assetInID: AssetID
    let assetOutID: AssetID
    let amount: UInt64
    let slippage: Decimal?

    var bodyParams: [APIBodyParam] {
        var params: [APIBodyParam] = []
        params.append(.init(.providers, providers.map { $0.rawValue }))
        params.append(.init(.swapperAddress, swapperAddress))
        params.append(.init(.swapType, type))
        params.append(.init(.device, deviceID))
        params.append(.init(.assetInID, assetInID))
        params.append(.init(.assetOutID, assetOutID))
        params.append(.init(.amount, amount))
        params.append(.init(.slippage, slippage, .setIfPresent))
        return params
    }
}
