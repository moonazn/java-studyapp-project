package com.cookandroid.studyapp;

import static android.service.controls.ControlsProviderService.TAG;
import static com.cookandroid.studyapp.MyPageActivity.groupKey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

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

    class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView writerTextView;
        private TextView postTitleTextView;
        private TextView postTimeTextView;
        private ImageView photoImageView;
        private ImageView deleteImageView;
        private UploadData uploadData;

        private ToggleButton heart;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            writerTextView = itemView.findViewById(R.id.writer);
            postTitleTextView = itemView.findViewById(R.id.postTitle);
            postTimeTextView = itemView.findViewById(R.id.postTime);
            photoImageView = itemView.findViewById(R.id.postImage);
            deleteImageView = itemView.findViewById(R.id.delete);
            heart = itemView.findViewById(R.id.heart);


        }

        void bind(UploadData uploadData) {
            this.uploadData = uploadData;
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

            // 현재 사용자의 UID 가져오기
            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group/" + groupKey + "/uploads/" + uploadData.getPost_id());

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String authorUid = dataSnapshot.child("user_id").getValue(String.class);
                        // authorUid에 게시물의 작성자 UID가 저장됩니다.

                        if (currentUserUid.equals(authorUid)) {
                            Log.d(TAG, "bind: 작성자 O");
                            // 현재 사용자가 게시물의 작성자인 경우에만 삭제 버튼 표시
                            deleteImageView.setVisibility(View.VISIBLE);

                            deleteImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                    builder.setTitle("삭제 확인");
                                    builder.setMessage("정말로 게시물을 삭제하시겠습니까?");

                                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // 사용자가 "탈퇴" 버튼을 클릭했을 때 실행할 작업
                                            if (uploadData != null) {
                                                // Firebase Realtime Database에서 게시물 삭제
                                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Group/" + groupKey + "/uploads/" + uploadData.getPost_id());
                                                databaseReference.removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // 게시물 삭제 성공
                                                                // Firebase Storage에서 이미지 파일 삭제
                                                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(uploadData.getPhoto_url());
                                                                storageReference.delete()
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                // 이미지 파일 삭제 성공

                                                                                // RecyclerView에서 해당 아이템을 삭제하고 업데이트
                                                                                int position = uploadDataList.indexOf(uploadData);
                                                                                if (position != -1) {
                                                                                    uploadDataList.remove(position);
                                                                                    notifyItemRemoved(position);
                                                                                }

                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                // 이미지 파일 삭제 실패
                                                                            }
                                                                        });
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // 게시물 삭제 실패
                                                                // 실패에 대한 처리 추가
                                                            }
                                                        });
                                            }
                                        }
                                    });

                                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // 사용자가 "취소" 버튼을 클릭했을 때 실행할 작업 (아무것도 하지 않음)
                                            dialog.dismiss();
                                        }
                                    });

                                    AlertDialog dialog = builder.create();
                                    dialog.show();

                                }

                            });
                        } else {
                            // 현재 사용자가 게시물의 작성자가 아닌 경우 삭제 버튼 숨김
                            deleteImageView.setVisibility(View.GONE);
                            Log.d(TAG, "bind: 작성자 X");
                            Log.d(TAG, "bind: "+currentUserUid+" vs "+authorUid);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (heart.isChecked()) {
                        // ToggleButton이 선택된 상태일 때의 동작
                        // (하트가 선택된 경우)
                        Toast.makeText(context, "하트가 선택되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // ToggleButton이 선택되지 않은 상태일 때의 동작
                        // (하트가 선택되지 않은 경우)
                        Toast.makeText(context, "하트 선택이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });


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
