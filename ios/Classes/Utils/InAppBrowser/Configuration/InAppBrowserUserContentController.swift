// Copyright 2023 Pera Wallet, LDA

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//   InAppBrowserUserContentController.swift

import Foundation
import WebKit

final class InAppBrowserUserContentController: WKUserContentController {
    /// <warning>
    /// It isn't enough to guarantee that there won't be a memory leak because of a retain cycle
    /// between the user content controller and the script message handler; thus, the message
    /// handlers should be removed just in case in `deinit` of the scrip message handler, which is
    /// probably a view controller.
    func add<T>(
        secureScriptMessageHandler: WKScriptMessageHandler,
        forMessage msg: T
    ) where T: InAppBrowserScriptMessage {
        add(
            secureScriptMessageHandler: secureScriptMessageHandler,
            forName: msg.rawValue
        )
    }

    /// <warning>
    /// It isn't enough to guarantee that there won't be a memory leak because of a retain cycle
    /// between the user content controller and the script message handler; thus, the message
    /// handlers should be removed just in case in `deinit` of the scrip message handler, which is
    /// probably a view controller.
    func add(
        secureScriptMessageHandler: WKScriptMessageHandler,
        forName name: String
    ) {
        let handler = InAppBrowserSecureScriptMessageHandler(handler: secureScriptMessageHandler)
        add(
            handler,
            name: name
        )
    }

    func removeScriptMessageHandlers<T>(forMessages msgs: some Collection<T>)
    where T: InAppBrowserScriptMessage {
        msgs.forEach(removeScriptMessageHandler)
    }

    func removeScriptMessageHandler<T>(forMessage msg: T)
    where T: InAppBrowserScriptMessage {
        removeScriptMessageHandler(forName: msg.rawValue)
    }
}

protocol InAppBrowserScriptMessage:
    RawRepresentable,
    CaseIterable
where RawValue == String {}

final class InAppBrowserSecureScriptMessageHandler:
    NSObject,
    WKScriptMessageHandler {
    private weak var handler: WKScriptMessageHandler?

    init(handler: WKScriptMessageHandler) {
        self.handler = handler
    }

    /// <mark>
    /// WKScriptMessageHandler
    func userContentController(
        _ userContentController: WKUserContentController,
        didReceive message: WKScriptMessage
    ) {
        handler?.userContentController(
            userContentController,
            didReceive: message
        )
    }
}

struct NoInAppBrowserScriptMessage: InAppBrowserScriptMessage {
    var rawValue: String = ""

    static var allCases: [NoInAppBrowserScriptMessage] = []

    init?(rawValue: String) {
        return nil
    }
}
