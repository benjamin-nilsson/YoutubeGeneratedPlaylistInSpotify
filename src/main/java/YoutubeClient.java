import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Sample Java code for youtube.playlists.list
 * Create an authorized API client service
 * that will be used for getting the liked
 * videos and their URI:s.
 */
public class YoutubeClient {
    private static final String CLIENT_SECRETS = "client.secret.json";
    private static final Collection<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/youtube.readonly");
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "API code samples";

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = Main.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        Credential credential =
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("1063996108586-jbg6d1drgegj3t64a4aemcpckbip6930.apps.googleusercontent.com");
        return credential;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public YouTube getYoutubeClient() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * We make a request for the liked videos and then we put each videos URI
     * into a list that will be used for finding the songs.
     *
     * @Returns a list containing each found songs URI.
     */

    public List<String> getLikedVideos() throws GeneralSecurityException, IOException {
        YouTube.Videos.List likedVideos = getYoutubeClient()
                .videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"));
        VideoListResponse response = likedVideos.setMaxResults(100L)
                .setMyRating("like")
                .execute();

        return parse(response.toString());
    }

    /**
     * As I could not find any documentation on how to extract artist and song
     * through YoutubeDL in Java, I came up with my own solution. As most music videos
     * are separated by a '-' sign, we can use this to separate between artist and song.
     *
     * @param response
     * @Returns a list of songs URI:s.
     */
    private List<String> parse(String response) {
        List<String> spotifyUris = new ArrayList<>();

        JSONArray videos = new JSONObject(response).getJSONArray("items");
        for (int i = 0; i < videos.length(); i++) {
            JSONObject video = videos.getJSONObject(i);
            JSONObject snippet = video.getJSONObject("snippet");
            String videoTitle = snippet.getString("title");
            String youtubeURL = "https://www.youtube.com/watch?v=" + video.getString("id");
            System.out.println(youtubeURL);

            /*
            Call to the spotify API to return the uri for each song
            and then storing it in a list of uris that will be used for
            adding all the songs to our playlist
             */
            var findSongs = new Search();
            String spotifyUri = findSongs.search(getTitle(videoTitle), getArtist(videoTitle));
            spotifyUris.add(spotifyUri);
        }

        return spotifyUris;
    }

    /**
     * Get the title of the video.
     *
     * @param videoTitle
     * @return the song title.
     */
    private String getTitle(String videoTitle) {
        StringBuilder songTitle = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int j = videoTitle.length() -1; j > 0; j--) {
            if (videoTitle.charAt(j) == '-')
                break;
            if (videoTitle.charAt(j) == '(' || videoTitle.charAt(j) == ')')
                continue;

            stack.push(videoTitle.toLowerCase().charAt(j));
        }

        while (!stack.isEmpty())
            songTitle.append(stack.pop());

            /*
            We remove unnecessary noise by removing the most used words
            related to music videos, as this may prevent us from getting
            search results in Spotify
             */
        String changeableTitle = songTitle.toString();
        String change1 = changeableTitle.replaceAll("video", "");
        String change2 = change1.replaceAll("lyric", "");
        String change3 = change2.replaceAll("official", "");
        String finalTitle = change3.replaceAll("m/v", "");

        return finalTitle.replaceAll("mv", "");
    }

    /**
     * Get the artist of the video.
     *
     * @param videoTitle
     * @return the artist.
     */
    private String getArtist(String videoTitle) {
        StringBuilder artistName = new StringBuilder();

        for (char ch : videoTitle.toCharArray()) {
            if (ch == '-')
                break;
            artistName.append(ch);
        }

        return artistName.toString();
    }
}
