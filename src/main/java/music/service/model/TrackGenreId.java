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
public class TrackGenreId implements java.io.Serializable {
    private static final long serialVersionUID = 4017325290798203960L;
    @NotNull
    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @NotNull
    @Column(name = "genre_id", nullable = false)
    private Long genreId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TrackGenreId entity = (TrackGenreId) o;
        return Objects.equals(this.genreId, entity.genreId) &&
                Objects.equals(this.trackId, entity.trackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genreId, trackId);
    }

}