package com.rak.summaries;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class CoursesAdapter extends ArrayAdapter<Course> {
    public CoursesAdapter(Context context, ArrayList<Course> courses) {
        super(context, 0, courses);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Course course = getItem(position);
        // generate the layout
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.course_row, parent, false);
        }
        applyCourseDetails(course, convertView);
        // set on (long) click listeners for text view
        convertView.findViewById(R.id.textView).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(course.getUrl()));
            getContext().startActivity(intent);
        });
        convertView.findViewById(R.id.textView).setOnLongClickListener(view -> {
            // copy url to clipboard
            ((ClipboardManager) getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText(
                            getContext().getString(R.string.url_clipboard_label),
                            course.getUrl()));
            ((MainActivity) getContext()).runOnUiThread(
                    () -> ((MainActivity) getContext()).toastMessage(getContext().getString(R.string.clipboard_copied_toast_message)));
            return true;
        });
        // set on (un)subscribe when switch fires
        ((SwitchMaterial) convertView.findViewById(R.id.switch1)).setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                // subscribe from FCM topic
                FirebaseMessaging.getInstance().subscribeToTopic(course.getNumber());
                // remove subscription from preferences
                getContext().getSharedPreferences(getContext()
                        .getString(R.string.shared_prefs_courses_list), Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(course.getNumber(), true)
                        .apply();
            } else {
                // unsubscribe from FCM topic
                FirebaseMessaging.getInstance().unsubscribeFromTopic(course.getNumber());
                // insert subscription from preferences
                getContext().getSharedPreferences(getContext()
                        .getString(R.string.shared_prefs_courses_list), Context.MODE_PRIVATE)
                        .edit()
                        .remove(course.getNumber())
                        .apply();
            }
        });
        // apply saved preferences to subscription
        ((SwitchMaterial) convertView.findViewById(R.id.switch1))
                .setChecked(getContext()
                        .getSharedPreferences(getContext().getString(R.string.shared_prefs_courses_list), Context.MODE_PRIVATE)
                        .getBoolean(course.getNumber(), false));
        return convertView;
    }

    private void applyCourseDetails(Course course, View view) {
        // set the text of the textview based on the course data
        ((TextView) view.findViewById(R.id.textView)).setText(String.format(
                getContext().getString(R.string.course_row_format),
                course.getName(),
                course.getNumber()
        ));
    }
}
