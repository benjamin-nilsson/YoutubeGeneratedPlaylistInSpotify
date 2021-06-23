
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Put all the parts together for adding
 * all our liked songs to a Spotify playlist.
 */
public class AddSongToPlaylist {
    private SpotifySecrets spotifySecrets = new SpotifySecrets();

    /**
     * Takes a songs uri and adds it to a newly created playlist.
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void addSong() throws GeneralSecurityException, IOException {
        // Get all the songs.
        var ytClient = new YoutubeClient();
        List<String> allSongsUri = ytClient.getLikedVideos();

        // Create the new playlist.
        var playlist = new CreateNewPlaylist();
        String playlistID = playlist.createPlaylist();

        // We add each song to our playlist if it has an uri.
        for (String uri : allSongsUri) {
            if (uri.isEmpty())
                continue;

            HttpClient client = HttpClient.newHttpClient();
            // Querying the Spotify's API for adding a song to a playlist.
            // Http header fields.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?uris=" + uri.replaceAll(":track:", "%3Atrack%3A")))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", spotifySecrets.getSpotifyToken())
                    .build();

            // Enables our primitive data to be read and be fed into the server.
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println)
                    .join();
        }
    }
}
