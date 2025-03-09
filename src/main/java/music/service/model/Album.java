package music.service.model;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "genre")
    private String genre;

    @OneToMany(mappedBy = "album",
            cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Track> tracks = new LinkedHashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(name = "users_albums",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new LinkedHashSet<>();

    public Album() {

    }

    public Album(String title) {
        this.title = title;
    }

}
