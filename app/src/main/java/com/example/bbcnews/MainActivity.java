package com.example.bbcnews;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NewsAdapter adapter;
    private JSONArray characters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        characters = new JSONArray();
        adapter = new NewsAdapter(this, characters);

        ListView myList = findViewById(R.id.list_articles);
        myList.setAdapter(adapter);

        new BBCInfo().execute("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");

        myList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    FrameLayout frame = findViewById(R.id.frame);
                    JSONObject character = (JSONObject) adapter.getItem(position);

                    try {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.favorites) {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.help) {
            new AlertDialog.Builder(this)
                    .setTitle("Help Menu")
                    .setMessage("Click on a news item to get more information and a link to the story")
                    .setPositiveButton("Ok", null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private class BBCInfo extends AsyncTask<String, Integer, JSONArray> {
        String response = "";
        JSONArray jsonArray = new JSONArray();

        @Override
        protected JSONArray doInBackground(String... strings) { // used code from one of my previous assignments (lab7)
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                response = convertStreamToString(inputStream);
                jsonArray = parseXMLToJSON(response);

                connection.disconnect();

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
            while ((byteRead = inputStream.read()) != -1) {
                sb.append((char) byteRead);
            }
            return sb.toString();
        }

        private JSONArray parseXMLToJSON(String xml) throws Exception {
            JSONArray jsonArray = new JSONArray();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);

            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new java.io.StringReader(xml));

            int eventType = xpp.getEventType();
            JSONObject currentItem = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tagName = xpp.getName();

                        if (tagName.equalsIgnoreCase("item")) {
                            currentItem = new JSONObject();
                        }
                        else if (currentItem != null) {
                            if (tagName.equalsIgnoreCase("title")) {
                                currentItem.put("title", xpp.nextText());
                            } else if (tagName.equalsIgnoreCase("description")) {
                                currentItem.put("description", xpp.nextText());
                            } else if (tagName.equalsIgnoreCase("link")) {
                                currentItem.put("link", xpp.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = xpp.getName();

                        if (tagName.equalsIgnoreCase("item") && currentItem != null) {
                            jsonArray.put(currentItem);
                        }
                        break;
                }
                eventType = xpp.next();
            }
            return jsonArray;
        }
    }

}

class NewsAdapter extends BaseAdapter {
    private Context context;
    private JSONArray characters;
    private final LayoutInflater inflater;

    public NewsAdapter(Context context, JSONArray characters) {
        this.context = context;
        this.characters = characters;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return characters.length(); }

    @Override
    public Object getItem(int position) { return characters.optJSONObject(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.article_item_list, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.article_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject character = characters.optJSONObject(position);
        if (character != null) {
            holder.textView.setText(character.optString("title"));
        }

        return convertView;
    }

    public void updateData(JSONArray newCharacters) {
        this.characters = newCharacters;
        notifyDataSetChanged();
    }

}

class ViewHolder {
    TextView textView;
}


