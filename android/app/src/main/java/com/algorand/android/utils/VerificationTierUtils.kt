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

package com.algorand.android.utils

import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.algorand.android.assetsearch.ui.model.VerificationTierConfiguration

fun ImageView.setDrawableByVerificationTier(verificationTierConfiguration: VerificationTierConfiguration) {
    isVisible = verificationTierConfiguration.drawableResId != null
    setImageResource(verificationTierConfiguration.drawableResId ?: return)
}

fun TextView.setAssetNameTextColorByVerificationTier(verificationTierConfiguration: VerificationTierConfiguration) {
    val textColorResId = verificationTierConfiguration.textColorResId
    setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, textColorResId)))
}
