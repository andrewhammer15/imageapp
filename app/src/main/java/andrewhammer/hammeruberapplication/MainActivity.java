package andrewhammer.hammeruberapplication;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private GridView gridView;
    private TextView noticeTextView;
    private ProgressBar progressBar;

    private final List<String> imageUrls = new ArrayList<>();
    private ImageAdapter imageAdapter;

    private OkHttpClient client;
    private String recentQuery = null;

    private boolean flag_loading = false;
    private SearchRecentSuggestions suggestions;

    //For adding extras to bundles
    private static final String KEY_IMAGE_URLS = "Image Url Key";
    private static final String KEY_GRIDVIEW_INDEX = "First visible image index";

    //save data on teardown
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(KEY_IMAGE_URLS, (ArrayList<String>) imageUrls);
        outState.putInt(KEY_GRIDVIEW_INDEX, gridView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    //handle recreation of activity
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //savedInstanceState is always non-null here

        // clears pre-existing imageUrls - this isn't needed but might be necessary in future implementations
        if (!imageUrls.isEmpty())
            imageUrls.clear();

        imageUrls.addAll(savedInstanceState.getStringArrayList(KEY_IMAGE_URLS));

        handleNoticeVisibility();

        imageAdapter.notifyDataSetChanged();

        int i = savedInstanceState.getInt(KEY_GRIDVIEW_INDEX, -1);
        if (i > 0) {
            gridView.smoothScrollToPosition(i);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noticeTextView = (TextView) findViewById(R.id.notice_textview);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        imageAdapter = new ImageAdapter(this, R.layout.image_grid_element, imageUrls);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(imageAdapter);
        gridView.setOnScrollListener(overScrollListener);

        client = new OkHttpClient();

        suggestions = new SearchRecentSuggestions(this, AHSuggestionsProvider.AUTHORITY, AHSuggestionsProvider.MODE);

        handleNoticeVisibility();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //GenyMotion has a bug that calls onNewIntent twice when search is conducted from hard keyboard
        handleIntent(intent);
    }

    //Handles incoming intents
    //As of now, only needs to deal with ACTION_SEARCH
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            saveQueryWrapper(query);

            imageUrls.clear(); //clear out url list for the new search

            String url = RequestUrlFactory.createRequestUrl(query);
            new ImagesAsyncTask().execute(url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //checks to see if there are images and hides noticeTextView accordingly
    private void handleNoticeVisibility() {
        int vis = imageUrls.isEmpty() ? View.VISIBLE : View.INVISIBLE;
        noticeTextView.setVisibility(vis);
    }

    //uses okhttp to connect to server
    //updates UI in onPostExecute
    private class ImagesAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag_loading = true;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Response response = NetworkRequestUtils.doConnectionRequest(params[0], client);
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (IOException e) {
                Log.d("MainActivity debug", e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                if (result != null)
                    NetworkRequestUtils.parseResult(result, imageUrls);
            } catch (Exception e) {
                Log.d("ImagesAsyncTask", "Error getting response");
                Log.d("ImagesAsyncTask", e.toString());
            } finally {
                imageAdapter.notifyDataSetChanged();
                flag_loading = false;

                handleNoticeVisibility();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    //Listener to load more data when user reaches the end of the list
    private final GridView.OnScrollListener overScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE) {
                if (!flag_loading && view.getLastVisiblePosition() >= view.getCount() - 1) {
                    String url = RequestUrlFactory.createRequestUrl(recentQuery, view.getCount());
                    new ImagesAsyncTask().execute(url);
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
    };

    //keeps track of local variable for most recent query
    private void saveQueryWrapper(String query) {
        recentQuery = query;
        suggestions.saveRecentQuery(query, null);
    }
}
