package com.alberto.tradepop.Extensions

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.bottom = space
        if (parent.getChildLayoutPosition(view) == 0 || parent.getChildLayoutPosition(view) == 1) {
            outRect.top = space
        } else {
            outRect.top = 0
        }
        if (parent.getChildLayoutPosition(view) % 2 == 0) {
            outRect.right = space/2
            outRect.left = 0
        } else {
            outRect.right = 0
            outRect.left = space/2
        }
    }
}