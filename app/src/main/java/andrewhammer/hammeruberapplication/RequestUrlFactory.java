package andrewhammer.hammeruberapplication;

/**
 * Created by andrewhammer on 5/2/15.
 * * Class to generate request for Google Image Search API
 */

import android.net.Uri;

public class RequestUrlFactory {
    private static final String BASE_URL = "https://ajax.googleapis.com/ajax/services/search/images?";
    private static final String VERSION = "v=1.0";
    private static final String QUERY = "&q=";
    private static final String RSZ = "&rsz=8"; //ensures that 8 results get returned each time
    public static final String START = "&start="; //URL field for start index
    private static final String USER_IP = "&userip=";  //recommended but not necessary
    public static final String REFERER_INFO = "referer";

    public static String createRequestUrl(String query) {
        query = String.format("%s", Uri.encode(query));  //formats query correctly

        StringBuilder sb = new StringBuilder(BASE_URL);

        sb.append(VERSION).append(QUERY).append(query);
        sb.append(RSZ);
        return sb.toString();
    }

    public static String createRequestUrl(String query, int startIndex) {
        query = String.format("%s", Uri.encode(query));  //formats query correctly
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append(VERSION).append(QUERY).append(query);
        sb.append(RSZ).append(START).append(startIndex);

        return sb.toString();
    }
}
