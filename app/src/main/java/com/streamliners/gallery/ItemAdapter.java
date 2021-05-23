package com.streamliners.gallery;

import android.content.Context;
import android.os.Build;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.streamliners.gallery.databinding.ItemCardBinding;
import com.streamliners.gallery.models.Item;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Inflater;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemHolder> implements ItemAdapterInterface {

    private Context context;
    private List<Item> items;
    private List<Item> itemsToShow;
    public ItemTouchHelper mItemTouchHelper;
    public String imageUrl;
    public int index;
    public ItemCardBinding itemCardBinding;

    /**
     * Parameterised Constructor for ItemAdapter
     * @param context
     * @param items
     */
    public ItemAdapter(Context context, List<Item> items){
        this.context = context;
        this.items = items;
        itemsToShow = items;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false);

        return new ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        //Inflate Card
        holder.binding.title.setText(itemsToShow.get(position).label);
        holder.binding.title.setBackgroundColor(itemsToShow.get(position).color);
        Glide.with(context)
                .asBitmap()
                .load(itemsToShow.get(position).imageUrl)
                .into(holder.binding.imageView);
    }

    @Override
    public int getItemCount() {
        return itemsToShow.size();
    }

    /**
     * Search Option
     * @param query
     */
    public void filter(String query){
        //Filter According to Search Query
        if (query.trim().isEmpty()) {
            itemsToShow = items;
            notifyDataSetChanged();
            return;
        }
        query = query.toLowerCase();
        // Temporary list of items filtered according to search
        List<Item> tempItems = new ArrayList<>();
        for (Item item : items){
            if (item.label.toLowerCase().contains(query)){
                tempItems.add(item);
            }
        }
        itemsToShow = tempItems;
        notifyDataSetChanged();
    }

    /**
     * Sort Items Alphabetically
     */
    public void sortAlphabetically(){
        //Sort List of Items according to alphabetical order of labels
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.label.compareTo(o2.label);
            }
        });
        itemsToShow = items;
        notifyDataSetChanged();
    }

    /**
     * Change position on item drag
     * @param from
     * @param to
     */
    @Override
    public void onItemDrag(int from, int to) {
        Item fromItem = items.get(from);
        items.remove(fromItem);
        items.add(to, fromItem);
        itemsToShow = items;
        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwipe(int position) {
        return;
    }

    public void setItemAdapterHelper(ItemTouchHelper itemTouchHelper){

        mItemTouchHelper = itemTouchHelper;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, GestureDetector.OnGestureListener, View.OnCreateContextMenuListener {
        public ItemCardBinding binding;
        GestureDetector gestureDetector;

        public ItemHolder(@NonNull ItemCardBinding binding){
            super(binding.getRoot());
            this.binding = binding;
            gestureDetector = new GestureDetector(binding.getRoot().getContext(), this);
            //Set on touch listener for drag movement
            binding.imageView.setOnTouchListener(this);
            //Create Context Menu
            binding.title.setOnCreateContextMenuListener(this);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mItemTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        /**
         * Create Context Menu with Edit and share option
         * @param menu
         * @param v
         * @param menuInfo
         */
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select The Action");
            menu.add(this.getAdapterPosition(), R.id.editMenuItem, 0, "Edit Item");
            menu.add(this.getAdapterPosition(), R.id.shareImage, 0, "Share Image");
            imageUrl = items.get(this.getAdapterPosition()).imageUrl;
            index = this.getAdapterPosition();
            itemCardBinding = binding;
        }
    }

}
