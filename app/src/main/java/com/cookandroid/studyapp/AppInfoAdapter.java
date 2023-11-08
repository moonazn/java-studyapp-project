package com.cookandroid.studyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.util.List;



public class AppInfoAdapter extends ArrayAdapter<AppInfo> {
    private List<AppInfo> appInfoList;

    public AppInfoAdapter(Context context, int resource, List<AppInfo> appInfoList) {
        super(context, resource, appInfoList);
        this.appInfoList = appInfoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_list_item, parent, false);
        }

        AppInfo appInfo = getItem(position);

        if (appInfo != null) {
            ImageView appIcon = convertView.findViewById(R.id.appIcon);
            TextView appName = convertView.findViewById(R.id.appName);
            ImageView appSelect = convertView.findViewById(R.id.appSelect);

            appIcon.setImageDrawable(appInfo.getIcon());
            appName.setText(appInfo.getName());
            appSelect.setImageResource(appInfo.getAppSelectImage()); // 이미지 리소스 설정

        }

        return convertView;
    }
}
