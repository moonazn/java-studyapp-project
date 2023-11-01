package com.cookandroid.studyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.app.Activity;
import androidx.annotation.Nullable;

public class RankActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        TextView back = findViewById(R.id.backButton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면 전환을 위한 Intent 생성
                Intent intent = new Intent(RankActivity.this, BoardActivity.class);

                // Intent를 사용하여 화면을 전환합니다.
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        LinearLayout rankingLayout = findViewById(R.id.scrollViewRanking).findViewById(R.id.rankingTexts);

        // Simulated ranking data
        String[] rankingData = {
                "Group4", "Group5",
                "Group6", "Group7", "Group8", "Group9", "Group10",
                "Group11", "Group12", "Group13", "Group14", "Group15",
                "Group16", "Group17", "Group18", "Group19", "Group20"
        };

        for (int i = 0; i < rankingData.length; i++) {
            TextView rankingItem = new TextView(this);
            rankingItem.setText((i + 4) + "위: " + rankingData[i]);
            rankingItem.setTextSize(20);
            rankingLayout.addView(rankingItem);

            if (i < rankingData.length - 1) {
                // Add a divider line for all except the last item
                View divider = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        8
                );
                params.setMargins(
                        0,
                        8,
                        0,
                        8
                );
                divider.setLayoutParams(params);
                divider.setBackgroundColor(000000);
                rankingLayout.addView(divider);
            }
        }
    }
}
