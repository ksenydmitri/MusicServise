package music.service.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AlbumGenreId implements java.io.Serializable {
    private static final long serialVersionUID = 1444344787476889915L;
    @NotNull
    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @NotNull
    @Column(name = "genre_id", nullable = false)
    private Long genreId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AlbumGenreId entity = (AlbumGenreId) o;
        return Objects.equals(this.genreId, entity.genreId) &&
                Objects.equals(this.albumId, entity.albumId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genreId, albumId);
    }

}