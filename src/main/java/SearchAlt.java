import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Searches for a song and its artist
 * and retrieves each songs URI.
 */
public class SearchAlt {
    private SpotifySecrets spotifySecrets = new SpotifySecrets();

    /**
     * Request Spotify's API for a song/artist.
     *
     * @param title
     * @param artist
     * @Returns the songs URI.
     */
    public String search(String title, String artist) {
        // We fix the title and the artist according to Spotify's requirement for querying a song/artist.
        var songTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        var theArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8);

        // Querying the Spotify's API for finding a song/artist.
        HttpClient client = HttpClient.newHttpClient();
        // Http header fields.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spotify.com/v1/search?query=" + songTitle + "+" + theArtist + "&type=track&offset=0&limit=20"))
                    .GET()
                    .headers("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", spotifySecrets.getSpotifyToken())
                    .build();

        int responseCode = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .join();

        /*
        If we get a bad status due to that the video is not about music
        we want to move on to the next one without throwing an exception.
        If we however get a bad connection due to other reasons, it will
        be caught in the CreateNewPlaylist as this is where we first establish
        a connection.
         */
        return (responseCode > 200) ? "" : client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseSearch)
                .join();
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
