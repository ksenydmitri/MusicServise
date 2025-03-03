package music.service.model;


import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    private Set<User> user;

    @Column
    private String title;

    @Column
    private Integer duration;

    @Column
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Album album;

    @ManyToMany(mappedBy = "tracks", cascade = {CascadeType.DETACH,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<Playlist> playlists;

    @ManyToMany
    @JoinTable(name = "track_genre",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "user_track",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new LinkedHashSet<>();

    public Track() {}

    public Track(String title, Set<User> artists, Set<Genre> genres,
                 Integer duration, LocalDate releaseDate) {
        this.title = title;
        this.user = artists;
        this.genres = genres;
        this.duration = duration;
        this.releaseDate = releaseDate;
    }
}
