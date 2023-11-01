package com.cookandroid.studyapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppRecyclerViewAdapter extends RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder> {
    private List<AppInfo> appList;

    public AppRecyclerViewAdapter(List<AppInfo> appList) {
        this.appList = appList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView appNameTextView;
        public ToggleButton toggleButton;
        public ImageView appIconImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            appNameTextView = itemView.findViewById(R.id.appName);
            toggleButton = itemView.findViewById(R.id.appLockToggle);
            appIconImageView = itemView.findViewById(R.id.appIcon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final AppInfo appInfo = appList.get(position);
        holder.appNameTextView.setText(appInfo.getName());
        holder.appIconImageView.setImageDrawable(appInfo.getIcon()); // 앱 아이콘 설정

        // 토글 버튼 상태 설정 및 리스너 추가
        holder.toggleButton.setChecked(appInfo.isSelected());
        holder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("test", "toggle checked");
                appInfo.setUsabled(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }
}
