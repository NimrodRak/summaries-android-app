package com.rak.summaries;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String COMMA_DELIMITER = ", ";
    private static final String REMOTE_URL = "https://pastebin.com/raw/HUwDMSfr";
    private static final int HELP_SNACKBAR_DURATION = 15_000;

    private CoursesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.archive_info)).setText(Html.fromHtml(""));

        // set help button listener
        findViewById(R.id.info_button).setOnClickListener(view -> {
            @SuppressLint("WrongConstant") Snackbar snackbar = Snackbar.make(view, R.string.app_help, HELP_SNACKBAR_DURATION);
            TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setMaxLines(10);  // show multiple line
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            snackbar.show();
        });

        // attach adapter
        adapter = new CoursesAdapter(this, new ArrayList<>());
        ((ListView) findViewById(R.id.listView)).setAdapter(adapter);

        // get courses from remote
        Executors.newSingleThreadExecutor().execute(this::fillCourses);
    }

    /**
     * get courses from remote and add them to listview
     */
    private void fillCourses() {
        ArrayList<Course> courses = getRemoteCourses();
        if (courses != null) {
            runOnUiThread(() -> {
                MainActivity.this.adapter.clear();
                MainActivity.this.adapter.addAll(courses);
            });
        }
    }

    /**
     * get courses from remote
     * @return arraylist of courses
     */
    public static ArrayList<Course> getRemoteCourses() {
        try {
            InputStream remoteStream = new URL(REMOTE_URL)
                    .openConnection()
                    .getInputStream();

            // read line by line the remote and parse them as courses
            Scanner scanner = new Scanner(remoteStream);
            ArrayList<Course> courses = new ArrayList<>();
            while (scanner.hasNext()) {
                String row = scanner.nextLine();
                String[] elements = row.split(COMMA_DELIMITER);
                courses.add(new Course(
                        elements[1],
                        elements[2],
                        elements[0],
                        elements[3].equals("1")
                ));
            }
            return courses;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * toast message on UI thread
     * @param message message to toast
     */
    public void toastMessage(String message) {
        runOnUiThread(() -> Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT)
                .show()
        );
    }
}
