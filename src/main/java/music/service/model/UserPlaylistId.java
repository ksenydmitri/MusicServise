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
public class UserPlaylistId implements java.io.Serializable {
    private static final long serialVersionUID = -1483835208935739954L;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserPlaylistId entity = (UserPlaylistId) o;
        return Objects.equals(this.playlistId, entity.playlistId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistId, userId);
    }

}