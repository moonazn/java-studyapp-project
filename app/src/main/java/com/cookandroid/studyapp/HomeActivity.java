package com.cookandroid.studyapp;

import static com.cookandroid.studyapp.MyPageActivity.nickname;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements AddGoalDialog.GoalUploadListener, GoalAdapter.GoalClickListener, MemberAdapter.OnMemberClickListener {

    // FirebaseAuth 객체 가져오기
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();
    static String selectedDate;
    DatabaseReference usersRef;

    private MemberAdapter memberAdapter;

    private List<GoalItem> goalItemList;
    private GoalAdapter goalAdapter;

    private TaskAdapter taskAdapter;
    static List<TaskItem> taskItemList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView home = findViewById(R.id.home);
        ImageView board = findViewById(R.id.board);
        ImageView alarm = findViewById(R.id.alarm);
        ImageView myPage = findViewById(R.id.myPage);

        CalendarView calendarViewMonthly = findViewById(R.id.calendarView);
        ImageView plusAdditional1 = findViewById(R.id.plus_additional1);
        ImageView plusAdditional2 = findViewById(R.id.plus_additional2);
        ImageView plusAdditional3 = findViewById(R.id.plus_additional3);

        LinearLayout mycircle = findViewById(R.id.mycircle);

        TextView totalTimeTextView = findViewById(R.id.totalTimeTextView);

        calendarViewMonthly.setFocusedMonthDateColor(Color.parseColor("#5858FA"));

        mycircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uid = currentUser.getUid();
                usersRef = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);

                // 선택한 멤버의 목표 & 할일 목록 가져와서 업데이트
                initRecyclerViewGoals();
                loadAndRefreshTasksForDate(selectedDate);
            }
        });

        // 오늘 날짜로 초기화
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더함
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        selectedDate = year + "-" + month + "-" + dayOfMonth;

        usersRef = FirebaseDatabase.getInstance().getReference("users").child("-"+uid);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("test", "nickname: " + nickname);

                    // 스터디 멤버 리스트 업데이트
                    getGroupMemForUser(nickname);

                } else {
                    // 사용자의 UID에 해당하는 데이터가 없을 때 처리
                    Log.d("test", "DataSnapshot does not exist for UID: " + uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("test", "onCancelled called");
            }
        });

        TextView penaltyCalc = findViewById(R.id.penaltyCalc);


        penaltyCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PenaltyCalcActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                overridePendingTransition(0, R.anim.horizon_enter);
                finish();
            }
        });

        usersRef.child("totalTime").child(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // selectedDate 노드가 존재하는지 확인

                    // 'totalDuration' 값을 가져오기
                    String totalTime = snapshot.child("totalDuration").getValue(String.class);

                    if (totalTime != null) {
                        // 'm'을 기준으로 분리
                        String[] parts = totalTime.split("m");

                        String[] frontParts = parts[0].split(" ");

                        String frontPart = frontParts[0];

                        String middlePart = frontParts[1];

                        // 뒷 부분에서 's'를 기준으로 분리
                        String[] thirdParts = parts[1].split("s");

                        // 뒷 부분 중 숫자 부분
                        String numberPart = thirdParts[0].trim();

                        // 뒷 부분의 숫자가 0보다 크면
                        if (Integer.parseInt(numberPart) > 0) {
                            // 중간 부분 중 숫자 부분을 숫자로 변환하여 1을 늘림
                            int minutes = Integer.parseInt(middlePart.trim());
                            if (minutes < 59) {
                                minutes++; // 1을 늘림
                                middlePart = String.valueOf(minutes);
                            }
                        }

                        // 최종적으로 필요한 문자열 생성
                        String finalResult = frontPart + " " + middlePart + "m";

                        totalTimeTextView.setText(finalResult);
                    } else {
                        // totalTime이 null인 경우에 대한 처리
                        totalTimeTextView.setText("totalTime is null");
                    }
                } else {
                    // selectedDate 노드가 존재하지 않는 경우에 대한 처리
                    totalTimeTextView.setText("0h 0m");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // onCancelled 메서드 내부에 오류 처리 로직 추가
                Log.e("FirebaseError", "Failed to read value.", error.toException());
            }
        });

        calendarViewMonthly.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int dayOfMonth) {
                // 월은 0부터 시작하므로 1을 더해줍니다.
                int adjustedMonth = month + 1;

                // 선택된 날짜의 정보를 문자열로 만듭니다.
                selectedDate = year + "-" + adjustedMonth + "-" + dayOfMonth;

                taskItemList.clear(); // Clear existing data
                loadAndRefreshTasksForDate(selectedDate);

                // 로그 추가: 선택된 날짜와 리스트 크기 확인
                Log.d("HomeActivity", "Selected Date: " + selectedDate);
                Log.d("HomeActivity", "TaskItemList Size: " + taskItemList.size());

                usersRef.child("totalTime").child(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // selectedDate 노드가 존재하는지 확인

                            // 'totalDuration' 값을 가져오기
                            String totalTime = snapshot.child("totalDuration").getValue(String.class);

                            if (totalTime != null) {
                                // 'm'을 기준으로 분리
                                String[] parts = totalTime.split("m");

                                String[] frontParts = parts[0].split(" ");

                                String frontPart = frontParts[0];

                                String middlePart = frontParts[1];

                                // 뒷 부분에서 's'를 기준으로 분리
                                String[] thirdParts = parts[1].split("s");

                                // 뒷 부분 중 숫자 부분
                                String numberPart = thirdParts[0].trim();

                                // 뒷 부분의 숫자가 0보다 크면
                                if (Integer.parseInt(numberPart) > 0) {
                                    // 중간 부분 중 숫자 부분을 숫자로 변환하여 1을 늘림
                                    int minutes = Integer.parseInt(middlePart.trim());
                                    if (minutes < 59) {
                                        minutes++; // 1을 늘림
                                        middlePart = String.valueOf(minutes);
                                    }
                                }

                                // 최종적으로 필요한 문자열 생성
                                String finalResult = frontPart + " " + middlePart + "m";

                                totalTimeTextView.setText(finalResult);
                            } else {
                                // totalTime이 null인 경우에 대한 처리
                                totalTimeTextView.setText("totalTime is null");
                            }
                        } else {
                            // selectedDate 노드가 존재하지 않는 경우에 대한 처리
                            totalTimeTextView.setText("0h 0m");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // onCancelled 메서드 내부에 오류 처리 로직 추가
                        Log.e("FirebaseError", "Failed to read value.", error.toException());
                    }
                });

                plusAdditional1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTaskPopupDialog();
                    }
                });

            }
        });

        plusAdditional2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 다이얼로그 인스턴스 생성
                AddGoalDialog addGoalDialog = new AddGoalDialog(HomeActivity.this, HomeActivity.this);

                // 다이얼로그 표시
                addGoalDialog.show();
            }
        });

        plusAdditional3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, StopwatchActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                overridePendingTransition(0, R.anim.horizon_enter);
                finish();
            }
        });

