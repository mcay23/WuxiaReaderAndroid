package com.yuwono.wuxiareader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class UpdateService extends Service {

    private static final String CHANNEL_ID = "com.yuwono.wuxiareader";
    public static boolean running = false;
    public static Context service;
    // collect tasks so we can stop if necessary
    public static ArrayList<BookTask> running_tasks;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        running_tasks = new ArrayList<>();
        service = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        Intent stopSelf = new Intent(this, UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.act,
                0, notificationIntent, 0);
        registerReceiver(stopServiceReceiver, new IntentFilter("stopfilter"));
        PendingIntent pendingStop = PendingIntent.getBroadcast
                (this, 0, new Intent("stopfilter"), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();

            Notification.Action action = new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_stop),
                    "STOP",
                    pendingStop).build();

            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID);
            builder.setContentTitle("Wuxia Reader")
                    .setContentText("Downloading new chapters..")
                    .setSmallIcon(R.drawable.ic_book)
                    .setContentIntent(pendingIntent)
                    .addAction(action);

            notification = builder.build();
        } else {
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                    .setContentTitle("Wuxia Reader")
                    .setContentText("Downloading new chapters..")
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.ic_stop, "STOP",
                            pendingIntent)
                    .build();
        }

        startForeground(1, notification);

        MainActivity.lib.updateAllBooks();

        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
//            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static class BookTask extends AsyncTask<Void, Void, Void> {
        private Book book;

        BookTask(Book b) {
            book = b;
            running_tasks.add(this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            BookUpdater x = new BookUpdater(book);
            x.update();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (BookActivity.arrayAdapter != null) {
                BookActivity.arrayAdapter.notifyDataSetChanged();
            }
        }
    }

    public static class BookAdder extends AsyncTask<Void, Void, Void> {

        private String url;
        private boolean added;

        BookAdder(String x) throws Exception {
            this.url = x;
        }

        @Override
        protected Void doInBackground(Void... params) {
            added = MainActivity.lib.addBook(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!running) {
                MainActivity.act.startService();
            }
            if (BookActivity.arrayAdapter != null) {
                BookActivity.arrayAdapter.notifyDataSetChanged();
            }
            if (added) {
                Log.d("BOOK ADDED", "YAY");
                MainActivity.notifyList();
            } else {
                Log.d("BOOK NOT ADDED", "YAY");
            }
            if (running && added) {
                BookTask add_book = new BookTask(Library.getBook(url));
                add_book.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                BookActivity.copyArrayList(MainActivity.book_titles, MainActivity.lib.getBookTitles());
            }
            if (added) {
                Toast.makeText(MainActivity.act,
                        "Book added", Toast.LENGTH_LONG).show();
            } else if (url.contains("wuxiaworld.site")) {
                Toast.makeText(MainActivity.act,
                        "adding wuxiaworld.site books currently disabled (CloudFlare)", Toast.LENGTH_LONG).show();
            } else {
                // not added successfully on add command
                Toast.makeText(MainActivity.act,
                        "Error adding book. Maybe it already exists", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        unregisterReceiver(stopServiceReceiver);
        Log.d("UPDATE SERVICE", String.valueOf(Library.book_list.size()));
        Log.d("UPDATE SERVICE", "STOPPING SERVICE");
        // write all book datas
        MainActivity.lib.saveLibrary();
        if (BookActivity.arrayAdapter != null) {
            BookActivity.arrayAdapter.notifyDataSetChanged();
        }
        try {
            running_tasks.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void cleanTask(Book b) {
        try {
            BookTask target = null;
            for (BookTask x : running_tasks) {
                if (x.book == b) {
                    target = x;
                    break;
                }
            }
            if (target != null) {
                running_tasks.remove(target);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (running_tasks.size() == 0) {
            service.stopService(MainActivity.updateService);
        }
    }

    public static boolean isValidSession(Book b) {
        for (BookTask x : running_tasks) {
            if (x.book == b) {
                return true;
            }
        }
        return false;
    }

    public static void stopTask(Book b) {
        BookTask target = null;
        try {
            for (BookTask x : running_tasks) {
                if (x.book == b) {
//                    x.cancel(true);
                    target = x;
                    break;
                }
            }
            if (target != null) {
                running_tasks.remove(target);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (running_tasks.size() == 0) {
            service.stopService(MainActivity.updateService);
        }
    }

    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };
}
