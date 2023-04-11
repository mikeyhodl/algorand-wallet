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

package com.algorand.android.customviews.alertview.ui

import android.animation.Animator

interface AlertViewAnimatorListener : Animator.AnimatorListener {

    override fun onAnimationCancel(animation: Animator)

    override fun onAnimationEnd(animation: Animator) {
        // optional
    }

    override fun onAnimationStart(animation: Animator) {
        // optional
    }

    override fun onAnimationRepeat(animation: Animator) {
        // no op
    }
}