// RecyclerView 초기화
        RecyclerView recyclerViewGoals = findViewById(R.id.recyclerViewGoals);
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        goalItemList = new ArrayList<>();
        // 어댑터 설정
        goalAdapter = new GoalAdapter(this, goalItemList);
        goalAdapter.setGoalClickListener(new GoalAdapter.GoalClickListener() {
            @Override
            public void onGoalClicked(int position) {
                // 목표를 클릭했을 때 수행할 동작 구현
                showGoalEditPopup(goalItemList.get(position));
            }
        });
        recyclerViewGoals.setAdapter(goalAdapter);

        // CustomSnapHelper 추가
        CustomSnapHelper customSnapHelper = new CustomSnapHelper();
        customSnapHelper.attachToRecyclerView(recyclerViewGoals);

        // 목표 데이터 로딩 및 갱신
        initRecyclerViewGoals();

// RecyclerView 초기화 및 어댑터 설정
        RecyclerView recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskItemList = new ArrayList<>();

        taskAdapter = new TaskAdapter(taskItemList, this);
        recyclerViewTasks.setAdapter(taskAdapter);

        loadAndRefreshTasksForDate(selectedDate);

        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final TaskItem deletedItem = taskItemList.get(position);

                // Remove the item from the RecyclerView
                taskItemList.remove(position);
                taskAdapter.notifyItemRemoved(position);

                // Remove the item from the database
                removeTaskFromDatabase(deletedItem);
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewTasks);

        home.setAlpha(1f);

        home.setOnClickListener(v -> {
            if (!getClass().equals(HomeActivity.class)) {
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AlarmActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        myPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MyPageActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });



    }

    public void onAdditionalClick(View view) {
        showTaskPopupDialog();
    }

    // 아래에 onAdditionalWatchClick 및 onAdditionalTimerClick 메소드를 추가
    public void onAdditionalGoalClick(View view) {
        uid = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);

        // 다이얼로그 인스턴스 생성
        AddGoalDialog addGoalDialog = new AddGoalDialog(HomeActivity.this, HomeActivity.this);

        // 콜백 설정
        addGoalDialog.setGoalUploadListener(HomeActivity.this);

        // 다이얼로그 표시
        addGoalDialog.show();
    }

    public void onAdditionalTimerClick(View view) {
        Intent intent = new Intent(HomeActivity.this, StopwatchActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        overridePendingTransition(0, R.anim.horizon_enter);
        finish();
    }

    // 데이터 로딩을 완료한 후 RecyclerView를 초기화
    private void initRecyclerView(List<String> memberList) {
        RecyclerView memberRecyclerView = findViewById(R.id.memberRecyclerView);

        if (memberList.isEmpty()) {
            // 데이터가 비어있는 경우에 대한 처리
            // 예를 들어, 사용자에게 메시지를 표시할 수 있습니다.
        } else {
            // 데이터가 준비된 경우 어댑터를 생성하고 연결
            memberAdapter = new MemberAdapter(memberList);
            memberRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.HORIZONTAL, false));
            memberRecyclerView.setAdapter(memberAdapter);

            // 클릭 리스너 설정
            memberAdapter.setOnMemberClickListener(HomeActivity.this);
        }
    }
    @Override
    public void onMemberClick(String nickname) {
        // 클릭된 멤버의 UID를 찾아서 uid 변수에 설정
        findUidByNickname(nickname);
    }

    private void findUidByNickname(String nickname) {
        DatabaseReference usersRef2 = FirebaseDatabase.getInstance().getReference("users");

        usersRef2.orderByChild("nickname").equalTo(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 닉네임으로 찾은 데이터가 존재하면 uid 변수에 해당 UID 설정
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String foundUid = userSnapshot.getKey();
                        uid = foundUid;
                        usersRef = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);

                        // 선택한 멤버의 목표 & 할일 목록 가져와서 업데이트
                        initRecyclerViewGoals();
                        loadAndRefreshTasksForDate(selectedDate);

                        Log.d("HomeActivity", "Found UID for nickname " + nickname + ": " + uid);
                        break;  // 하나만 찾으면 루프 종료
                    }
                } else {
                    Log.d("HomeActivity", "No user found for nickname: " + nickname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeActivity", "Error finding UID for nickname: " + nickname, databaseError.toException());
            }
        });
    }

    private void getGroupMemForUser(String currentUserNickname) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Group");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    DataSnapshot membersSnapshot = groupSnapshot.child("members");

                    if (membersSnapshot.hasChild(currentUserNickname)) {

                        // 멤버 목록을 가져온 후 RecyclerView 초기화
                        List<String> memberList = new ArrayList<>();
                        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
                            String memberName = memberSnapshot.getKey();
                            memberList.add(memberName);
                        }
                        initRecyclerView(memberList);
                        break; // 그룹을 찾았으면 루프 종료
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
            }
        });
    }

    // 목표 데이터 로딩 및 갱신 메서드
    private void initRecyclerViewGoals() {
        DatabaseReference goalsRef = usersRef.child("goals");

        goalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalItemList.clear(); // Clear existing data

                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    String goalName = goalSnapshot.child("goalName").getValue(String.class);
                    String targetDateStr = goalSnapshot.child("target_date").getValue(String.class);
                    int targetHour = goalSnapshot.child("hour").getValue(Integer.class);
                    int targetMinute = goalSnapshot.child("minute").getValue(Integer.class);

                    // 디데이와 남은 시간 갱신
                    int dday = updateDdayAndRemainingTime(targetDateStr, targetHour, targetMinute);
                    String goalId = goalSnapshot.getKey();

                    // Create a GoalItem object and add it to the list
                    GoalItem goalItem = new GoalItem(goalId, goalName, String.valueOf(dday), targetDateStr, targetHour, targetMinute);
                    goalItemList.add(goalItem);
                }

                // Notify the adapter that the data has changed
                goalAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("HomeActivity", "Error loading goals", databaseError.toException());
            }
        });
    }
    // 디데이와 남은 시간 갱신 메서드
    private int updateDdayAndRemainingTime(String targetDateStr, int targetHour, int targetMinute) {

        int dday = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());

        try {
            // 목표의 날짜와 시간을 Calendar 객체로 변환
            Calendar targetCalendar = Calendar.getInstance();
            targetCalendar.setTime(dateFormat.parse(targetDateStr));
            targetCalendar.set(Calendar.HOUR_OF_DAY, targetHour);
            targetCalendar.set(Calendar.MINUTE, targetMinute);
            targetCalendar.set(Calendar.SECOND, 0);

            // 현재 날짜와 시간을 Calendar 객체로 변환
            Calendar currentCalendar = Calendar.getInstance();

            // 디데이 계산
            long ddayMillis = targetCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();
            dday = (int) (ddayMillis / (24 * 60 * 60 * 1000));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dday;
    }

    private void showTaskPopupDialog() {
        uid = currentUser.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users").child("-" + uid);

        TaskPopupDialog taskPopupDialog = new TaskPopupDialog(
                HomeActivity.this,
                usersRef,
                selectedDate,
                taskAdapter
        );

        taskPopupDialog.show();
    }
    // 통합된 데이터 로딩 및 갱신 메서드
    private void loadAndRefreshTasksForDate(String selectedDate) {
        DatabaseReference tasksRef = usersRef.child("date").child(selectedDate);

        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskItemList.clear(); // Clear existing data

                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    String taskName = taskSnapshot.child("taskName").getValue(String.class);
                    Boolean isCompletedObject = taskSnapshot.child("isCompleted").getValue(Boolean.class);
                    boolean isCompleted = (isCompletedObject != null) ? isCompletedObject.booleanValue() : false;

                    // 새로운 데이터의 고유한 post_id를 설정
                    String task_id = taskSnapshot.getKey();
                    Log.d("HomeActivity", "taskID: " + task_id);

                    // Create a TaskItem object and add it to the list
                    TaskItem taskItem = new TaskItem(task_id, taskName, isCompleted);
                    taskItemList.add(taskItem);
                }

                // Notify the adapter that the data has changed
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("HomeActivity", "Error loading tasks for date: " + selectedDate, databaseError.toException());
            }
        });
    }
    private void removeTaskFromDatabase(TaskItem deletedItem) {
        // Get the task ID from the deleted item
        String taskID = deletedItem.getTaskId();

        // Remove the task from the database
        usersRef.child("date").child(selectedDate).child(taskID).removeValue();

// Log 추가: 정상적으로 taskItemList에서 아이템이 제거되었는지 확인
        Log.d("HomeActivity", "Removing item from taskItemList with taskID: " + taskID);

        // Remove the item from taskItemList
        taskItemList.remove(deletedItem);
        taskAdapter.notifyDataSetChanged(); // 데이터 변경을 어댑터에 알림
    }
    @Override
    public void onGoalUploaded() {
        // 목표가 업로드되었을 때 호출되는 메소드
        initRecyclerViewGoals();

    }

    // GoalClickListener의 메서드 구현
    @Override
    public void onGoalClicked(int position) {
        GoalItem goalItem = goalItemList.get(position);
        showGoalEditPopup(goalItem);
    }

    private void showGoalEditPopup(GoalItem goalItem) {
        GoalEditPopupDialog goalEditPopupDialog = new GoalEditPopupDialog(this, goalItem, goalAdapter, goalItemList.indexOf(goalItem));
        goalEditPopupDialog.show();
    }


}