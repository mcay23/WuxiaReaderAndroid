package com.yuwono.wuxiareader;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public abstract class ParseTools {
    public static String getChapterContent(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("mozilla/17.0").timeout(30000).get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String title = "";
        String text = "";

        if (url.contains("wuxiaworld.co")) {
            try {
                Elements body = doc.select("div#content");
                Elements chapter_title = doc.select("div.bookname").select("h1");
                String html = body.first().html();
                title = chapter_title.text() + "\n";
                text = Jsoup.clean(html, "", Whitelist.none(),
                        new Document.OutputSettings().prettyPrint(false));
            } catch(Exception ex) {
                Log.d("INVALID CHAPTER", "CAUGHT");
                title = "INVALID CHAPTER";
                text = "Source link broken.";
            }

        } else if (url.contains("wuxiaworld.site")) {
            title = doc.select("li.active").first().text();
            Elements body = doc.select("div.text-left").select("p");
            for (Element e : body) {
                text += e.text() + "\n\n";
            }
        } else {
            title = "NULL TITLE";
            text = "NULL TEXT";
        }
        return (title.trim() + "\n" + text);
    }
}
