package music.service.dto;

import java.util.Set;

public class PlaylistDTO {

    private Long id;
    private String name;
    private Set<Long> trackIds;

    // Конструкторы
    public PlaylistDTO() {}

    public PlaylistDTO(Long id, String name, Set<Long> trackIds) {
        this.id = id;
        this.name = name;
        this.trackIds = trackIds;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(Set<Long> trackIds) {
        this.trackIds = trackIds;
    }
}
