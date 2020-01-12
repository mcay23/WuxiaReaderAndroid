/*

DEPRECATED

package com.yuwono.wuxiareader;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Chapter {

    private String title;
    private int chapter_num;

    private Context context;

    public Chapter(Context context, String title, int chapter_num) {
        this.context = context;
        this.title = title;
        this.chapter_num = chapter_num;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setChapter(int num) {
        this.chapter_num = num;
    }

    public String getChapter() {

        StringBuffer buffer = new StringBuffer();
        InputStream is = context.getResources().openRawResource(R.raw.file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        if (is != null) {
            try {
                String data;
                while ((data = reader.readLine()) != null) {
                    buffer.append(data + "\n");
                }
                is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return buffer.toString();
    }

}
*/