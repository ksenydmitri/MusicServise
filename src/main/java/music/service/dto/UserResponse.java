package music.service.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import music.service.model.Album;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private List<Long> albumIds;
}
