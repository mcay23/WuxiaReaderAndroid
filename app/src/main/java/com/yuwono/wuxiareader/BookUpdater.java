package com.yuwono.wuxiareader;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BookUpdater extends AsyncTask<Void, Void, String> {

    private Book book;
    private ArrayList<String> links;

    public BookUpdater(Book book) {
        this.book = book;
        this.links = new ArrayList<>();
    }

    @Override
    protected String doInBackground(Void... cb) {
        try {
            book.setUpdating(true);
            Document doc = Jsoup.connect(book.getURL()).userAgent("mozilla/17.0").timeout(10000).get();
            book.getPath().mkdirs();

            int site_key = -1;
            if (book.getURL().contains("wuxiaworld.co")) {
                site_key = 1;
                Elements list = doc.select("div#list").select("a[href]");
                for (Element e : list) {
                    links.add(e.attr("abs:href"));
                }
            } else if (book.getURL().contains("wuxiaworld.site")) {
                site_key = 2;
                // get latest chapter URL
                String target = doc.select("li.wp-manga-chapter")
                        .first().select("a").first().attr("href");
                System.out.println(target);
                String latest_num = "";
                for (int i = target.length() - 1; i != 0; i--) {
                    if (target.charAt(i) != '-') {
                        latest_num += target.charAt(i);
                    } else {
                        break;
                    }
                }
                latest_num = new StringBuilder(latest_num).reverse().toString();
                int latest_chapter = Integer.parseInt(latest_num);
                for (int i = 1; i <= latest_chapter; i++) {
                    links.add(book.getURL() + "chapter-" + i);
                }
            }

            book.setLatestChapter(links.size());

            // write chapter to txt
            int counter = 1;
            int updated_counter = 0;
            for (String url : links) {
                if (book.isMarkedRemove()) {
                    break;
                }
                // different getPath() methods
                File file = new File(book.getPath().getPath() + "/ch" + counter + ".txt");
                if (!file.exists()) {
                    String text = ParseTools.getChapterContent(url);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    // first line is chapter title
                    fileOutputStream.write((text).getBytes());
                    fileOutputStream.close();
                    updated_counter++;
                    if (updated_counter % 3 == 0) {
                        book.updateChapterTitles();
                    }
                }
                counter++;
            }
            if (!book.isMarkedRemove()) {
                book.updateChapterTitles();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Timeout", "YES");
        }
        book.setUpdating(false);
        return "executed";
    }

    @Override
    protected void onPostExecute(String s) {
        if (BookActivity.arrayAdapter != null) {
            BookActivity.arrayAdapter.notifyDataSetChanged();
        }
    }
}

