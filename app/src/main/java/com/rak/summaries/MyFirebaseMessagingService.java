package com.rak.summaries;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final static AtomicInteger c = new AtomicInteger(0);
    private static final String SLASH_DELIM = "/";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void
    onMessageReceived(RemoteMessage remoteMessage) {
        showNotification(
                remoteMessage.getData().get(getApplicationContext().getString(R.string.notification_title_key)),
                remoteMessage.getData().get(getApplicationContext().getString(R.string.notification_text_key)),
                Objects.requireNonNull(remoteMessage.getFrom()));
    }

    /**
     * build a notification and show it the user
     * @param title title of notification
     * @param message message inside notification
     * @param from field from FCM determining topic
     */
    public void showNotification(String title, String message, String from) {
        // get topic from message
        String[] splitTopic = from.split(SLASH_DELIM);
        String topic = splitTopic[splitTopic.length - 1];

        // get courses from remote
        ArrayList<Course> courses = MainActivity.getRemoteCourses();
        if (courses == null) {
            return;
        }

        // find the relevant course (by number)
        Course cur = null;
        for (Course course : courses) {
            if (course.getNumber().equals(topic)) {
                cur = course;
                break;
            }
        }
        if (cur == null) {
            return;
        }

        // create intent to go to website
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cur.getUrl()));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // build the notification configuration
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                getString(R.string.default_notification_channel_id)
        ).setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        // build the actual notification
        builder = builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification_logo);

        // create the channel to send notification through
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    getString(R.string.default_notification_channel_id),
                    getString(R.string.notifications_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(
                    notificationChannel);
            notificationManager.notify(c.incrementAndGet(), builder.build());
        }
    }
}

