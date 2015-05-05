package andrewhammer.hammeruberapplication;

import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by andrewhammer on 5/3/15.
 */
public class SearchHistorySingleton {
    private static SearchHistorySingleton mInstance;
    private static Set<String> queries;  //set of recent search queries
    public static final String QUERY_INDEX = "query index"; //key for intent extra

    private SearchHistorySingleton() {
        queries = new LinkedHashSet<>();  //initializes on creation, and linkedhashset maintains order for searches
    }

    public static SearchHistorySingleton getInstance() {
        if (mInstance == null) {
            mInstance = new SearchHistorySingleton();
        }

        return mInstance;
    }

    public Set<String> getQueriesSet() {
        return queries;
    }

    public void addQuery(String query) {
        queries.add(query);
//        logHistory();
    }

    public String[] getQueries() {
        return queries.toArray(new String[queries.size()]);
    }

    public String getQuery(int i) {
        String[] qs = queries.toArray(new String[queries.size()]);
        return qs[i];
    }

    //Doesn't actually work - linkedhashset does not update order
    public String getMostRecentQuery() {
        if (queries.size() == 0)
            return null;

        return (String) queries.toArray()[queries.size() -1];
    }

    public int getSize() {
        return queries.size();
    }

    public void logHistory() {
        Log.d("SearchHistory", queries.toString());
    }

}
