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

package com.algorand.android.discover.home.domain

import android.webkit.JavascriptInterface

class PeraMobileWebInterface private constructor(val listener: WebInterfaceListener) {

    @JavascriptInterface
    fun pushTokenDetailScreen(jsonEncodedPayload: String) {
        listener.pushTokenDetailScreen(jsonEncodedPayload)
    }

    @JavascriptInterface
    fun pushDappViewerScreen(jsonEncodedPayload: String) {
        listener.pushDappViewerScreen(jsonEncodedPayload)
    }

    @JavascriptInterface
    fun pushNewScreen(jsonEncodedPayload: String) {
        listener.pushNewScreen(jsonEncodedPayload)
    }

    @JavascriptInterface
    fun getDeviceId(jsonEncodedPayload: String) {
        listener.getDeviceId()
    }

    @JavascriptInterface
    fun handleTokenDetailActionButtonClick(jsonEncodedPayload: String) {
        listener.handleTokenDetailActionButtonClick(jsonEncodedPayload)
    }

    @JavascriptInterface
    fun openSystemBrowser(jsonEncodedPayload: String) {
        listener.openSystemBrowser(jsonEncodedPayload)
    }

    interface WebInterfaceListener {
        fun pushTokenDetailScreen(jsonEncodedPayload: String) {}
        fun pushDappViewerScreen(jsonEncodedPayload: String) {}
        fun pushNewScreen(jsonEncodedPayload: String) {}
        fun handleTokenDetailActionButtonClick(jsonEncodedPayload: String) {}
        fun getDeviceId() {}
        fun openSystemBrowser(jsonEncodedPayload: String) {}
    }

    companion object {
        const val WEB_INTERFACE_NAME = "peraMobileInterface"
        fun create(listener: WebInterfaceListener): PeraMobileWebInterface {
            return PeraMobileWebInterface(listener)
        }
    }
}
