
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Creates a new playlist in the users Spotify account
 * and retrieves that playlists ID.
 */
public class CreateNewPlaylistAlt {
    private HttpsURLConnection connection;
    private SpotifySecrets spotifySecrets = new SpotifySecrets();

    /**
     * We submit data to the server.
     *
     * @Returns the created playlists ID.
     */
    public String createPlaylist() {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        // The data we want submitted.
        String data = "{\"name\":\"YouTube\",\"description\":\"Liked Videos\",\"public\":false}";
        byte[] postData = data.getBytes(StandardCharsets.UTF_8);

        try {
            // Http header fields.
            URL url = new URL("https://api.spotify.com/v1/users/" + spotifySecrets.getUserID() + "/playlists");
            connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", spotifySecrets.getSpotifyToken());

            // Enables our primitive data to be read and be fed into the server
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
                wr.flush();
            }

            /*
            If we get a response code of 201 it means that our
            request was successfully submitted and created.
             */
            int status = connection.getResponseCode();
            if (status > 201)
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            else
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null)
                responseContent.append(line);
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return parsePlaylist(responseContent.toString());
    }

    /**
     * Parse our json data.
     * Go through the response data from the server.
     *
     * @param responseBody
     * @Returns the playlists ID.
     */
    private String parsePlaylist(String responseBody) {
        JSONObject account = new JSONObject(responseBody);

        return account.getString("id");
    }
}
