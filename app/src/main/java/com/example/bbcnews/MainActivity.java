package com.example.bbcnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends BaseActivity  {

    private static NewsAdapter adapter;
    private static JSONArray characters;

    private SearchView searchView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // call from base class to create toolbar
        setupToolbarAndDrawer(R.layout.activity_main);


        // initialize characters arrya
        characters = new JSONArray();

        // initialize adapter for list
        adapter = new NewsAdapter(this, characters);

        // get listview from
        ListView myList = findViewById(R.id.list_articles);
        myList.setAdapter(adapter);

        searchView = findViewById(R.id.search_view); // get search view

        prefs = getSharedPreferences("MyQuery", Context.MODE_PRIVATE);
        String savedQuery = prefs.getString("savedmessage", "");
        searchView.setQuery(savedQuery, false); // Retrieve and set saved queery

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // on submit
                adapter.getFilter().filter(query); // filter based on the input

                Snackbar.make(myList, R.string.snackbar_querymessage, Snackbar.LENGTH_LONG).show(); // show snackbar
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // on change
                adapter.getFilter().filter(newText); // filter based on input

                SharedPreferences.Editor editor = prefs.edit(); // save query in preference
                editor.putString("savedmessage", newText);
                editor.apply();

                return false;
            }
        });

        // get listview elements from the website
        new BBCInfo().execute("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");

        myList.setOnItemClickListener( // list item clicked
                (parent, view, position, id) -> {

                    // get item from list
                    JSONObject character = (JSONObject) adapter.getItem(position);

                    try {
                        // create new intent to display the information about the news item
                        Intent intent = new Intent(MainActivity.this, EmptyActivity.class);
                        intent.putExtra("title", character.getString("title"));
                        intent.putExtra("description", character.getString("description"));
                        intent.putExtra("link", character.getString("link"));
                        startActivity(intent);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

    }

    @Override
    protected void onPause() {
        super.onPause();

        String query = searchView.getQuery().toString();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("savedmessage", query);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // when toolbar item is selected
        int id = item.getItemId();
        if (id == R.id.favorites) { // go to favorites page when toolbar item clicked
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.help) { // help functionality
            new AlertDialog.Builder(this)
                    .setTitle(R.string.helpmenu_title)
                    .setMessage(R.string.helpmenu_messagehome)
                    .setPositiveButton(R.string.helpmenu_button, null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) { // when navigation drawer item selected
        int id = item.getItemId();
        if (id == R.id.favorites) {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class BBCInfo extends AsyncTask<String, Integer, JSONArray> {
        String response = "";
        JSONArray jsonArray = new JSONArray();

        @Override
        protected JSONArray doInBackground(String... strings) { // used code from one of my previous assignments (lab7)
            try {
                URL url = new URL(strings[0]); // get website link from list
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // create http connection
                connection.setRequestMethod("GET"); // get method

                InputStream inputStream = connection.getInputStream(); // structure for input from website
                response = convertStreamToString(inputStream); // call to get whole file as a string
                jsonArray = parseXMLToJSON(response); // xml to json

                connection.disconnect(); // disconnect from the site

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            characters = result;
            adapter.updateData(characters);
        }

        private String convertStreamToString(InputStream inputStream) throws Exception {
            StringBuilder sb = new StringBuilder();
            int byteRead;
            while ((byteRead = inputStream.read()) != -1) { // read every byte from website and add to stringbuilder
                sb.append((char) byteRead);
            }
            return sb.toString(); // stringbuilder to string
        }

        private JSONArray parseXMLToJSON(String xml) throws Exception {
            JSONArray jsonArray = new JSONArray(); // hold converted json
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); // create instance

            XmlPullParser xpp = factory.newPullParser(); // used to read xml data
            xpp.setInput(new java.io.StringReader(xml)); // set input stream

            int eventType = xpp.getEventType(); // stores start or end tag
            JSONObject currentItem = null; // temp storage for item

            while (eventType != XmlPullParser.END_DOCUMENT) { // loop until end of xml document
                String tagName;
                switch (eventType) { // different action for each xml tag being processed
                    case XmlPullParser.START_TAG:
                        tagName = xpp.getName();

                        if (tagName.equalsIgnoreCase("item")) { // for each item found
                            currentItem = new JSONObject();
                        }
                        else if (currentItem != null) { // save information from item
                            if (tagName.equalsIgnoreCase("title")) {
                                currentItem.put("title", xpp.nextText());
                            } else if (tagName.equalsIgnoreCase("description")) {
                                currentItem.put("description", xpp.nextText());
                            } else if (tagName.equalsIgnoreCase("link")) {
                                currentItem.put("link", xpp.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG: // end tag found
                        tagName = xpp.getName();

                        if (tagName.equalsIgnoreCase("item") && currentItem != null) {
                            jsonArray.put(currentItem); // item endtag found, save item into array
                        }
                        break;
                }
                eventType = xpp.next();
            }
            return jsonArray;
        }
    }

}

class NewsAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private JSONArray characters;
    private JSONArray filteredCharacters;
    private final LayoutInflater inflater;

    public NewsAdapter(Context context, JSONArray characters) {
        this.context = context;
        this.characters = characters;
        this.filteredCharacters = characters;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return filteredCharacters.length(); } // length of list

    @Override
    public Object getItem(int position) { return filteredCharacters.optJSONObject(position); } // position in list

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) { // inflate new view for the item
            convertView = inflater.inflate(R.layout.article_item_list, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.article_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject character = filteredCharacters.optJSONObject(position);
        if (character != null) { // if in filtered characters list
            holder.textView.setText(character.optString("title"));
        }

        return convertView;
    }

    public void updateData(JSONArray newCharacters) {
        this.characters = newCharacters;
        this.filteredCharacters = newCharacters;
        notifyDataSetChanged();
    }

    public Filter getFilter() { // return instance of filter
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults(); // stores filtered data
                JSONArray filteredList = new JSONArray(); //

                if (constraint == null || constraint.length() == 0) { // if nothing typed
                    filteredList = characters; // sets filtered list back to include all info
                }
                else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    // convert input to lowercase and take off trailing and leadign spaces

                    for (int i = 0; i < characters.length(); i++) { // check each element of list
                        JSONObject item = characters.optJSONObject(i);
                        if (item.optString("title").toLowerCase().contains(filterPattern)) {
                            filteredList.put(item); // add item to list if contains input text
                        }
                    }
                }

                // store results
                results.values = filteredList;
                results.count = filteredList.length();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredCharacters = (JSONArray) results.values; // update filtered list with results of filter
                notifyDataSetChanged(); // notify change
            }
        };
    }

}

class ViewHolder {
    TextView textView;
}


