package com.yuwono.wuxiareader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class Library extends MainActivity implements Serializable {

    public static ArrayList<Book> book_list;
    private static File lib_path;
    private static Context context;

    public Library(Context c) {
        this.context = c;
        this.lib_path = c.getFilesDir();
        book_list = new ArrayList<>();
        loadLibrary();
    }

    public boolean addBook(String url) {
        Book b = new Book(url, context);
        for (Book list : book_list) {
            if (list.getURL().equals(Book.urlFormat(url))) {
                Log.d("addBook", "Book already exists.");
                return false;
            }
        }
        book_list.add(b);
        b.writeData();
        return true;
    }

    public static void removeBook(Book book) {
        File file = book.getPath();
        if (file.listFiles() != null) {
            for(File x: file.listFiles()) {
                x.delete();
            }
        }
        file.delete();
        book_list.remove(book);
        MainActivity.notifyList();
        Toast.makeText(act,
                "Book removed", Toast.LENGTH_LONG).show();
    }

    public static void updateBook(Book book) {
        if (!book.isUpdating()) {
            // start update on parallel thread
            // book isUpdating called inside thread
            Log.d("START", "BOOK UPDATING");
            new BookUpdater(book) {}.
                    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.d("UPDATEBOOK", "ALREADY UPDATING");
        }
    }

    public void updateAllBooks() {
        Log.d("UPDATING ALL BOOKS", "B");
        for (Book b : book_list) {
            updateBook(b);
        }
    }

    public String getBookChapter(String title, int chapter) {
        return getBook(title).getChapter(chapter);
    }


    public Book getBook(String title) {
        for (Book b : book_list) {
            if (title.equals(b.getTitle())) {
                return b;
            }
        }
        return null;
    }

    public String getChapterTitle(Book b, int chapter) {
        return b.getChapterTitle(chapter);
    }

    public ArrayList<String> getBookTitles() {
        ArrayList<String> titles = new ArrayList<>();
        for (Book b : book_list) {
            titles.add(b.getTitle());
        }
        return titles;
    }

    public static ArrayList<Book> getBookList() {
        return book_list;
    }

    public void loadLibrary() {
        File file = new File(lib_path, "/books/");
        file.mkdirs();
        ArrayList<File> files = new ArrayList<>(Arrays.asList(file.listFiles()));

        for (File f : files) {
            File target = new File(f, "info.txt");
            try {
                Scanner sc = new Scanner(target);
                String url = sc.nextLine();
                String title = sc.nextLine();
                String author = sc.nextLine();
                int latest_chapter = Integer.parseInt(sc.nextLine());
                int curr_chapter = Integer.parseInt(sc.nextLine());
                book_list.add(new Book(url, context, title,
                        author, latest_chapter, curr_chapter));
                sc.close();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        Log.d("LOADLIBRARY", "BOOKS LOADED");
    }

    public void saveLibrary() {
        for (Book b : book_list) {
            b.writeData();
        }
    }
}
