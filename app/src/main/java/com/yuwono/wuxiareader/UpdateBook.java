package com.yuwono.wuxiareader;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateBook {

    private Book book;
    private ArrayList<String> links;

    public UpdateBook(Book book) {
        this.book = book;
        this.links = new ArrayList<>();
    }

    public void update() {
        Log.d("UPDATING", "TRUE");
        try {
            book.setUpdating(true);
            Document doc = Jsoup.connect(book.getURL()).userAgent("mozilla/17.0").timeout(30000).get();
            book.getPath().mkdirs();

            int site_key = -1;
            if (book.getURL().contains("wuxiaworld.co")) {
                site_key = 1;
                Elements list = doc.select("div#list").select("a[href]");
                for (Element e : list) {
                    links.add(e.attr("abs:href"));
                }
            }
            book.setLatestChapter(links.size());

            // write chapter to txt
            int counter = 1;
            int updated_counter = 0;
            for (String url : links) {
                if (!UpdateService.isValidSession(book) || book.isMarkedRemove()) {
                    book.updateChapterTitles();
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
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error updating book", "YES");
        }
        book.updateChapterTitles();
        book.setUpdating(false);
        UpdateService.cleanTask(book);
    }
}

