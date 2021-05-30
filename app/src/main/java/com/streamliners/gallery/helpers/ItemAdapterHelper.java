package com.streamliners.gallery.helpers;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.streamliners.gallery.adapters.ItemAdapterInterface;

import org.jetbrains.annotations.NotNull;

public class ItemAdapterHelper extends ItemTouchHelper.Callback{

    ItemAdapterInterface itemTouchHelperAdapter;

    /**
     * Parameterised Constructor for ItemAdapterHelper
     * @param itemTouchHelperAdapter
     */
    public ItemAdapterHelper(ItemAdapterInterface itemTouchHelperAdapter) {
        this.itemTouchHelperAdapter = itemTouchHelperAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    /**
     * Adds movement flags for swipe and drag movement
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    /**
     * On Move Callback
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return
     */
    @Override
    public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
        itemTouchHelperAdapter.onItemDrag(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * On Swipe Callback
     * @param viewHolder
     * @param direction
     */
    @Override
    public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
        itemTouchHelperAdapter.onItemSwipe(viewHolder.getAdapterPosition());
    }

}
