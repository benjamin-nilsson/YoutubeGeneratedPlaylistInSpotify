
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Put all the parts together for adding
 * all our liked songs to a Spotify playlist.
 */
public class AddSongToPlaylistAlt {
    private HttpsURLConnection connection;
    private SpotifySecrets spotifySecrets = new SpotifySecrets();

    /**
     * Takes a songs uri and adds it to a newly created playlist.
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void addSong() throws GeneralSecurityException, IOException {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        // Get all the songs.
        YoutubeClient ytClient = new YoutubeClient();
        List<String> allSongsUri = ytClient.getLikedVideos();

        // Create the new playlist.
        CreateNewPlaylistAlt playlist = new CreateNewPlaylistAlt();
        String playlistID = playlist.createPlaylist();

        // We add each song to our playlist if it has an uri.
        for (String uri : allSongsUri) {
            if (uri.isEmpty())
                continue;

            // The data we want submitted which in this case is nothing.
            String data = "{}";
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);

            // Http header fields.
            URL url = new URL("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks?uris=" + uri.replaceAll(":track:", "%3Atrack%3A"));
            connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", spotifySecrets.getSpotifyToken());

            // Enables our primitive data to be read and be fed into the server.
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
                wr.flush();
            }

            /*
            If we get a response code of 201 it means that our
            request was successfully submitted and created (added).
             */
            int status = connection.getResponseCode();
            if (status > 201)
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            else
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null)
                    responseContent.append(line);
                reader.close();

            connection.disconnect();
        }
    }
}
