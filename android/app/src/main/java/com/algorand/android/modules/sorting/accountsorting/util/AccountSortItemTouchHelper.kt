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

package com.algorand.android.modules.sorting.accountsorting.util

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.algorand.android.modules.sorting.accountsorting.ui.viewholder.AccountSortItemViewHolder

// TODO: Make this class generic
class AccountSortItemTouchHelper(
    private val listener: ItemMoveListener
) : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (!isViewHoldersInTheSameAdapter(viewHolder, target)) return false
        if (target !is AccountSortItemViewHolder) return false

        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        listener.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Nothing to do here
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    private fun isViewHoldersInTheSameAdapter(
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return viewHolder.bindingAdapter == target.bindingAdapter
    }

    fun interface ItemMoveListener {
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }
}
