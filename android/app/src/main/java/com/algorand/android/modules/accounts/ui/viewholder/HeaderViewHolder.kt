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

package com.algorand.android.modules.accounts.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.algorand.android.databinding.ItemAccountHeaderBinding
import com.algorand.android.models.BaseViewHolder
import com.algorand.android.modules.accounts.domain.model.BaseAccountListItem

class HeaderViewHolder(
    val binding: ItemAccountHeaderBinding,
    val listener: OptionsClickListener
) : BaseViewHolder<BaseAccountListItem>(binding.root) {

    override fun bind(item: BaseAccountListItem) {
        if (item !is BaseAccountListItem.HeaderItem) return
        with(binding.listConfigurationHeaderView) {
            setTitle(item.titleResId)
            setPrimaryButtonClickListener { listener.onSortClick() }
            setSecondaryButtonClickListener { listener.onAddAccountClick() }
        }
    }

    companion object {
        fun create(parent: ViewGroup, listener: OptionsClickListener): HeaderViewHolder {
            val binding = ItemAccountHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return HeaderViewHolder(binding, listener)
        }
    }

    interface OptionsClickListener {
        fun onSortClick()
        fun onAddAccountClick()
    }
}
