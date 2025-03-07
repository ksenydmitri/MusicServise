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
public class UserTrackId implements java.io.Serializable {
    private static final long serialVersionUID = 7929371672366066327L;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserTrackId entity = (UserTrackId) o;
        return Objects.equals(this.trackId, entity.trackId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackId, userId);
    }

}