package music.service.model;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "users_albums",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id"))
    private Set<Album> albums = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "users_playlists",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "playlist_id"))
    private Set<Playlist> playlists = new LinkedHashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "users_tracks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id"))
    private Set<Track> tracks = new LinkedHashSet<>();

    public User() {}

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }
}
