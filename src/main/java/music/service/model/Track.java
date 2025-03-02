package music.service.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="artist", nullable=false)
    private String artist;

    @Column(name="genre")
    private String genre;

    @Column(name="duration")
    private Integer duration; // Duration in seconds

    @Column(name="release_date")
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany(mappedBy = "tracks", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Playlist> playlists;

    // Конструкторы, геттеры и сеттеры
    public Track() {}

    public Track(String title, String artist, String genre, Integer duration, LocalDate releaseDate) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.releaseDate = releaseDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getDuration() {
        return duration;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Album getAlbum() {
        return album;
    }

    public Set<Playlist> getPlaylists() {
        return playlists;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public void setPlaylists(Set<Playlist> playlists) {
        this.playlists = playlists;
    }
}
