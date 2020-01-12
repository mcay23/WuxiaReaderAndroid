package com.yuwono.wuxiareader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BookUpdater extends AsyncTask<Void, Void, String> {

    private Context context;
    private Book book;
    private ArrayList<String> links;

    public BookUpdater(Context context, Book book) {
        this.context = context;
        this.book = book;
        this.links = new ArrayList<>();
    }

    @Override
    protected String doInBackground(Void... cb) {
        try {
            book.setUpdating(true);
            Document doc = Jsoup.connect(book.getURL()).userAgent("mozilla/17.0").timeout(10000).get();

            book.getPath().mkdirs();

            // get links
            Elements list = doc.select("div#list").select("a[href]");
            for (Element e : list) {
                links.add(e.attr("abs:href"));
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
                    Document doc2 = Jsoup.connect(url).userAgent("mozilla/17.0").timeout(20000).get();
                    Elements body = doc2.select("div#content");
                    Elements chapter_title = doc2.select("div.bookname").select("h1");
                    String html = body.first().html();
                    String title = chapter_title.text() + "\n";
                    String text = Jsoup.clean(html, "", Whitelist.none(),
                            new OutputSettings().prettyPrint(false));
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    // first line is chapter title
                    fileOutputStream.write((title + text).getBytes());
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

