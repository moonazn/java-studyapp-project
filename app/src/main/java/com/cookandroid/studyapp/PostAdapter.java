package com.cookandroid.studyapp;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<UploadData> uploadDataList;
    private Context context;

    public PostAdapter(List<UploadData> uploadDataList, Context context) {
        this.uploadDataList = uploadDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        UploadData uploadData = uploadDataList.get(position);
        holder.bind(uploadData);
    }

    @Override
    public int getItemCount() {
        return uploadDataList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView writerTextView;
        private TextView postTitleTextView;
        private TextView postTimeTextView;
        private ImageView photoImageView;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            writerTextView = itemView.findViewById(R.id.writer);
            postTitleTextView = itemView.findViewById(R.id.postTitle);
            postTimeTextView = itemView.findViewById(R.id.postTime);
            photoImageView = itemView.findViewById(R.id.postImage); // ImageView 초기화
        }

        void bind(UploadData uploadData) {
            writerTextView.setText(uploadData.getUser_id() + " 님");
            postTitleTextView.setText(uploadData.getPhoto_title());

            // 업로드 시간을 원하는 형식으로 포맷팅하여 표시 (예: 9월 17일 4:17PM)
            String formattedTime = formatUploadTime(uploadData.getUpload_time());
            postTimeTextView.setText(formattedTime);

            // 이미지를 ImageView에 로드
            if (!TextUtils.isEmpty(uploadData.getPhoto_url())) {
                Glide.with(itemView.getContext())
                        .load(uploadData.getPhoto_url())
                        .into(photoImageView);
            }
        }

        private String formatUploadTime(Long timestamp) {
            // 시간 형식 포맷팅

            if (timestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 h:mm a", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            } else {
                return "";
            }

        }
    }
}
