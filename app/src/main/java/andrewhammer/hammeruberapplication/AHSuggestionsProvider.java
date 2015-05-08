package andrewhammer.hammeruberapplication;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by andrewhammer on 5/7/15.
 */
public class AHSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "andrewhammer.hammeruberapplication.AHSuggestionsProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public AHSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
