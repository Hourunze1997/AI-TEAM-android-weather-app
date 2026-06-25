package com.example.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.model.Forecast;
import com.example.weatherapp.util.WeatherIconUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 天气预报列表适配器。
 * 在主页 RecyclerView 中展示未来天气预报项。
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<Forecast> forecastList = new ArrayList<>();

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        Forecast forecast = forecastList.get(position);
        holder.bind(forecast);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    /** 更新数据并刷新 */
    public void updateData(List<Forecast> newData) {
        this.forecastList.clear();
        if (newData != null) {
            this.forecastList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    /** 静态ViewHolder */
    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final ImageView ivIcon;
        private final TextView tvCondition;
        private final TextView tvMaxTemp;
        private final TextView tvMinTemp;

        ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_forecast_date);
            ivIcon = itemView.findViewById(R.id.iv_forecast_icon);
            tvCondition = itemView.findViewById(R.id.tv_forecast_condition);
            tvMaxTemp = itemView.findViewById(R.id.tv_forecast_max_temp);
            tvMinTemp = itemView.findViewById(R.id.tv_forecast_min_temp);
        }

        void bind(Forecast forecast) {
            tvDate.setText(forecast.getShortDate());
            ivIcon.setImageResource(WeatherIconUtil.getIconResource(forecast.getIconCode()));
            tvCondition.setText(WeatherIconUtil.getConditionText(forecast.getCondition()));
            tvMaxTemp.setText(forecast.getFormattedMaxTemp());
            tvMinTemp.setText(forecast.getFormattedMinTemp());
        }
    }
}
