
package com.rak.summaries;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String COMMA_DELIMITER = ", ";
    private static final String REMOTE_URL = "https://pastebin.com/raw/HUwDMSfr";
    private static final int HELP_SNACKBAR_DURATION = 15_000;
    private AnimatedExpandableListView listView;
    private ExampleAdapter adapter;
    ArrayList<Course> courses;
    private final ArrayDeque<Integer> previousGroups = new ArrayDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (AnimatedExpandableListView) findViewById(R.id.listView);
        adapter = new ExampleAdapter(this);


        listView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            // if closing tab, close it and remove it from the to be removed list
            if (listView.isGroupExpanded(groupPosition)) {
                listView.collapseGroupWithAnimation(groupPosition);
                previousGroups.removeFirstOccurrence(groupPosition);
                // if opening, close all other ones
            } else {

                listView.expandGroupWithAnimation(groupPosition);
                while (!previousGroups.isEmpty()) {
                    listView.collapseGroupWithAnimation(Objects.requireNonNull(previousGroups.peek()));
                    previousGroups.pop();
                }
                previousGroups.push(groupPosition);
            }

            return true;
        });

        // set help button listener
        findViewById(R.id.info_button).setOnClickListener(view -> {
            @SuppressLint("WrongConstant") Snackbar snackbar = Snackbar.make(view, R.string.app_help, HELP_SNACKBAR_DURATION);
            TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setMaxLines(10);  // show multiple line
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            snackbar.show();
        });

        // get courses from remote
        Executors.newSingleThreadExecutor().execute(this::generateAdapter);
    }

    private void generateAdapter() {
        courses = getRemoteCourses();
        List<GroupItem> items = new ArrayList<>(Objects.requireNonNull(courses).size());
        // Populate our list with groups and it's children
        for (int i = 0, coursesSize = courses.size(); i < coursesSize; i++) {
            Course course = courses.get(i);
            GroupItem item = new GroupItem();

            item.title = course.getName();
            item.items.add(new ChildItem(course.getUrl(), course.getName(), course.getNumber()));

            items.add(item);

            getSharedPreferences(getString(R.string.shared_prefs_courses_list), Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(course.getNumber() + "2", course.getActive())
                    .apply();
        }

        adapter.setData(items);
        runOnUiThread(() -> {
            listView.setAdapter(adapter);


        });
    }


    /**
     * get courses from remote
     *
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
     *
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

    private static class GroupItem {
        String title;
        List<ChildItem> items = new ArrayList<>();
    }

    private static class ChildItem {
        String link;
        String title;
        String number;

        public ChildItem(String link, String title, String number) {
            this.link = link;
            this.title = title;
            this.number = number;
        }
    }

    private static class ChildHolder {
        ImageButton courseOpen;
        ImageButton courseLink;
        ImageButton courseSub;
    }

    private static class GroupHolder {
        TextView title;
    }

    private class ExampleAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
        private final LayoutInflater inflater;
        private List<GroupItem> items;
        private int prev;

        public ExampleAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<GroupItem> items) {
            this.items = items;
        }

        @Override
        public ChildItem getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            ChildItem item = getChild(groupPosition, childPosition);
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder.courseOpen = (ImageButton) convertView.findViewById(R.id.courseOpen);
                holder.courseLink = (ImageButton) convertView.findViewById(R.id.courseLink);
                holder.courseSub = (ImageButton) convertView.findViewById(R.id.courseSub);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }

            holder.courseOpen.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(item.link));

                MainActivity.this.startActivity(intent);
            });
            holder.courseLink.setOnClickListener(view -> {
                // copy url to clipboard
                ((ClipboardManager) MainActivity.this
                        .getSystemService(Context.CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newPlainText(
                                getApplicationContext().getString(R.string.url_clipboard_label),
                                item.link));
                ((MainActivity) MainActivity.this).runOnUiThread(
                        () -> MainActivity.this
                                .toastMessage(getApplicationContext()
                                        .getString(R.string.clipboard_copied_toast_message)));
            });
            holder.courseSub.setBackgroundTintList(getColorStateList(isSubbed(item)
                    ? R.color.activated_sub : R.color.deactivated_sub));
            holder.courseSub.setVisibility(isActive(item)
                    ? View.VISIBLE : View.GONE);

            holder.courseSub.setOnClickListener(view -> {
                boolean isSubscribed = isSubbed(item);
                holder.courseSub.setBackgroundTintList(getColorStateList(
                        !isSubscribed ? R.color.activated_sub : R.color.deactivated_sub));
                if (isSubscribed) {
                    ((TextView) parent.getChildAt(groupPosition).findViewById(R.id.textTitle)).setTextColor(prev);
                } else {

                    ((TextView) parent.getChildAt(groupPosition).findViewById(R.id.textTitle)).setTextColor(getResources().getColor(R.color.activated_sub, getTheme()));
                }
                getSharedPreferences(getString(R.string.shared_prefs_courses_list), Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(item.number, !isSubscribed)
                        .apply();
                notifyDataSetChanged();
            });
            notifyDataSetChanged();


            return convertView;
        }

        private boolean isActive(ChildItem item) {
            return getSharedPreferences(
                    getString(R.string.shared_prefs_courses_list), Context.MODE_PRIVATE)
                    .getBoolean(item.number + "2", false);
        }

        private boolean isSubbed(ChildItem item) {
            return getSharedPreferences(
                    getString(R.string.shared_prefs_courses_list), Context.MODE_PRIVATE)
                    .getBoolean(item.number, false);
        }


        @Override
        public int getRealChildrenCount(int groupPosition) {
            return items.get(groupPosition).items.size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            GroupItem item = getGroup(groupPosition);
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.group_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.textTitle);

                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }

            holder.title.setText(item.title);
            prev = holder.title.getCurrentTextColor();
            if (isSubbed(getChild(groupPosition, 0))) {
                holder.title.setTextColor(getResources().getColor(R.color.activated_sub, getTheme()));
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

    }
}
