package music.service.model;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tracks_playlists",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id"))
    private Set<Track> tracks = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_playlists",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new LinkedHashSet<>();

    public Playlist() {}

    public Playlist(String name) {
        this.name = name;
    }

}

