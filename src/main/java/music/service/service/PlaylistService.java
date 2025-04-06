package music.service.service;

import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import music.service.config.CacheConfig;
import music.service.dto.*;
import music.service.exception.ResourceNotFoundException;
import music.service.model.Playlist;
import music.service.model.Track;
import music.service.model.User;
import music.service.repositories.PlaylistRepository;
import music.service.repositories.TrackRepository;
import music.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final CacheConfig cacheService;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository,
                           TrackRepository trackRepository,
                           UserRepository userRepository,
                           CacheConfig cacheService) {
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public Page<Playlist> getAllPlaylists(
            String user, String name, int page, int size, String sortBy) {
        String cacheKey = buildPlaylistsCacheKey(user, name, page, size, sortBy);

        if (cacheService.containsKey(cacheKey)) {
            return (Page<Playlist>) cacheService.get(cacheKey);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Playlist> playlists = fetchPlaylistsFromDB(user, name, pageable);
        cacheService.put(cacheKey, playlists);
        return playlists;
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private Page<Playlist> fetchPlaylistsFromDB(
            String user, String name, Pageable pageable) {
        if (user != null && name != null) {
            return playlistRepository.findByUserUsernameAndNameNative(
                    user, name, pageable);
        } else if (user != null) {
            return playlistRepository.findByUserUsername(user, pageable);
        } else if (name != null) {
            return playlistRepository.findAllByName(name, pageable);
        } else {
            return playlistRepository.findAll(pageable);
        }
    }

    private String buildPlaylistsCacheKey(
            String user, String name, int page, int size, String sortBy) {
        return String.format("playlists_%s_%s_page%d_size%d_sort%s",
                user != null ? user : "all",
                name != null ? name : "all",
                page, size, sortBy
        );
    }

    public Optional<Playlist> getPlaylistById(Long id) {
        return playlistRepository.findById(id);
    }

    public Playlist savePlaylist(CreatePlaylistRequest request) {
        cacheService.clear();
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        evictAllPlaylistCaches();
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void deletePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        for (Track track : playlist.getTracks()) {
            playlist.getTracks().remove(track);
        }

        for (User user : playlist.getUsers()) {
            playlist.getUsers().remove(user);
        }

        playlistRepository.delete(playlist);
        evictAllPlaylistCaches();
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
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        updatePlaylistName(playlist, request.getName());
        addUserToPlaylist(playlist, request.getUserId());
        addTrackToPlaylist(playlist, request.getTrackId());

        Playlist savedPlaylist = playlistRepository.save(playlist);
        evictAllPlaylistCaches();
        return mapToPlaylistResponse(savedPlaylist);
    }

    private void updatePlaylistName(Playlist playlist, String name) {
        if (name != null) {
            playlist.setName(name);
        }
    }

    private void addUserToPlaylist(Playlist playlist, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (!playlist.getUsers().contains(user)) {
                playlist.getUsers().add(user);
            }
        }
    }

    private void addTrackToPlaylist(Playlist playlist, Long trackId) {
        if (trackId != null) {
            Track track = trackRepository.findById(trackId)
                    .orElseThrow(() -> new ResourceNotFoundException("Track not found"));
            if (!playlist.getTracks().contains(track)) {
                playlist.getTracks().add(track);
            }
        }
    }

    private void evictAllPlaylistCaches() {
        cacheService.evictByPattern("playlist_*");
        cacheService.evictByPattern("playlists_*");
    }
}