package com.example.weatherapp.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemTouchHelper.Callback 实现。
 * 处理 RecyclerView 列表项的拖拽移动和左滑删除手势。
 */
public class ItemMoveCallback extends ItemTouchHelper.Callback {

    private final ItemMoveListener mListener;

    /** 拖拽和滑动监听接口 */
    public interface ItemMoveListener {
        void onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
        void onItemMoveFinished();
    }

    public ItemMoveCallback(ItemMoveListener listener) {
        this.mListener = listener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true; // 允许长按拖拽
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true; // 允许左滑删除
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        mListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mListener.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        // 拖拽结束，通知保存顺序
        mListener.onItemMoveFinished();
    }
}
