package com.example.bbcnews;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
    }

    private void loadDataFromDatabase() {
        MyOpener dbOpener = new MyOpener(this);
        db = dbOpener.getWritableDatabase();

        String[] columns = {MyOpener.COL_ID, MyOpener.COL_TITLE, MyOpener.COL_LINK};

        Cursor results = db.query(false, MyOpener.TABLE_NAME, columns, null, null,
                null, null, null, null);



        results.close();
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