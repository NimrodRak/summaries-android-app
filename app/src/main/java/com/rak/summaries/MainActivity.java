package com.rak.summaries;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String COMMA_DELIM = ", ";
    private CoursesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    public ArrayList<Course> getRemoteCourses() {
        try {
            InputStream remoteStream = new URL(getApplicationContext().getString(R.string.remote_url))
                    .openConnection()
                    .getInputStream();

            // read line by line the remote and parse them as courses
            Scanner scanner = new Scanner(remoteStream);
            ArrayList<Course> courses = new ArrayList<>();
            while (scanner.hasNext()) {
                String row = scanner.nextLine();
                String[] elems = row.split(COMMA_DELIM);
                courses.add(new Course(
                        elems[1],
                        elems[2],
                        elems[0]
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