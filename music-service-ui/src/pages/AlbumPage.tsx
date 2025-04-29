import React, { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Album } from '../types/album';
import { albumApi, mediaApi } from '../api/api';
import './styles/albumPage.css';
import TrackList from '../components/TrackList';
import defaultAvatar from '../cover_image.jpg';
import AddTrackForm from "../components/AddTrackForm";

const AlbumPage = () => {
    const { id } = useParams<{ id: string }>();
    const [album, setAlbum] = useState<Album | null>(null);
    const [albumCoverUrl, setAlbumCoverUrl] = useState<string>(defaultAvatar);
    const [isLoadingAlbum, setIsLoadingAlbum] = useState<boolean>(false);
    const [showModal, setShowModal] = useState(false);
    const [refreshTracks, setRefreshTracks] = useState<boolean>(false);

    const numericId = Number(id);

    const fetchAlbum = useCallback(async () => {
        setIsLoadingAlbum(true);
        try {
            const response = await albumApi.getAlbum(numericId);
            if (response.data) {
                setAlbum(response.data);
            } else {
                console.error('Альбом не найден');
            }
        } catch (error) {
            console.error('Ошибка загрузки альбома:', error);
            setAlbum(null);
        } finally {
            setIsLoadingAlbum(false);
        }
    }, [numericId, refreshTracks]);

    const fetchCover = useCallback(async () => {
        if (!album?.coverImageId) {
            setAlbumCoverUrl(defaultAvatar);
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
            setAlbumCoverUrl(defaultAvatar);
        }
    }, [album?.coverImageId]);

    useEffect(() => {
        fetchAlbum();
    }, [fetchAlbum]);

    useEffect(() => {
        if (album) {
            fetchCover();
        }
    }, [album, fetchCover]);

    const toggleModal = () => {
        setShowModal((prev) => !prev);
    };

    const handleTrackAdded = () => {
        setRefreshTracks(prev => !prev);
        toggleModal();
    };

    if (isLoadingAlbum) {
        return <div>Загрузка альбома...</div>;
    }

    if (!album) {
        return <div>Альбом не найден</div>;
    }

    return (
        <div className="album-page">
            <div className="album-info">
                <h1 className="album-title">{album.title}</h1>
                <p className="album-authors">
                    <strong>Авторы:</strong> {album.artists.join(', ')}
                </p>
                <div className="track-list">
                    <TrackList tracks={album.tracks}/>
                    <button onClick={toggleModal} className="custom-button">
                        Добавить трек
                    </button>
                    {showModal && (
                        <AddTrackForm
                            albumId={album.id}
                            onClose={() => setShowModal(false)}
                            onSuccess={() => {
                                setShowModal(false);
                            }}
                        />
                    )}
                </div>
            </div>
            <div className="album-cover-container">
                <img
                    src={albumCoverUrl}
                    alt={`Обложка альбома ${album.title}`}
                    className="album-cover"
                />
            </div>
        </div>
    );
};

export default AlbumPage;