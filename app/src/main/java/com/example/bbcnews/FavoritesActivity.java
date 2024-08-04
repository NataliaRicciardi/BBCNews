package com.example.bbcnews;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FavoritesActivity extends AppCompatActivity {

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadDataFromDatabase();



    }

    private void loadDataFromDatabase() { // get favorites from database
        MyOpener dbOpener = new MyOpener(this);
        db = dbOpener.getWritableDatabase();

        String[] columns = {MyOpener.COL_ID, MyOpener.COL_TITLE, MyOpener.COL_LINK};

        Cursor results = db.query(false, MyOpener.TABLE_NAME, columns, null, null,
                null, null, null, null);

        int titleColIndex = results.getColumnIndex(MyOpener.COL_TITLE);
        int linkColIndex = results.getColumnIndex(MyOpener.COL_LINK);
        int idColIndex = results.getColumnIndex(MyOpener.COL_ID);

        if (results.moveToFirst()) { // move to first cursor result
            do {
                String title = results.getString(titleColIndex);
                String link = results.getString(linkColIndex);
                long id = results.getLong(idColIndex);

                newsList.add(new BBCNewsItem(id, title, link));

            } while (results.moveToNext()); // there is next result
        }

        results.close();
    }
}