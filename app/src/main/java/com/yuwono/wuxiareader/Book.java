package com.yuwono.wuxiareader;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Book implements Serializable {

    private String url;
    private String title;
    private String author;
    private int latest_chapter;
    private int curr_chapter;
    private File book_path;
    private boolean book_updating;
    private ArrayList<String> chapter_titles;
    private boolean markedRemove;

    /*
        used only to recreate existing books
     */
    public Book(String in, Context context, String title,
                String author, int latest_chapter, int curr_chapter) {
        this.url = urlFormat(in);
        this.title = title;
        this.author = author;
        this.curr_chapter = curr_chapter;
        this.latest_chapter = latest_chapter;
        this.book_updating = false;
        this.markedRemove = false;
        book_path = (new File(context.getFilesDir(), "books/" + title));
        chapter_titles = new ArrayList<>();
        updateChapterTitles();
    }

    public Book(String in, Context context) {
        this.url = urlFormat(in);
        this.curr_chapter = 1;
        this.book_updating = false;
        this.markedRemove = false;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Document doc = Jsoup.connect(url).userAgent("mozilla/17.0").get();
            // get title/author info
            Elements info_title = doc.select("div#info").select("h1");
            Elements info_author = doc.select("div#info").select("p");
            this.title = info_title.text();
            this.author = info_author.first().text();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        book_path = (new File(context.getFilesDir(), "books/" + title));
        chapter_titles = new ArrayList<>();
        updateChapterTitles();
    }

    public String getChapter(int chapter) {
        StringBuffer buffer = new StringBuffer();
        try {
            File file = new File(book_path.getPath() + "/ch" + chapter + ".txt");
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                try {
                    String data;
                    // flush out title
                    reader.readLine();
                    while ((data = reader.readLine()) != null) {
                        buffer.append(data + "\n");
                    }
                    is.close();
                    // writes current chapter to info.txt
                    setCurrentChapter(chapter);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return buffer.toString();
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getURL() {
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
        writeData();
    }

    public void setAuthor(String author) {
        this.author = author;
        writeData();

    }

    public void setPath(File path) {
        this.book_path = path;
        writeData();
    }

    public File getPath() {
        return book_path;
    }

    public void setLatestChapter(int num) {
        this.latest_chapter = num;
        writeData();
    }

    public int getLatestChapter() {
        return latest_chapter;
    }

    public void setCurrentChapter(int current) {
        curr_chapter = current;
        writeData();
    }

    public int getCurrentChapter() {
        return curr_chapter;
    }

    public boolean isUpdating() {
        return book_updating;
    }

    public void setUpdating(boolean updating) {
        this.book_updating = updating;
    }

    public boolean isMarkedRemove() {
        return markedRemove;
    }

    public String getChapterTitle(int chapter) {
        File file = new File(book_path.getPath() + "/ch" + chapter + ".txt");
        try {
            Scanner sc = new Scanner(file);
            String ret = sc.nextLine();
            sc.close();
            return ret;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void updateChapterTitles() {
        int count = chapter_titles.size() + 1;
        File file = new File(book_path.getPath() + "/ch" + count + ".txt");
        while (file.exists()) {
            chapter_titles.add(getChapterTitle(count));
            count++;
            file = new File(book_path.getPath() + "/ch" + count + ".txt");
        }
        if (latest_chapter == 0) {
            setLatestChapter(count);
        }
    }

    public ArrayList<String> getChapterTitles() {
        ArrayList<String> ret = new ArrayList<String>();
        BookActivity.copyArrayList(ret, chapter_titles);
        ret.add("Last Read Chapter");
        Collections.reverse(ret);
        return ret;
    }

    public void writeData() {
        File file = new File(book_path.getPath() + "/" + "info.txt");
        try {
            PrintWriter printer = new PrintWriter(file);
            printer.write("");
            printer.println(this.url);
            printer.println(this.title);
            printer.println(this.author);
            printer.println(this.latest_chapter);
            printer.println(this.curr_chapter);
            printer.flush();
            printer.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        Log.d("BOOK", "DATA WRITTEN");
    }

    public static String urlFormat(String str) {
        String ret = "";
        if (!str.contains("https")) {
            ret = "https://www.wuxiaworld.co/" + str + "/";
        } else {
            ret = str;
        }
        return ret;
    }
}
