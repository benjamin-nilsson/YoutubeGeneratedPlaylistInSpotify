
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Searches for a song and its artist
 * and retrieves each songs URI.
 */
public class Search {
    private HttpsURLConnection connection;
    private SpotifySecrets spotifySecrets = new SpotifySecrets();

    /**
     * Request Spotify's API for a song/artist.
     *
     * @param title
     * @param artist
     * @Returns the songs URI.
     */
    public String search(String title, String artist) {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        // We fix the title and the artist according to Spotify's requirement for querying a song/artist.
        var songTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        var theArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8);

        // Querying the Spotify's API for finding a song/artist.
        try {
            // Http header fields.
            URL url = new URL("https://api.spotify.com/v1/search?query=" + songTitle + "+" + theArtist + "&type=track&offset=0&limit=20");
            connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", spotifySecrets.getSpotifyToken());

            /*
            If we get a bad status due to that the video is not about music
            we want to move on to the next one without throwing an exception.
            If we however get a bad connection due to other reasons, it will
            be caught in the CreateNewPlaylist as this is where we first establish
            a connection.
             */
            int status = connection.getResponseCode();
            if (status > 200)
                return "";
            else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null)
                    responseContent.append(line);
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return parseSearch(responseContent.toString());
    }

    /**
     * Parse our json data.
     * Goes through the response data from the server.
     *
     * @param responseBody
     * @Returns the songs URI.
     */
    private String parseSearch(String responseBody) {
        JSONObject song = new JSONObject(responseBody);
        JSONArray songArray = song.getJSONObject("tracks").getJSONArray("items");

        /*
        Need to handle JSONException as all liked videos
        might not be music content or the original song.
         */
        try {
            JSONObject item = songArray.getJSONObject(0);

            return item.getString("uri");
        } catch (JSONException e) {
            return "";
        }
    }
}
