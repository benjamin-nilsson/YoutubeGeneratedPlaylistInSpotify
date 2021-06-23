
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
    /**
     * Create an instance object of the AddSongToPlaylist class,
     * which will get all songs, create a playlist and then add
     * all the songs.
     *
     * @Throws GeneralSecurityException, IOException.
     */
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        var addSongToPlaylistDemo = new AddSongToPlaylist();
        addSongToPlaylistDemo.addSong();
    }
}