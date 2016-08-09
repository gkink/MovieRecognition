package com.example.giorgi.movierecognition.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.giorgi.movierecognition.R;
import com.example.giorgi.movierecognition.controller.ImageDownloadedListener;
import com.example.giorgi.movierecognition.controller.JSONObjectDownloadedListener;
import com.example.giorgi.movierecognition.model.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayMovieActivity extends AppCompatActivity  implements ImageDownloadedListener, JSONObjectDownloadedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_movie);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        startDownloadJson();
    }

    private void displayResponse(int viewId, int key_name, JSONObject response) throws JSONException {
        TextView textView = ((TextView)findViewById(viewId));
        textView.setText(response.getString(getResources().getString(key_name)));
    }

    private void displayResponses(JSONObject response) throws JSONException {
        String title = String.format("%s (%s)", response.getString(getResources()
                .getString(R.string.movie_title_key)), response
                .getString(getResources().getString(R.string.movie_year_key)));
        ((TextView)findViewById(R.id.title_text)).setText(title);

        displayResponse(R.id.genre, R.string.movie_genre_key, response);
        displayResponse(R.id.runtime, R.string.movie_runtime_key, response);
        displayResponse(R.id.plot, R.string.movie_plot_key, response);
        displayResponse(R.id.directors, R.string.movie_director_key, response);
        displayResponse(R.id.writers, R.string.movie_writer_key, response);
        displayResponse(R.id.cast_list, R.string.movie_actors_key, response);
    }

    private String getFilmId(){
        return getIntent().getStringExtra(getResources().getString(R.string.movie_id_intent_key));
    }

    private void startDownloadJson() {
        String film_id = getFilmId();
        if (film_id.equals(getResources().getString(R.string.not_found_string)))
            film_id = getResources().getString(R.string.not_found_film_id);

        String url = String.format(getResources().getString(R.string.omdb_api_url), film_id);
        VolleySingleton.getInstance(this).downloadJSONObject(url, this, null);
    }

    private void downloadImage(String url){
        VolleySingleton.getInstance(this).downloadImage(url, this);
    }

    @Override
    public void imageDownloaded(Bitmap image) {
        ImageView mImageView;
        mImageView = (ImageView) findViewById(R.id.cover);
        mImageView.setImageBitmap(image);
    }

    @Override
    public void jsonObjectDownloaded(JSONObject response) {
        try {
            downloadImage(response.getString(getResources()
                    .getString(R.string.movie_poster_key)));
            displayResponses(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

