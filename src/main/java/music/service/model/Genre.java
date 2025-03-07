package music.service.model;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToMany
    @JoinTable(name = "album_genre",
            joinColumns = @JoinColumn(name = "genre_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id"))
    private Set<Album> albums = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "track_genre",
            joinColumns = @JoinColumn(name = "genre_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id"))
    private Set<Track> tracks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "genre")
    private Set<AlbumGenre> albumGenres = new LinkedHashSet<>();

    @OneToMany(mappedBy = "genre")
    private Set<TrackGenre> trackGenres = new LinkedHashSet<>();

    public Genre() {}

    public Genre(String name) {
        this.name = name;
    }
}
