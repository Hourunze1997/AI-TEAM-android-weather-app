package com.example.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.model.City;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 城市列表适配器。
 * 用于城市搜索结果和城市管理页的城市列表展示。
 * 支持拖拽移动和左滑删除。
 */
public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityViewHolder>
        implements com.example.weatherapp.util.ItemMoveCallback.ItemMoveListener {

    private final List<City> cities = new ArrayList<>();
    private final OnCityActionListener listener;
    private final boolean showDeleteButton;

    /** 城市列表项点击/删除事件回调接口 */
    public interface OnCityActionListener {
        void onCityClick(City city);
        void onCityDelete(City city);
    }

    public CityListAdapter(OnCityActionListener listener) {
        this(listener, false);
    }

    public CityListAdapter(OnCityActionListener listener, boolean showDeleteButton) {
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        City city = cities.get(position);
        holder.bind(city);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    /** 更新城市列表数据 */
    public void updateCities(List<City> newCities) {
        this.cities.clear();
        if (newCities != null) {
            this.cities.addAll(newCities);
        }
        notifyDataSetChanged();
    }

    /** 获取当前列表数据 */
    public List<City> getCities() {
        return new ArrayList<>(cities);
    }

    // ---- ItemMoveCallback.ItemMoveListener 实现 ----

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= cities.size()
                || toPosition < 0 || toPosition >= cities.size()) {
            return;
        }
        Collections.swap(cities, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        if (position < 0 || position >= cities.size()) {
            return;
        }
        City removed = cities.remove(position);
        if (listener != null) {
            listener.onCityDelete(removed);
        }
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMoveFinished() {
        // 拖拽结束的回调，由外部Activity处理保存
    }

    // ---- ViewHolder ----

    class CityViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCityName;
        private final TextView tvCityInfo;
        private final ImageView ivDelete;
        private final ImageView ivDefault;

        CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tvCityInfo = itemView.findViewById(R.id.tv_city_info);
            ivDelete = itemView.findViewById(R.id.iv_city_delete);
            ivDefault = itemView.findViewById(R.id.iv_city_default);
        }

        void bind(City city) {
            tvCityName.setText(city.getCityName());
            String info = "";
            if (city.getCountry() != null && !city.getCountry().isEmpty()) {
                info = city.getCountry();
            }
            // 显示坐标
            info += (info.isEmpty() ? "" : " · ") +
                    String.format("%.2f, %.2f", city.getLatitude(), city.getLongitude());
            tvCityInfo.setText(info);

            // 默认城市标记
            ivDefault.setVisibility(city.isDefault() ? View.VISIBLE : View.GONE);

            // 删除按钮
            if (showDeleteButton) {
                ivDelete.setVisibility(View.VISIBLE);
                ivDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCityDelete(city);
                    }
                });
            } else {
                ivDelete.setVisibility(View.GONE);
            }

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCityClick(city);
                }
            });
        }
    }
}
