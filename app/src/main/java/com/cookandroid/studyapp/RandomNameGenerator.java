package com.cookandroid.studyapp;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RandomNameGenerator {

    public interface RandomNameCallback {
        void onSuccess(String randomName);
        void onError(String errorMessage);
    }

    public static void generateRandomName(RandomNameCallback callback) {
        new RandomNameAsyncTask(callback).execute();
    }

    private static class RandomNameAsyncTask extends AsyncTask<Void, Void, String> {
        private RandomNameCallback callback;

        public RandomNameAsyncTask(RandomNameCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // 네트워크 요청
                String apiUrl = "https://nickname.hwanmoo.kr/?format=json&count=1";
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray wordsArray = jsonResponse.getJSONArray("words");

                if (wordsArray.length() > 0) {
                    return wordsArray.getString(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "RandomUser"; // 실패 시 기본 이름
        }

        @Override
        protected void onPostExecute(String randomName) {
            if (randomName.equals("RandomUser")) {
                callback.onError("네트워크 작업 중 오류 발생");
            } else {
                callback.onSuccess(randomName);
            }
        }
    }
}
