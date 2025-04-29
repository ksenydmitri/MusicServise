import { Link } from 'react-router-dom';
import { Album } from '../types/album';
import './styles/albumCard.css';
import { useState, useCallback, useEffect } from "react";
import defaultAvatar from "../cover_image.jpg";
import { mediaApi } from "../api/api";

interface AlbumCardProps {
    album: Album;
    onAddTrack?: () => void; // Колбэк для добавления трека
}

const AlbumCard = ({ album, onAddTrack }: AlbumCardProps) => {
    const [albumCoverUrl, setAlbumCoverUrl] = useState<string>(defaultAvatar);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        fetchCover();
    }, [album.coverImageId]);

    const fetchCover = useCallback(async () => {
        if (!album.coverImageId) {
            setAlbumCoverUrl(defaultAvatar);
            return;
        }

        setIsLoading(true);
        try {
            const response = await mediaApi.downloadMedia(album.coverImageId);
            const contentType = response.headers['content-type'] || 'image/jpeg';
            const blob = new Blob([response.data], { type: contentType });
            const url = URL.createObjectURL(blob);
            setAlbumCoverUrl(url);
        } catch (error) {
            console.error('Ошибка загрузки обложки:', error);
            setAlbumCoverUrl(defaultAvatar);
        } finally {
            setIsLoading(false);
        }
    }, [album.coverImageId]);

    const handleAddTrack = async () => {

    }

    return (
        <div className="album-card">
            <div className="album-content">
                <Link to={`/album/${album.id}`} className="album-link">
                    <img
                        src={albumCoverUrl}
                        alt={album.title}
                        className="album-cover"
                    />
                    <div className="album-info">
                        <h3 className="album-title">{album.title}</h3>
                        <p className="album-artist">{album.artist}</p>
                    </div>
                </Link>
                <button
                    onClick={onAddTrack}
                    className="add-track-button"
                    aria-label="Добавить трек"
                >
                    +
                </button>
            </div>
        </div>
    );
};

export default AlbumCard;