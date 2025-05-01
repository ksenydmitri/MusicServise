import { Track } from '../types/track';
import { mediaApi, trackApi } from '../api/api';
import './styles/trackCard.css';
import { useState, useEffect, useCallback } from 'react';
import defaultAvatar from '../cover_image.jpg';

interface TrackCardProps {
    track: Track;
}

const TrackCard = ({ track }: TrackCardProps) => {
    const [albumCoverUrl, setAlbumCoverUrl] = useState<string>(defaultAvatar);
    const [audioUrl, setAudioUrl] = useState<string | undefined>();
    const [isLoading, setIsLoading] = useState(false);

    const fetchAlbumCover = useCallback(async () => {
        if (!track.album.coverImageId) {
            console.log('Обложка альбома отсутствует, используется стандартная аватарка.'+track.album.title);
            setAlbumCoverUrl(defaultAvatar);
            return;
        }

        console.log(`Попытка загрузки обложки альбома с ID: ${track.album.coverImageId}`);
        setIsLoading(true);

        try {
            const response = await mediaApi.downloadMedia(track.album.coverImageId);
            console.log('Успешный ответ от API при загрузке обложки:', response);
            const contentType = response.headers['content-type'] || 'image/jpeg';
            const blob = new Blob([response.data], { type: contentType });
            const url = URL.createObjectURL(blob);

            setAlbumCoverUrl(url);
            console.log(`Обложка успешно загружена: ${url}`);
        } catch (error) {
            console.error('Ошибка при загрузке обложки альбома:', error);
            setAlbumCoverUrl(defaultAvatar);
        } finally {
            setIsLoading(false);
        }
    }, [track.album.coverImageId]);

    useEffect(() => {
        console.log('Запуск функции fetchAlbumCover...');
        fetchAlbumCover();

        // Очистка URL для предотвращения утечек памяти
        return () => {
            if (albumCoverUrl && albumCoverUrl !== defaultAvatar) {
                console.log('Очистка временного URL обложки альбома:', albumCoverUrl);
                URL.revokeObjectURL(albumCoverUrl);
            }
        };
    }, [fetchAlbumCover]);

    const playTrack = async () => {
        console.log(`Попытка воспроизведения трека с ID: ${track.mediaFileId}`);
        try {
            const response = await trackApi.downloadTrack(track.mediaFileId);
            console.log('Успешный ответ от API при загрузке трека:', response);

            const url = window.URL.createObjectURL(new Blob([response.data]));
            setAudioUrl(url);
            console.log(`Трек успешно подготовлен для воспроизведения: ${url}`);
        } catch (error) {
            console.error('Ошибка при воспроизведении трека:', error);
        }
    };

    const downloadTrack = async () => {
        console.log(`Попытка скачивания трека с ID: ${track.mediaFileId}`);
        try {
            const response = await trackApi.downloadTrack(track.mediaFileId);
            console.log('Успешный ответ от API при скачивании трека:', response);

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `${track.title}.mp3`);
            document.body.appendChild(link);
            link.click();

            console.log(`Трек успешно скачан: ${url}`);

            // Очистка после скачивания
            setTimeout(() => {
                document.body.removeChild(link);
                URL.revokeObjectURL(url);
                console.log('Очистка временного URL трека.');
            }, 100);
        } catch (error) {
            console.error('Ошибка при скачивании трека:', error);
        }
    };

    return (
        <div className="track-card">
            <div className="track-avatar">
                {isLoading ? (
                    <div className="avatar-loading">Загрузка...</div>
                ) : (
                    <img
                        src={albumCoverUrl}
                        alt={`${track.title} album cover`}
                        className="circular-avatar"
                        onError={() => {
                            console.warn('Ошибка загрузки изображения, используется стандартная аватарка.');
                            setAlbumCoverUrl(defaultAvatar);
                        }}
                    />
                )}
            </div>
            <h3>{track.title}</h3>
            <p>Длительность: {track.duration}</p>
            <p>Исполнитель: {track.usernames}</p>
            <button onClick={playTrack} >Воспроизвести</button>
            <button onClick={downloadTrack}>Скачать</button>

            {audioUrl && (
                <audio
                    controls
                    onEnded={() => {
                        console.log('Воспроизведение завершено, очищаем URL...');
                        URL.revokeObjectURL(audioUrl);
                        setAudioUrl(undefined);
                    }}
                >
                    <source src={audioUrl} type="audio/mpeg" className="audio-player" />
                    Ваш браузер не поддерживает аудиоплеер.
                </audio>
            )}
        </div>
    );
};

export default TrackCard;
