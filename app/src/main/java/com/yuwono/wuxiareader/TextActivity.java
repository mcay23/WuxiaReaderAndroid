package com.yuwono.wuxiareader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Scanner;

public class TextActivity extends AppCompatActivity {

    static Context context;

    static final String MY_PREFS_NAME = "FONT_DATA";
    static SharedPreferences prefs;

    static NestedScrollView nested;
    static Toolbar toolbar;
    static TextView bodyText;
    static GestureDetector detector;
    static int scrollVal;
    static TextView title;
    static FloatingActionButton next_button;
    static FloatingActionButton prev_button;
    static AppBarLayout text_appbar;
    static DisplayMetrics dm;
    static CoordinatorLayout layout;

    static int activity_width;
    static int activity_height;

    static Book book;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        activity_width = dm.widthPixels;
        activity_height = dm.heightPixels;

        layout = findViewById(R.id.text_layout);
        context = getApplicationContext();

        nested = findViewById(R.id.recycler);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.chaptertitle);
        bodyText = findViewById(R.id.textinside);
        next_button = findViewById(R.id.next_button);
        prev_button = findViewById(R.id.prev_button);
        text_appbar = findViewById(R.id.text_appbar);

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        setFontFamily(getFontFamily());
        setTextTheme(getTextTheme());

        detector = new GestureDetector(this, new GestureTap());

        // onResume() controls some UI

        bodyText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            detector.onTouchEvent(event);
            return true;
            }
        });

        nested.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                scrollVal = scrollY;
                int bottom = v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight();
                if (scrollY == bottom) {
                    text_appbar.setExpanded(true);
                    next_button.show();
                    prev_button.show();
                } else {
                    text_appbar.setExpanded(false);
                    next_button.hide();
                    prev_button.hide();
                }
            }
        });

        next_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextPage();
            }
        });

        prev_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prevPage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(getApplicationContext(),
                        PopupSettingsActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.action_refresh:
                File file = new File(book.getPath(), "/ch" + book.getCurrentChapter() + ".txt");
                file.delete();
                Library.updateBook(book);
                Toast.makeText(context,
                        "Refreshing... Please re-open chapter", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // right side screen
            if(e.getX() >= (activity_width * 0.3)){
                nextPage();
            }
            // LEFT SIDE SCREEN
            if(e.getX() < (activity_width * 0.3)){
                prevPage();
            }
            return true;
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (next_button.getVisibility() == View.VISIBLE &&
                    prev_button.getVisibility() == View.VISIBLE) {
                next_button.hide();
                prev_button.hide();
                text_appbar.setExpanded(false);
            } else {
                next_button.show();
                prev_button.show();
                text_appbar.setExpanded(true);
            }
            return true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setScrollVal(Integer.toString(scrollVal), getApplicationContext(), book);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setScrollVal(Integer.toString(scrollVal), getApplicationContext(), book);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BookActivity.onCreateTextActivity();

        text_appbar.setExpanded(false);
        next_button.hide();
        prev_button.hide();
    }

    public static void setTitle(String str) {
        title.setText(str);
    }

    public static void setScrollVal(String data, Context context, Book target) {
        try {
            File file = new File(target.getPath().getPath(), "/scrollval.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(data.getBytes());
            } finally {
                fileOutputStream.close();
            }
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private static int getScrollVal() {
        File file = new File(book.getPath().getPath() + "/scrollval.txt");
        if (file.exists()) {
            try {
                Scanner sc = new Scanner(file);
                int ret = Integer.parseInt(sc.next());
                sc.close();
                return ret;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public static void setContent(Book b) {
        book = b;
        int chapter = b.getCurrentChapter();
        String chapter_title = b.getChapterTitle(chapter);
        String content = b.getChapter(chapter);

        bodyText.setText(content);
        title.setText(chapter_title);

        if(prefs.contains("fontSize")) {
            int fontSize = prefs.getInt("fontSize", 0);
            setFontSettings(fontSize, -1);
        }
        if (prefs.contains("fontSpacing")) {
            int fontSpacing = prefs.getInt("fontSpacing", 0);
            setFontSettings(-1, fontSpacing);
        }
        if (prefs.contains("font")) {
            int font = prefs.getInt("font", 0);
            setFontFamily(font);
        }
        setTextTheme(prefs.getInt("theme", 0));
        nested.post(new Runnable() {
            @Override
            public void run() {
                nested.scrollTo(0, getScrollVal());
            }
        });
    }

    public static void setFontSettings(int fontSize, int spacing) {
        if (fontSize != -1) {
            bodyText.setTextSize(fontSize);
        }
        if (spacing != -1) {
            bodyText.setLineSpacing(spacing, 1);
        }
    }

    public static void setFontFamily(int pos) {
        /*
        Serif
        Sans Serif
        Monospace
        Biryani Light
        Cinzel
         */
        switch(pos) {
            case 0:
                bodyText.setTypeface(Typeface.SERIF);
                break;
            case 1:
                bodyText.setTypeface(Typeface.SANS_SERIF);
                break;
            case 2:
                bodyText.setTypeface(Typeface.MONOSPACE);
                break;
            case 3:
                bodyText.setTypeface(FontCache.get("biryani_light", context));
                break;
            case 4:
                bodyText.setTypeface(FontCache.get("cinzel", context));
                break;
        }
    }

    public static int getFontFamily() {
        if (prefs.contains("font")) {
            return prefs.getInt("font", 0);
        }
        return 0;
    }

    public static int getTextTheme() {
        if (prefs.contains("theme")) {
            return prefs.getInt("theme", 0);
        }
        return 0;
    }

    public static void setTextTheme(int pos) {
        /*
            Default White
            Off White
            Warm White
            Ocean Blue
            Gray Night
            True Black 1
            True Black 2
         */
        switch(pos) {
            case 0:
                bodyText.setTextColor(Color.parseColor("#000000"));
                layout.setBackgroundColor(Color.WHITE);
                break;
            case 1:
                bodyText.setTextColor(Color.parseColor("#000000"));
                layout.setBackgroundColor(Color.parseColor("#F5F2D0"));
                break;
            case 2:
                bodyText.setTextColor(Color.parseColor("#000000"));
                layout.setBackgroundColor(Color.parseColor("#EFEBD8"));
                break;
            case 3:
                bodyText.setTextColor(Color.parseColor("#ffffff"));
                layout.setBackgroundColor(Color.parseColor("#093145"));
                break;
            case 4:
                bodyText.setTextColor(Color.parseColor("#ffffff"));
                layout.setBackgroundColor(Color.parseColor("#424242"));
                break;
            case 5:
                bodyText.setTextColor(Color.parseColor("#ffffff"));
                layout.setBackgroundColor(Color.parseColor("#000000"));
                break;
            case 6:
                bodyText.setTextColor(Color.parseColor("#888888"));
                layout.setBackgroundColor(Color.parseColor("#000000"));
                break;
        }
    }

    public void nextPage() {
        if (book.getCurrentChapter() != book.getLatestChapter()) {
            setScrollVal("0", getApplicationContext(), book);
            book.setCurrentChapter(book.getCurrentChapter() + 1);
            setContent(book);
        } else {
            Toast.makeText(context, "No new chapters", Toast.LENGTH_SHORT).show();
        }
    }

    public void prevPage() {
        if (book.getCurrentChapter() != 1) {
            setScrollVal("0", getApplicationContext(), book);
            book.setCurrentChapter(book.getCurrentChapter() - 1);
            setContent(book);
        } else {
            Toast.makeText(context, "No previous chapters", Toast.LENGTH_SHORT).show();
        }
    }
}

class FontCache {
    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();
    public static Typeface get(String name, Context context) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}