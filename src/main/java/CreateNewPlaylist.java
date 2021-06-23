import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Creates a new playlist in the users Spotify account
 * and retrieves that playlists ID.
 */
public class CreateNewPlaylist {
    private SpotifySecrets spotifySecrets = new SpotifySecrets();

    /**
     * We submit data to the server.
     *
     * @Returns the created playlists ID.
     */
    public String createPlaylist() {
        // The data we want submitted.
        String data = "{\"name\":\"YouTube\",\"description\":\"Liked Videos\",\"public\":false}";

        HttpClient client = HttpClient.newHttpClient();
        // Querying the Spotify's API for creating a playlist.
        // Http header fields.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/users/" + spotifySecrets.getUserID() + "/playlists"))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", spotifySecrets.getSpotifyToken())
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parsePlaylist)
                .join();
    }

    /**
     * Parse our json data.
     * Go through the response data from the server.
     *
     * @param responseBody
     * @Returns the playlists ID.
     */
    private String parsePlaylist(String responseBody) {
        var account = new JSONObject(responseBody);

        return account.getString("id");
    }
}
