package com.notts.MindMate;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SubmissionActivity extends AppCompatActivity {

    EditText feeling;
    TextView feelingError;
    TextView descError;
    EditText description;
    RadioGroup emotionGroup;
    TextView moodError;
    Integer butIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);
    }

    public void back(View view){
        Intent calendarIntent = new Intent(this, CalendarActivity.class);
        startActivity(calendarIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void submit(View view) throws IOException {

        feeling = findViewById(R.id.FeelingBox);
        description = findViewById(R.id.DescriptionBox);
        feelingError = findViewById(R.id.error1);
        descError = findViewById(R.id.error2);
        emotionGroup = findViewById(R.id.MoodButtons);
        moodError = findViewById(R.id.error3);

        if (feeling.getText().toString().isEmpty()){
            feelingError.setText("Cannot be empty");
        } else
            feelingError.setText("");

        if (description.getText().toString().isEmpty()){
            descError.setText("Cannot be empty");
        } else
            descError.setText("");

        if (emotionGroup.getCheckedRadioButtonId() == -1){
            moodError.setText("Must choose one");
        } else {
            moodError.setText("");
            butIndex = emotionGroup.indexOfChild(findViewById(emotionGroup.getCheckedRadioButtonId()));
        }

        if (!feeling.getText().toString().isEmpty() && !description.getText().toString().isEmpty() && emotionGroup.getCheckedRadioButtonId() != -1) {

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String date = df.format(c);

            String data = feeling.getText().toString() + "," + description.getText().toString() + "," + butIndex + "," + date + ";";


            File file = new File("WellbeingFile.txt");

            if (!file.exists()){
                try {
                    file.createNewFile();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            String filename = "WellbeingFile.txt";

            FileHandling fileHandling = new FileHandling(SubmissionActivity.this);

            fileHandling.writeFile(this, filename, data);

            Intent calendarIntent = new Intent(this, CalendarActivity.class);
            startActivity(calendarIntent);
        }
    }
}
