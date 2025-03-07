package music.service.service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.dto.CreateTrackRequest;
import music.service.dto.TrackResponse;
import music.service.model.Album;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.AlbumRepository;
import music.service.repositories.TrackRepository;
import music.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrackService {
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    @Autowired
    public TrackService(TrackRepository trackRepository, AlbumRepository albumRepository, UserRepository userRepository) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Optional<Track> getTrackById(Long id) {
        return trackRepository.findById(id.intValue());
    }

    public List<Track> getByGenre(String genre) {
        return trackRepository.findByGenresName(genre);
    }

    public List<Track> getByAlbum(String album) {
        return trackRepository.findByAlbumTitle(album);
    }

    public List<Track> getByPlaylist(String playlist) {
        return trackRepository.findByPlaylistsName(playlist);
    }

    public List<Track> getByUser(String username) {
        return trackRepository.findByUsersUsername(username);
    }

    @Transactional
    public TrackResponse addTrack(CreateTrackRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> new RuntimeException("Album not found"));

        Track track = trackRepository.findByTitle(request.getTitle())
                .orElseGet(() -> new Track(request.getTitle(), request.getDuration()));

        if (!track.getUsers().contains(user)) {
            track.getUsers().add(user);
        }
        track.setAlbum(album);
        album.getTracks().add(track);
        Track savedTrack = trackRepository.save(track);

        return mapToTrackResponse(savedTrack);
    }


    public TrackResponse mapToTrackResponse(Track track) {
        TrackResponse response = new TrackResponse();
        response.setId(track.getId());
        response.setTitle(track.getTitle());
        response.setDuration(track.getDuration());
        response.setAlbumTitle(track.getAlbum() != null ? track.getAlbum().getTitle() : null);
        response.setUsernames(track.getUsers().stream()
                .map(User::getUsername)
                .collect(Collectors.toList()));
        response.setReleaseDate(track.getReleaseDate());
        return response;
    }

}

