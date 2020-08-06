package com.yuwono.wuxiareader;

import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public abstract class ParseTools {
    public static String getChapterContent(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("mozilla/17.0").timeout(15000).get();
        } catch (Exception ex) {
            if (UpdateService.service != null && UpdateService.running) {
                UpdateService.service.stopService(MainActivity.updateService);
            }
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
                if (NetworkTools.isConnectedInternet(MainActivity.act.getApplicationContext()) && UpdateService.service != null && UpdateService.running) {
                    UpdateService.service.stopService(MainActivity.updateService);
                    Toast.makeText(MainActivity.act.getApplicationContext(),
                            "No internet connection.", Toast.LENGTH_SHORT).show();
                    title = "DL ERROR";
                    text = "INET CONNECTION N/A. Try refreshing from the toolbar";
                } else {
                    title = "INVALID CHAPTER";
                    text = "Source link broken. Try refreshing from the toolbar";
                }
                Log.d("INVALID CHAPTER", "CAUGHT");
            }
        } else {
            title = "NULL TITLE";
            text = "NULL TEXT";
        }
        return (title.trim() + "\n" + text);
    }
}
