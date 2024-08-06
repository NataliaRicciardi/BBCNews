package com.example.bbcnews;

import android.content.ContentValues;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_LINK = "link";

    private String title;
    private String description;
    private String link;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(String param1, String param2, String param3) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, param1);
        args.putString(ARG_DESCRIPTION, param2);
        args.putString(ARG_LINK, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            link = getArguments().getString(ARG_LINK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_details, container, false);

        TextView viewTitle = view.findViewById(R.id.title_text);
        TextView viewDescription = view.findViewById(R.id.description_text);
        TextView viewLink = view.findViewById(R.id.link_text);
        Button addToFavoritesButton = view.findViewById(R.id.favorites_button);

        if (getArguments() != null) {
            viewTitle.setText(title);
            viewDescription.setText(description);

            // Create clickable link
            String linkHtml = "<a href=\"" + link + "\">" + link + "</a>";
            viewLink.setText(Html.fromHtml(linkHtml));
            viewLink.setMovementMethod(LinkMovementMethod.getInstance());
        }

        addToFavoritesButton.setOnClickListener(v -> addToFavorites());

        return view;
    }

    private void addToFavorites() {
        if (getContext() == null) return;

        MyOpener dbOpener = new MyOpener(getContext());
        SQLiteDatabase db = dbOpener.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyOpener.COL_TITLE, title);
        values.put(MyOpener.COL_LINK, link);
        db.insert(MyOpener.TABLE_NAME, null, values);

        Toast.makeText(getContext(), "Item added to favorites", Toast.LENGTH_SHORT).show();

        db.close();
    }
}