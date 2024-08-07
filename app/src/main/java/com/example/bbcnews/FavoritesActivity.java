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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends BaseActivity {

    private List<BBCNewsItem> favoritesList;
    private FavoritesAdapter adapter;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // call from base class to create toolbar
        setupToolbarAndDrawer(R.layout.activity_favorites); // setup from parent activity

        ListView myList = findViewById(R.id.list_favorites); // get listview

        favoritesList = new ArrayList<>(); // initialize array

        loadDataFromDatabase(); // load database call

        adapter = new FavoritesAdapter(this,  favoritesList); // set adapter for list
        myList.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) { // if home clicked
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.help) { // if help clicked
            new AlertDialog.Builder(this)
                    .setTitle(R.string.helpmenu_title)
                    .setMessage(R.string.helpmenu_messagefav)
                    .setPositiveButton(R.string.helpmenu_button, null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) { // if home clicked
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START); // close navigation
        return true;
    }

    private void loadDataFromDatabase() { // get favorites from database
        MyOpener dbOpener = new MyOpener(this); // get instance of db opener
        db = dbOpener.getWritableDatabase(); // get the database

        String[] columns = {MyOpener.COL_ID, MyOpener.COL_TITLE, MyOpener.COL_LINK}; // columns

        Cursor results = db.query(false, MyOpener.TABLE_NAME, columns, null, null,
                null, null, null, null); // query all data

        // get column indexes from results
        int titleColIndex = results.getColumnIndex(MyOpener.COL_TITLE);
        int linkColIndex = results.getColumnIndex(MyOpener.COL_LINK);
        int idColIndex = results.getColumnIndex(MyOpener.COL_ID);

        if (results.moveToFirst()) { // move to first cursor result
            do {
                String title = results.getString(titleColIndex);
                String link = results.getString(linkColIndex);
                long id = results.getLong(idColIndex);

                favoritesList.add(new BBCNewsItem(id, title, link)); // create new element in list

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
    public int getCount() { return favoritesList.size(); } // return number of items

    @Override
    public Object getItem(int position) { return favoritesList.get(position); } // return position of item

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) { // inflate view if no view already inflated
            convertView = inflater.inflate(R.layout.favorites_item_list, parent, false);
        }

        // get view sfrom inflated layout
        TextView titleView = convertView.findViewById(R.id.title);
        TextView linkView = convertView.findViewById(R.id.link);
        Button deleteButton = convertView.findViewById(R.id.delete_button);

        BBCNewsItem favoritesItem = favoritesList.get(position); // get the position of the item

        titleView.setText(favoritesItem.getTitle()); // set title

        // linkify the link
        String link = favoritesItem.getLink();
        String linkHtml = "<a href=\"" + link + "\">" + link + "</a>";
        linkView.setText(Html.fromHtml(linkHtml)); // set link
        linkView.setMovementMethod(LinkMovementMethod.getInstance());

        deleteButton.setOnClickListener(v -> { // when delete button clicked
            new AlertDialog.Builder(context) // alert dialog
                    .setTitle(R.string.alertdialog_title)
                    .setMessage(R.string.alertdialog_body)
                    .setPositiveButton(R.string.alertdialog_yes, (dialog, which) -> {
                        deleteItem(favoritesItem.getId(), position); // if yes run deleteitem
                    })
                    .setNegativeButton(R.string.alertdialog_no, null)
                    .show();
        });

        return convertView;
    }

    private void deleteItem(long id, int position) {
        MyOpener dbOpener = new MyOpener(context); // open and get database
        db = dbOpener.getWritableDatabase();

        // delete from database and list
        db.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + "=?", new String[] {String.valueOf(id)});
        favoritesList.remove(position);

        notifyDataSetChanged(); // notify the change

        // notify user of successful deletion with toast
        Toast.makeText(context, R.string.toast_deletemessage, Toast.LENGTH_LONG).show();
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