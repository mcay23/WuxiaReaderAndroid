package com.yuwono.wuxiareader;

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
            // wuxiaworld.com vs .co
            boolean com = book.getURL().contains("com");
            Document doc = Jsoup.connect(book.getURL()).userAgent("mozilla/17.0").timeout(10000).get();
            book.getPath().mkdirs();
            if (com) {
                Elements item = doc.select("li.chapter-item").select("a[href]");
                for (Element e : item) {
                    links.add(e.attr("abs:href"));
                }
            } else {
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
                if (book.isMarkedRemove()) {
                    break;
                }
                // different getPath() methods
                File file = new File(book.getPath().getPath() + "/ch" + counter + ".txt");
                if (!file.exists()) {
                    Document doc2 = Jsoup.connect(url).userAgent("mozilla/17.0").timeout(20000).get();
                    String title;
                    String text;
                    if (com) {
                        Elements title_container =
                                doc2.select("div.p-15").select("h4");
                        Elements body = doc2.select("div#chapter-content").select("p");
                        StringBuilder builder = new StringBuilder("\n");
                        for (Element line : body) {
                            builder.append(line.html());
                            builder.append("\n\n");
                        }
                        title = title_container.first().text() + "\n";
                        text = Jsoup.clean(builder.toString(), "", Whitelist.none(),
                                new OutputSettings().prettyPrint(false));
                    } else {
                        Elements body = doc2.select("div#content");
                        Elements chapter_title = doc2.select("div.bookname").select("h1");
                        String html = body.first().html();
                        title = chapter_title.text() + "\n";
                        text = Jsoup.clean(html, "", Whitelist.none(),
                                new OutputSettings().prettyPrint(false));
                    }

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

