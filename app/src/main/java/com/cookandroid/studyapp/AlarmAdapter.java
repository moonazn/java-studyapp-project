package com.cookandroid.studyapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adapter 클래스 추가
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private List<AlarmActivity.AlarmInfo> alarmList;

    public AlarmAdapter(List<AlarmActivity.AlarmInfo> alarmList) {
        this.alarmList = alarmList;
    }

    public void updatedata(List<AlarmActivity.AlarmInfo> alarmList){
        this.alarmList = alarmList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AlarmActivity.AlarmInfo alarm = alarmList.get(position);
        holder.alarmTextView.setText("알람 시간 : " + alarm.getHour() + "시 " + alarm.getMinute() + "분"+"\n" +"미션 : "+alarm.getMission());

    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView alarmTextView;
        public Button deleteButton;

        public ViewHolder(View view) {
            super(view);
            alarmTextView = view.findViewById(R.id.alarmTextView);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}