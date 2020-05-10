package com.yuwono.wuxiareader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class SearchActivity extends AppCompatActivity {

    SearchView searchView;
    ListView listView;
    ArrayList<String> titles;
    ArrayList<String> urls;
    ArrayAdapter<String> adapter;
    int index_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);

        searchView = (SearchView) findViewById(R.id.searchView);
        listView = (ListView) findViewById(R.id.search_list);

        titles = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.titles)));
        urls = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.urls)));

        index_size = titles.size();
        searchView.setQueryHint("Search " + index_size + " titles...");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) (listView.getItemAtPosition(position));
                final String url = urls.get(titles.indexOf(item));
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // POSITIVE and NEGATIVE are switched (location)
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                if (isValidURL(url)) {
                                    MainActivity.forceUpdateBook add_book = new MainActivity.forceUpdateBook(url);
                                    add_book.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    finish();
                                } else {
                                    dialog.cancel();
                                    Toast.makeText(SearchActivity.this,
                                            "Bad URL. Book may be broken", Toast.LENGTH_SHORT).show();
                                }
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setMessage("Add this book? \n" + url)
                        .setPositiveButton("No", dialogClickListener)
                        .setNegativeButton("Yes", dialogClickListener).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_manual_add:
                LayoutInflater li = LayoutInflater.from(SearchActivity.this);
                View promptsView = li.inflate(R.layout.add_prompt, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        SearchActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        // positive / negative button switched
                        .setPositiveButton("CANCEL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        String url = userInput.getText().toString();
                                        if (isValidURL(url)) {
                                            MainActivity.forceUpdateBook add_book = new MainActivity.forceUpdateBook(url);
                                            add_book.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            finish();
                                        } else {
                                            dialog.cancel();
                                            Toast.makeText(SearchActivity.this,
                                                    "Invalid URL", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public static boolean isValidURL(String input)
    {
        try {
            URL url = new URL(input);
            URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
            return false;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

