import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { albumApi, mediaApi } from '../api/api';
import TrackList from '../components/TrackList';
import { Album } from '../types/album';
import './styles/albumPage.css';
import AddTracksForm from '../components/AddTrackForm';
import defaultAvatar from '../cover_image.jpg';

const AlbumPage = () => {
    const { id } = useParams<{ id: number }>();
    const [album, setAlbum] = useState<Album | null>(null); // Album data
    const [albumCoverUrl, setAlbumCoverUrl] = useState<string>(defaultAvatar); // Media URL
    const [isCoverLoading, setIsCoverLoading] = useState<boolean>(true); // Media loading state
    const [isAlbumLoading, setIsAlbumLoading] = useState<boolean>(true); // Album loading state

    // Function to fetch album data
    const fetchAlbum = useCallback(async () => {
        setIsAlbumLoading(true);
        try {
            const response = await albumApi.getAlbum(id);
            setAlbum(response.data);
        } catch (error) {
            console.error('Ошибка загрузки альбома:', error);
        } finally {
            setIsAlbumLoading(false);
        }
    }, [id]);

    // Function to fetch album cover
    const fetchCover = useCallback(async () => {
        if (!album?.coverImageId) {
            setAlbumCoverUrl(defaultAvatar);
            setIsCoverLoading(false); // Mark loading as complete
            return;
        }
        try {
            const response = await mediaApi.downloadMedia(album.coverImageId);
            const contentType = response.headers['content-type'] || 'image/jpeg';
            const blob = new Blob([response.data], { type: contentType });
            const url = URL.createObjectURL(blob);
            setAlbumCoverUrl(url);
        } catch (error) {
            console.error('Ошибка загрузки обложки:', error);
            setAlbumCoverUrl(defaultAvatar); // Default fallback image
        } finally {
            setIsCoverLoading(false); // Mark loading as complete
        }
    }, [album?.coverImageId]);

    // Fetch album data and cover on component mount
    useEffect(() => {
        fetchAlbum();
    }, [fetchAlbum]);

    // Fetch album cover when album data changes
    useEffect(() => {
        if (album) {
            fetchCover();
        }
    }, [album, fetchCover]);

    // Render loading states or album details
    if (isAlbumLoading) return <div>Загрузка данных альбома...</div>;

    return (
        <div className="album-page">
            {album ? (
                <>
                    <h1>{album.title}</h1>
                    <img
                        src={albumCoverUrl}
                        alt={album.title}
                        className={`album-cover ${isCoverLoading ? 'loading' : ''}`}
                    />
                    <TrackList tracks={album.tracks} />
                    <AddTracksForm albumId={album.id} onAddTrack={() => console.log('Трек успешно добавлен!')} />
                </>
            ) : (
                <div>Альбом не найден</div>
            )}
        </div>
    );
};

export default AlbumPage;
