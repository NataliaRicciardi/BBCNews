package com.example.bbcnews;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private List<BBCNewsItem> favoritesList;
    private FavoritesAdapter adapter;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView myList = findViewById(R.id.list_favorites);

        favoritesList = new ArrayList<>();

        loadDataFromDatabase();

        adapter = new FavoritesAdapter(this,  favoritesList);

        myList.setAdapter(adapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.help) {
            new AlertDialog.Builder(this)
                    .setTitle("Help Menu")
                    .setMessage("This is your favorites list. Everything you add to it is saved for later.")
                    .setPositiveButton("Ok", null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
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

                favoritesList.add(new BBCNewsItem(id, title, link));

                Log.d("FavoritesActivity", "Loaded item: ID=" + id + ", Title=" + title + ", Link=" + link);

            } while (results.moveToNext()); // there is next result
        }
        else {
            Log.d("FavoritesActivity", "No data found");
        }

        results.close();
    }
}

class FavoritesAdapter extends BaseAdapter {
    private Context context;
    private final List<BBCNewsItem> favoritesList;
    private final LayoutInflater inflater;
    private SQLiteDatabase db;

    public FavoritesAdapter(Context context, List<BBCNewsItem> favoritesList) {
        this.context = context;
        this.favoritesList = favoritesList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return favoritesList.size(); }

    @Override
    public Object getItem(int position) { return favoritesList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.favorites_item_list, parent, false);
        }

        TextView titleView = convertView.findViewById(R.id.title);
        TextView linkView = convertView.findViewById(R.id.link);
        Button deleteButton = convertView.findViewById(R.id.delete_button);

        BBCNewsItem favoritesItem = favoritesList.get(position);

        titleView.setText(favoritesItem.getTitle());

        // linkify the link
        String link = favoritesItem.getLink();
        String linkHtml = "<a href=\"" + link + "\">" + link + "</a>";
        linkView.setText(Html.fromHtml(linkHtml));
        linkView.setMovementMethod(LinkMovementMethod.getInstance());

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteItem(favoritesItem.getId(), position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }

    private void deleteItem(long id, int position) {
        MyOpener dbOpener = new MyOpener(context);
        db = dbOpener.getWritableDatabase();
        db.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + "=?", new String[] {String.valueOf(id)});

        favoritesList.remove(position);

        notifyDataSetChanged();

        Toast.makeText(context, "Item Deleted", Toast.LENGTH_LONG).show();
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