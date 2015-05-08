package andrewhammer.hammeruberapplication;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

/**
 * Created by andrewhammer on 5/6/15.
 */
public class NetworkRequest {
    private static final String REFERER_HEADER = "referer";
    private static final String REFERER_SRC = "http://www.uber.com"; //placeholder referer URL for referer header in api request

    public static Response doConnectionRequest(String url, OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header(REFERER_HEADER, REFERER_SRC)
                .build();

        return client.newCall(request).execute();
    }

    //parses the json result of the api call using gson
    //stores image urls into destinationList - java passes by reference
    public static void parseResult(String result, List<String> destinationList) {
        JsonElement jsonResult = new JsonParser().parse(result);

        JsonObject jsObjResult = jsonResult.getAsJsonObject();
        JsonObject responseData = jsObjResult.getAsJsonObject("responseData");

        //Might not be super robust w/ bad responses, but should be handled by a try/catch

        //Iterate over responses to add urls to list
        JsonArray responses = responseData.getAsJsonArray("results");

        if (responses.size() == 0) {
            return;
        }

        for (int i = 0; i < responses.size(); i ++) {
            JsonObject response = responses.get(i).getAsJsonObject();
            String url = response.get("tbUrl").getAsString();  //gets the thumbnail url
            destinationList.add(url);
        }
    }


}
