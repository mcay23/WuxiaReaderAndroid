package com.yuwono.wuxiareader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class BookActivity extends AppCompatActivity {

    static ListView lv;
    static Book book;
    static BookActivity act;
    static TextView book_title;
    static TextView book_author;
    static ArrayAdapter<String> arrayAdapter;
    static ArrayList<String> chapter_titles;
    private static Context context;
    static Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        context = this;
        act = this;
        chapter_titles = new ArrayList<>();
        lv = findViewById(R.id.chapter_list);
        book_title = findViewById(R.id.book_title);
        book_author = findViewById(R.id.book_author);
        toolbar = findViewById(R.id.book_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.book_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive is NO, negative is YES (switched)
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                Library.removeBook(book);
                                finish();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure?").setPositiveButton("No", dialogClickListener)
                        .setNegativeButton("Yes", dialogClickListener).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.onCreateBookActivity();
        if (book != null) {
            refresh refresher = new refresh();
            refresher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    public static void setContent(Book b) {
        book = b;
        book_title.setText(b.getTitle());
        book_author.setText(b.getAuthor());
        copyArrayList(chapter_titles, b.getChapterTitles());
        arrayAdapter = new ArrayAdapter<>(act.getApplicationContext(),
                        android.R.layout.simple_list_item_1, chapter_titles);
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int target_chapter;
                if (position == 0) {
                    target_chapter = book.getCurrentChapter();
                } else {
                    target_chapter = chapter_titles.size() - position;
                    TextActivity.setScrollVal("0", act, book);
                }
                book.setCurrentChapter(target_chapter);
                Intent i = new Intent(act, TextActivity.class);
                context.startActivity(i);
            }
        });
    }

    public static void onCreateTextActivity() {
        TextActivity.setContent(book);
    }

    public class refresh extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            copyArrayList(chapter_titles, book.getChapterTitles());
            arrayAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (book.isUpdating()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                publishProgress();
            }
            return null;
        }
    }

    public static <T> void copyArrayList(ArrayList<T> dest, ArrayList<T> source){
        dest.clear();
        for (int i = 0; i < source.size() ; i++) {
            dest.add(source.get(i));
        }
    }
}