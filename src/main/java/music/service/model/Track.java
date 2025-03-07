package music.service.model;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "duration", nullable = false)
    private Long duration;

    @CreationTimestamp
    @Column(name = "releaseDate", nullable = false, updatable = false)
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany
    @JoinTable(name = "track_genre",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "track_playlist",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "playlist_id"))
    private Set<Playlist> playlists = new LinkedHashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "user_track",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new LinkedHashSet<>();

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TrackGenre> trackGenres = new LinkedHashSet<>();

    @OneToMany(mappedBy = "track")
    private Set<TrackPlaylist> trackPlaylists = new LinkedHashSet<>();

    @OneToMany(mappedBy = "track")
    private Set<UserTrack> userTracks = new LinkedHashSet<>();

    public Track() {}

    public Track(String title, Long duration) {
        this.title = title;
        this.duration = duration;
    }
}
