package music.service.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.dto.*;
import music.service.model.Playlist;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.PlaylistRepository;
import music.service.repositories.TrackRepository;
import music.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository,
                           TrackRepository trackRepository, UserRepository userRepository, CacheService cacheService) {
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public List<Playlist> getAllPlaylists(String user, String name) {
        String cacheKey = buildPlaylistsCacheKey(user, name);

        if (cacheService.containsKey(cacheKey)) {
            return (List<Playlist>) cacheService.get(cacheKey);
        }

        List<Playlist> playlists = fetchPlaylistsFromDB(user, name);
        cacheService.put(cacheKey, playlists);
        return playlists;
    }

    private List<Playlist> fetchPlaylistsFromDB(String user, String name) {
        if (user != null && name != null) {
            return playlistRepository.findByUserUsernameAndNameNative(user, name);
        } else if (user != null) {
            return playlistRepository.findByUserUsername(user);
        } else if (name != null) {
            return playlistRepository.findAllByName(name);
        } else {
            return playlistRepository.findAll();
        }
    }

    private String buildPlaylistsCacheKey(String user, String name) {
        return String.format("playlists_%s_%s",
                user != null ? user : "all",
                name != null ? name : "all"
        );
    }


    public Optional<Playlist> getPlaylistById(Long id) {
        return playlistRepository.findById(id);
    }

    public Playlist savePlaylist(CreatePlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void deletePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        for (Track track : playlist.getTracks()) {
            playlist.getTracks().remove(track);
        }

        for (User user : playlist.getUsers()) {
            playlist.getUsers().remove(user);
        }

        playlistRepository.delete(playlist);
    }

    public PlaylistResponse mapToPlaylistResponse(Playlist playlist) {
        PlaylistResponse response = new PlaylistResponse();
        response.setId(playlist.getId());
        response.setName(playlist.getName());
        response.setUsers(playlist.getUsers().stream()
                .map(User::getUsername)
                .collect(Collectors.toList()));
        response.setTracks(playlist.getTracks().stream()
                .map(Track::getTitle)
                .collect(Collectors.toList()));
        return response;
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long playlistId, UpdatePlaylistRequest request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        updatePlaylistName(playlist, request.getName());
        addUserToPlaylist(playlist, request.getUserId());
        addTrackToPlaylist(playlist, request.getTrackId());

        Playlist savedPlaylist = playlistRepository.save(playlist);
        evictPlaylistCaches(playlistId);
        return mapToPlaylistResponse(savedPlaylist);
    }

    private void evictPlaylistCaches(Long playlistId) {
        cacheService.evict("playlist_" + playlistId);
        cacheService.evictByPattern("playlists_*");
    }

    private void updatePlaylistName(Playlist playlist, String name) {
        if (name != null) {
            playlist.setName(name);
        }
    }

    private void addUserToPlaylist(Playlist playlist, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!playlist.getUsers().contains(user)) {
                playlist.getUsers().add(user);
            }
        }
    }

    private void addTrackToPlaylist(Playlist playlist, Long trackId) {
        if (trackId != null) {
            Track track = trackRepository.findById(trackId)
                    .orElseThrow(() -> new RuntimeException("Track not found"));
            if (!playlist.getTracks().contains(track)) {
                playlist.getTracks().add(track);
            }
        }
    }

}
