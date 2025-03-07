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
public class TrackPlaylistId implements java.io.Serializable {
    private static final long serialVersionUID = 8255644673115466016L;
    @NotNull
    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @NotNull
    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TrackPlaylistId entity = (TrackPlaylistId) o;
        return Objects.equals(this.playlistId, entity.playlistId) &&
                Objects.equals(this.trackId, entity.trackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistId, trackId);
    }

}