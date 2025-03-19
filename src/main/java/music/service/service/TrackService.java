package music.service.service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.dto.*;
import music.service.model.*;
import music.service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrackService {
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;

    @Autowired
    public TrackService(TrackRepository trackRepository, AlbumRepository albumRepository,
                        UserRepository userRepository, PlaylistRepository playlistRepository){
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Optional<Track> getTrackById(Long id) {
        return trackRepository.findById(id);
    }

    public List<Track> getByGenre(String genre) {
        return trackRepository.findByGenre(genre);
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

    /*@Transactional
    public TrackResponse addTrack(CreateTrackRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> new RuntimeException("Album not found"));

        Track track = new Track(request.getTitle(), request.getDuration());
        if (!track.getUsers().contains(user)) {
            track.getUsers().add(user);
        }
        track.setAlbum(album);
        track.setGenre(request.getGenre());
        album.getTracks().add(track);
        Track savedTrack = trackRepository.save(track);

        return mapToTrackResponse(savedTrack);
    }*/

    @Transactional
    public TrackResponse addTrackWithFile(CreateTrackRequest request, MultipartFile file) {

        Album album = albumRepository.findById(request.getAlbumId())
                .orElseThrow(() -> new RuntimeException("Album not found"));
        Track track = new Track(request.getTitle(), request.getDuration());
        track.setGenre(request.getGenre());
        track.setAlbum(album);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!track.getUsers().contains(user)) {
            track.getUsers().add(user);
        }
        album.getTracks().add(track);
        Track savedTrack = trackRepository.save(track);

        return mapToTrackResponse(savedTrack);
    }


    @Transactional
    public TrackResponse updateTrack(Long trackId, UpdateTrackRequest request) {
        validateInput(trackId, request);

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        updateTrackTitle(track, request.getTitle());
        updateTrackGenre(track, request.getGenre());
        updateTrackDuration(track, request.getDuration());
        addPlaylistToTrack(track, request.getPlaylistId());
        addUserToTrack(track, request.getUserId());

        Track savedTrack = trackRepository.save(track);
        return mapToTrackResponse(savedTrack);
    }

    private void validateInput(Long trackId, UpdateTrackRequest request) {
        if (trackId == null) {
            throw new IllegalArgumentException("Track ID must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }
    }

    private void updateTrackTitle(Track track, String title) {
        if (title != null && !title.isEmpty()) {
            track.setTitle(title);
        } else if (title != null) {
            throw new IllegalArgumentException("Track title must not be empty");
        }
    }

    private void updateTrackGenre(Track track, String genre) {
        if (genre != null && !genre.isEmpty()) {
            track.setGenre(genre);
        } else if (genre != null) {
            throw new IllegalArgumentException("Track genre must not be empty");
        }
    }

    private void updateTrackDuration(Track track, int duration) {
        if (duration > 0) {
            track.setDuration(duration);
        }
    }

    private void addPlaylistToTrack(Track track, Long playlistId) {
        if (playlistId != null) {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new RuntimeException("Playlist not found"));
            if (!track.getPlaylists().contains(playlist)) {
                track.getPlaylists().add(playlist);
            }
        }
    }

    private void addUserToTrack(Track track, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!track.getUsers().contains(user)) {
                track.getUsers().add(user);
            }
        }
    }

    @Transactional
    public void deleteTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found"));

        for (Playlist playlist : track.getPlaylists()) {
            playlist.getTracks().remove(track);
        }

        for (User user : track.getUsers()) {
            user.getTracks().remove(track);
        }

        trackRepository.delete(track);
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
        response.setPlaylists(track.getPlaylists().stream()
                .map(Playlist::getName)
                .collect(Collectors.toList()));
        response.setGenre(track.getGenre());
        return response;
    }

}

