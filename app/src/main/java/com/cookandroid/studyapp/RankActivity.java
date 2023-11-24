package com.cookandroid.studyapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RankActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RankAdapter rankAdapter;
    private List<RankItem> rankItemList;

    TextView firstName, firstTotal, secondName, secondTotal, thirdName, thirdTotal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                overridePendingTransition(0, R.anim.horizon_exit);
                finish();
            }
        });

        firstName = findViewById(R.id.firstName);
        firstTotal = findViewById(R.id.firstTotal);
        secondName = findViewById(R.id.secondName);
        secondTotal = findViewById(R.id.secondTotal);
        thirdName = findViewById(R.id.thirdName);
        thirdTotal = findViewById(R.id.thirdTotal);

        // 데이터베이스에 존재하는 모든 그룹을 확인하고 총 공부시간을 가져옵니다.
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Group");
        groupsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot groupsSnapshot) {
                if (groupsSnapshot.exists()) {
                    for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                        String groupName = groupSnapshot.child("groupName").getValue(String.class);
                        Log.d("groupName", groupName + "");


                        // 각 그룹별로 총 공부시간을 가져옵니다.
                        getWeeklyTotalStudyTimeForGroup(groupName, new OnWeeklyTotalStudyTimeCalculatedListener() {

                            @Override
                            public void onWeeklyTotalStudyTimeCalculated(int groupWeeklyStudyTime) {
                                Log.d("addRankedGroupToList", String.valueOf(groupsSnapshot.getChildren()));
                                addRankedGroupToList(groupName, groupWeeklyStudyTime, groupsSnapshot.getChildrenCount());
                            }
                        });
                    }
                } else {
                    // 그룹 데이터가 없는 경우 처리
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터를 가져오는 중 에러가 발생한 경우 처리
            }
        });

        // 리사이클러뷰 설정
        recyclerView = findViewById(R.id.fromForthRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rankItemList = new ArrayList<>();

        // 어댑터 설정
        rankAdapter = new RankAdapter(rankItemList);
        recyclerView.setAdapter(rankAdapter);
    }

    // 인터페이스 정의
    interface OnWeeklyTotalStudyTimeCalculatedListener {
        void onWeeklyTotalStudyTimeCalculated(int weeklyTotalStudyTime);
    }

    private void getWeeklyTotalStudyTimeForGroup(String groupName, OnWeeklyTotalStudyTimeCalculatedListener onWeeklyTotalStudyTimeCalculatedListener) {

        DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("Group");

        // 현재 날짜를 얻어오는 코드 (예: 2023-11-22)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // 지난 7일 동안의 날짜들을 계산
        List<String> last7DaysDates = getLast7DaysDates(currentDate);

        groupReference.orderByChild("groupName").equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot groupSnapshot) {
                if (groupSnapshot.exists()) {
                    int totalDuration = 0;
                    DataSnapshot totalTimeSnapshot = groupSnapshot.child("totalTime");
                    Log.d("totalTimeSnapshot", String.valueOf(totalTimeSnapshot.exists()));
//                    if (groupSnapshot.child("totalTime").exists()){
//
//                    } else {
//                        onWeeklyTotalStudyTimeCalculatedListener.onWeeklyTotalStudyTimeCalculated(0);
//                    }

                    // 각 날짜 노드를 순회하면서 해당 날짜의 총 공부시간을 더합니다.
                    for (String date : last7DaysDates) {
                        DataSnapshot dateSnapshot = totalTimeSnapshot.child(date);
                        Log.d("for (String date : ", date);
                        Log.d("for (String date : ", String.valueOf(groupSnapshot.child("totalTime").getChildrenCount()));

                        Log.d("Date Snapshot", "Date: " + date + ", Exists: " + dateSnapshot.exists());

                        if (dateSnapshot.exists()) {
                            Log.d("Total Duration", "Date: " + date + ", Total Duration: " + dateSnapshot.child("totalDuration").getValue(String.class));
                            totalDuration += parseDurationInSeconds(dateSnapshot.child("totalDuration").getValue(String.class));
                        }
                    }
                    // 계산이 완료되면 리스너를 통해 결과를 전달
                    onWeeklyTotalStudyTimeCalculatedListener.onWeeklyTotalStudyTimeCalculated(totalDuration);
                    Log.d("RankActivity", "Weekly total study time calculated for group: " + groupName);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // 지난 7일 동안의 날짜 목록을 얻어오는 메서드
    private List<String> getLast7DaysDates(String currentDate) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date startDate = dateFormat.parse(currentDate);
            Calendar calendar = Calendar.getInstance();

            for (int i = 0; i < 7; i++) {
                calendar.setTime(startDate);
                Log.d("calendar.setTime", String.valueOf(startDate));

                calendar.add(Calendar.DAY_OF_YEAR, -i);
                String formattedDate = dateFormat.format(calendar.getTime());
                dates.add(formattedDate);
                Log.d("formattedDate", formattedDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("getLast7DaysDates", String.valueOf(dates));
        return dates;
    }

    // 사용자의 일주일 내 총 공부시간을 가져오는 메서드
    // 이 메서드는 비동기로 동작하므로 결과를 받기 위해 콜백을 사용합니다.
//    private void getWeeklyTotalStudyTimeForGroup(String groupName) {
//        DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("Group");
//
//        groupReference.orderByChild("groupName").equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot groupSnapshot) {
//                if (groupSnapshot.exists()) {
//                    // 그룹이 존재하면 그룹 내의 모든 멤버를 가져옵니다.
//                    DataSnapshot membersSnapshot = groupSnapshot.getChildren().iterator().next().child("members");
//
//                    // 각 멤버의 주간 총 공부시간을 가져와 합산합니다.
//                    final int[] totalGroupWeeklyStudyTime = {0};
//
//                    for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
//                        String memberNickname = memberSnapshot.getKey();
//
//                        // 각 멤버의 닉네임을 사용하여 users 노드에서 해당 멤버의 UID를 찾습니다.
//                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");
//                        userReference.orderByChild("nickname").equalTo(memberNickname).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot userSnapshot) {
//                                if (userSnapshot.exists()) {
//                                    // 각 멤버에 대해 주간 총 공부시간을 가져오고 합산합니다.
//                                    String memberUid = userSnapshot.getChildren().iterator().next().getKey();
//                                    DatabaseReference memberReference = FirebaseDatabase.getInstance().getReference("users").child(memberUid);
//
//                                    memberReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                            int memberWeeklyStudyTime = calculateWeeklyTotalStudyTime(snapshot);
//                                            totalGroupWeeklyStudyTime[0] += memberWeeklyStudyTime;
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError error) {
//                                            // 에러 처리
//                                        }
//                                    });
//
//                                    // 여기에서 totalGroupWeeklyStudyTime을 사용하거나, UI 업데이트 등을 수행할 수 있습니다.
//                                } else {
//                                    // 사용자 데이터가 없는 경우 처리
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//                                // 데이터를 가져오는 중 에러가 발생한 경우 처리
//                            }
//                        });
//                    }
//
//                    // 총 공부시간이 가장 많은 그룹을 리스트에 추가
//                    addRankedGroupToList(groupName, totalGroupWeeklyStudyTime[0], groupSnapshot.getChildrenCount());
//                } else {
//                    // 그룹 데이터가 없는 경우 처리
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // 데이터를 가져오는 중 에러가 발생한 경우 처리
//            }
//        });
//    }

    // 정렬된 그룹 정보로 UI 업데이트
    private void addRankedGroupToList(String groupName, int totalGroupWeeklyStudyTime, long totalGroupsCount) {
        // 총 공부시간이 가장 많은 그룹을 리스트에 추가
        rankItemList.add(new RankItem(groupName, totalGroupWeeklyStudyTime, totalGroupsCount));

        Log.d("rankItemList", String.valueOf(rankItemList.size()));
        Log.d("rankItemList", String.valueOf(totalGroupsCount));

        // 모든 그룹의 총 공부시간을 확인했을 때 UI 업데이트 수행
        if (rankItemList.size() == totalGroupsCount) {
            // 총 공부시간이 많은 순으로 정렬
            Collections.sort(rankItemList, new Comparator<RankItem>() {
                @Override
                public int compare(RankItem group1, RankItem group2) {
                    return Integer.compare(group2.getTotalWeeklyStudyTime(), group1.getTotalWeeklyStudyTime());
                }
            });
            updateUIWithRankedGroups(rankItemList);
            Log.d("rankItemList", String.valueOf(rankItemList.size()));


//            // 정렬된 순서대로 UI에 표시
//            if (rankItemList.size() >= 3) {
//                // 리스트가 3개 이상인 경우에만 subList 호출
//                updateUIWithRankedGroups(rankItemList.subList(0, 3));
//            } else {
//                // 그렇지 않은 경우 전체 리스트를 전달
//                updateUIWithRankedGroups(rankItemList);
//            }

        }
    }

    // 정렬된 그룹 정보로 UI 업데이트
    private void updateUIWithRankedGroups(List<RankItem> rankedGroups) {
        // RecyclerView에 데이터를 설정하기 전에 먼저 UI 상위 3개 그룹 정보를 설정합니다.
        if (!rankedGroups.isEmpty()) {
            RankItem firstGroup = rankedGroups.get(0);
            firstName.setText(firstGroup.getGroupName());
            Log.d("rankItemList", firstGroup.getGroupName());

            firstTotal.setText(String.valueOf(firstGroup.getTotalWeeklyStudyTime()));
        } else {
            // 리스트 크기가 0인 경우 1~3위를 비웁니다.
            firstName.setText("");
            firstTotal.setText("");
            secondName.setText("");
            secondTotal.setText("");
            thirdName.setText("");
            thirdTotal.setText("");
        }

        if (rankedGroups.size() > 1) {
            RankItem secondGroup = rankedGroups.get(1);
            secondName.setText(secondGroup.getGroupName());
            secondTotal.setText(String.valueOf(secondGroup.getTotalWeeklyStudyTime()));
        } else {
            // 리스트 크기가 1인 경우 2~3위를 비웁니다.
            secondName.setText("");
            secondTotal.setText("");
            thirdName.setText("");
            thirdTotal.setText("");
        }

        if (rankedGroups.size() > 2) {
            RankItem thirdGroup = rankedGroups.get(2);
            thirdName.setText(thirdGroup.getGroupName());
            thirdTotal.setText(String.valueOf(thirdGroup.getTotalWeeklyStudyTime()));
        } else {
            // 리스트 크기가 2인 경우 3위를 비웁니다.
            thirdName.setText("");
            thirdTotal.setText("");
        }

        // 상위 3개 그룹을 리스트에서 제거합니다.
        if (rankedGroups.size() >= 3) {
            rankedGroups.remove(0);
            rankedGroups.remove(0);
            rankedGroups.remove(0);
        } else {
            // 상위 3개 미만의 그룹이 있다면 나머지는 비웁니다.
            rankedGroups.clear();
        }

        // 나머지 그룹을 RecyclerView에 설정합니다.
        int endIndex = Math.min(rankedGroups.size(), 17);
        List<RankItem> recyclerViewData = new ArrayList<>(rankedGroups.subList(0, endIndex));

        // 어댑터에 데이터 설정
        rankAdapter.setData(recyclerViewData);
        rankAdapter.notifyDataSetChanged();
    }





    // 시간 문자열을 파싱하여 초로 변환하는 메서드
    private int parseDurationInSeconds(String durationAsString) {
        String[] timeUnits = durationAsString.split("\\s+");

        int hours = 0, minutes = 0, seconds = 0;

        for (String unit : timeUnits) {
            if (unit.endsWith("h")) {
                hours = Integer.parseInt(unit.replace("h", ""));
            } else if (unit.endsWith("m")) {
                minutes = Integer.parseInt(unit.replace("m", ""));
            } else if (unit.endsWith("s")) {
                seconds = Integer.parseInt(unit.replace("s", ""));
            }
        }

        // 시, 분, 초를 초 단위로 변환하여 반환
        return hours * 3600 + minutes * 60 + seconds;
    }

    // 사용자에게 보여줄 데이터 클래스
    static class RankItem extends com.cookandroid.studyapp.RankItem {
        private String groupName;
        private int totalWeeklyStudyTime;
        private long totalGroupsCount;

        public RankItem(String groupName, int totalWeeklyStudyTime, long totalGroupsCount) {
            this.groupName = groupName;
            this.totalWeeklyStudyTime = totalWeeklyStudyTime;
            this.totalGroupsCount = totalGroupsCount;
        }

        public String getGroupName() {
            return groupName;
        }

        public int getTotalWeeklyStudyTime() {
            return totalWeeklyStudyTime;
        }

        public long getTotalGroupsCount() {
            return totalGroupsCount;
        }
    }
}
