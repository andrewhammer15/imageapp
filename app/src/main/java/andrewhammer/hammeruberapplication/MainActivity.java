package andrewhammer.hammeruberapplication;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private TextView historyTab;
    private TextView imagesTab;
    private GridView gridView;
    private TextView noticeTextView;
    private ProgressBar progressBar;

    private List<String> imageUrls = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private ArrayAdapter<String> historyAdapter;

    private OkHttpClient client;
    private String recentQuery = null;

    private boolean flag_loading = false;
    private SearchRecentSuggestions suggestions;

    //save data on teardown
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("gridview", gridView.onSaveInstanceState());
        outState.putBoolean("imagesSelected", imagesTab.isSelected());
        outState.putStringArrayList("imageUrls", (ArrayList<String>) imageUrls);
        super.onSaveInstanceState(outState);
    }

    //handle recreation of activity
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            gridView.onRestoreInstanceState(savedInstanceState.getParcelable("gridview"));

            //restore imageUrls
            imageUrls.clear();
            imageUrls.addAll(savedInstanceState.getStringArrayList("imageUrls"));
            imageAdapter.notifyDataSetChanged();

            if (savedInstanceState.getBoolean("imagesSelected", true)) {
                gridView.setAdapter(imageAdapter);

                selectImagesTab();
            }
//            else selectHistoryTab();

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noticeTextView = (TextView) findViewById(R.id.notice_textview);

        gridView = (GridView) findViewById(R.id.gridView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        historyTab = (TextView) findViewById(R.id.history_tab);
//        historyTab.setOnClickListener(tabListener);

        ArrayList<String> list = new ArrayList<>();
//        list.addAll(Arrays.asList(SearchHistorySingleton.getInstance().getQueries()));

//        historyAdapter =  new ArrayAdapter<>(this, R.layout.history_element,
//                R.id.history_list_textview, list);


        imagesTab = (TextView) findViewById(R.id.image_grid_tab);
        imagesTab.setOnClickListener(tabListener);

        imageAdapter = new ImageAdapter(this, R.layout.image_grid_element, imageUrls);


        selectImagesTab();

        client = new OkHttpClient();

        suggestions = new SearchRecentSuggestions(this, AHSuggestionsProvider.AUTHORITY, AHSuggestionsProvider.MODE);

//        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //GenyMotion has a bug that calls onNewIntent twice when search is conducted from hard keyboard
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            addQueryWrapper(query);
            String url = RequestUrlFactory.createRequestUrl(query);

            imageUrls.clear(); //clear out url list for the new search

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

    private View.OnClickListener tabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.isSelected())
                return;

//            switchTab();
        }
    };

    /*
    private void switchTab() {
        if (imagesTab.isSelected()) {
            selectHistoryTab();
        } else {
            selectImagesTab();
        }

    }
    */

    //selects imagesTab and updates views accordingly
    private void selectImagesTab() {

        imagesTab.setSelected(true);
        historyTab.setSelected(false);
        noticeTextView.setText(getText(R.string.no_results));
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(null);
        gridView.setOnScrollListener(overScrollListener);

        handleNoticeVisibility();
        handleBarVisibility();
    }

    //selects historyTab and updates views accordingly
    /*
    private void selectHistoryTab() {
        imagesTab.setSelected(false);
        historyTab.setSelected(true);

        noticeTextView.setText(getText(R.string.no_history));

        gridView.setNumColumns(1);
        gridView.setAdapter(historyAdapter);

        // calling notifyDataSetChanged() alone doesn't work bc adapter references static collection
        if (gridView.getCount() < SearchHistorySingleton.getInstance().getSize()) {
            historyAdapter.clear();
            historyAdapter.addAll(SearchHistorySingleton.getInstance().getQueriesSet());
            historyAdapter.notifyDataSetChanged();
        }

        gridView.setOnItemClickListener(historyListener);
        gridView.setOnScrollListener(null);

        handleNoticeVisibility();
        handleBarVisibility();
    }
        */

    //checks to see if relevant adapter has elements and hides noticeTextView accordingly
    private void handleNoticeVisibility() {
        if (gridView.getAdapter().getCount() > 0) {
            noticeTextView.setVisibility(View.INVISIBLE);
        } else {
            noticeTextView.setVisibility(View.VISIBLE);
        }
    }

    private void handleBarVisibility() {
        if (flag_loading && imagesTab.isSelected()) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
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
//                    Log.d("MainActivity debug", "valid json");
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

    private GridView.OnItemClickListener historyListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String query = SearchHistorySingleton.getInstance().getQuery(position);

            if (!query.equals(recentQuery)) {
                addQueryWrapper(query); //in order to maintain the order
                imageUrls.clear();
                String url = RequestUrlFactory.createRequestUrl(query);

                new ImagesAsyncTask().execute(url);

            }

            selectImagesTab();
        }
    };


    //Listener to load more data when user reaches the end of the list
    private GridView.OnScrollListener overScrollListener = new AbsListView.OnScrollListener() {
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
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    };

    //keeps track of local variable for most recent query
    private void addQueryWrapper(String query) {
//        SearchHistorySingleton.getInstance().addQuery(query); //in order to maintain the order
        recentQuery = query;
        suggestions.saveRecentQuery(query, null);
//        SearchHistorySingleton.getInstance().logHistory();
    }

}
