package com.yuwono.wuxiareader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.*;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static TextView loading_text;
    static TextView no_books;
    static Library lib;
    static ArrayList<String> book_titles;
    static MainActivity act;
    static ProgressBar load_bar;
    static ListView lv;
    static Book target;
    static FloatingActionButton add_button;
    static ArrayAdapter<String> arrayAdapter;
    static Toast loading_transition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        act = this;
        lv = findViewById(R.id.list_view);
        loading_text = findViewById(R.id.loading_text);
        no_books = findViewById(R.id.no_books);
        load_bar = findViewById(R.id.progressBar);
        add_button = findViewById(R.id.add_button);
        book_titles = new ArrayList<>();

        loading_text.setVisibility(View.VISIBLE);
        load_bar.setVisibility(View.VISIBLE);

        start loadUI = new start();
        loadUI.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        lib.saveLibrary();
    }

    public static void onCreateBookActivity() { BookActivity.setContent(target); }

    public class start extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            lib = new Library(act.getApplicationContext());
        }

        @Override
        protected Void doInBackground(Void... params) {
            lib.updateAllBooks();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            BookActivity.copyArrayList(book_titles, lib.getBookTitles());
            arrayAdapter =
                    new ArrayAdapter<>(act.getApplicationContext(),
                            android.R.layout.simple_list_item_1, book_titles);
            if (book_titles.size() == 0) {
                no_books.setVisibility(View.VISIBLE);
            }
            lv.setAdapter(arrayAdapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String choice = (String) lv.getItemAtPosition(position);
                    target = lib.getBook(choice);
                    Intent i = new Intent(act, BookActivity.class);
                    startActivity(i);
                }
            });

            add_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    loading_transition.makeText(act, "Loading index...",
                            Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(act, SearchActivity.class);
                    startActivity(i);
                }
            });

            load_bar.setVisibility(View.GONE);
            loading_text.setVisibility(View.GONE);

        }
    }

    public static class forceUpdateBook extends AsyncTask<Void, Void, Void> {

        private String url;
        private boolean added;

        forceUpdateBook(String url) {
            this.url = url;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // in case of crashes, initialize first
            added = false;
            added = lib.addBook(url);
            lib.updateAllBooks();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            BookActivity.copyArrayList(book_titles, lib.getBookTitles());
            arrayAdapter.notifyDataSetChanged();
            no_books.setVisibility(View.GONE);
            if (added) {
                Toast.makeText(act,
                        "Book added", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(act,
                        "Error adding book. Maybe it already exists", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void notifyList() {
        BookActivity.copyArrayList(book_titles, lib.getBookTitles());
        arrayAdapter.notifyDataSetChanged();
        if (book_titles.size() == 0) {
            no_books.setVisibility(View.VISIBLE);
        } else {

        }
    }
}