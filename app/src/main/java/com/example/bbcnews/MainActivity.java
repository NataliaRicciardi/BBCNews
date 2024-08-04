package com.example.bbcnews;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<BBCNewsItem> newsList;
    private NewsAdapter adapter;


    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView myList = findViewById(R.id.list_articles);

        newsList = new ArrayList<>();

        adapter = new NewsAdapter(this,  newsList);

        myList.setAdapter(adapter);



    }

}

class BBCNewsItem {
    private final String title;
    private final String link;
    private final long id;

    public BBCNewsItem(long id, String title, String link) {
        this.id = id;
        this.title = title;
        this.link = link;
    }

    public long getId() { return id; }

    public String getTitle() { return title; }

    public String getLink() { return link;}
}


class NewsAdapter extends BaseAdapter {
    private Context context;
    private final List<BBCNewsItem> newsList;
    private final LayoutInflater inflater;

    public NewsAdapter(Context context, List<BBCNewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return newsList.size(); }

    @Override
    public Object getItem(int position) { return newsList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.article_item_list, parent, false);

        TextView textView = convertView.findViewById(R.id.article_title);

        BBCNewsItem newsItem = newsList.get(position);

        textView.setText(newsItem.getTitle());

        return convertView;
    }

}