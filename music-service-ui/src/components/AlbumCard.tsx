import { Link } from 'react-router-dom';
import { Album } from '../types/album';
import './styles/albumCard.css';
import { useState, useCallback, useEffect } from "react";
import defaultAvatar from "../cover_image.jpg";
import { mediaApi } from "../api/api";
import { Card, CardMedia, CardContent, Typography, IconButton } from '@mui/material';
import { Add } from '@mui/icons-material';

interface AlbumCardProps {
    album: Album;
    onAddTrack?: () => void;
}

const AlbumCard = ({ album, onAddTrack }: AlbumCardProps) => {
    const [albumCoverUrl, setAlbumCoverUrl] = useState<string>(defaultAvatar);

    useEffect(() => {
        fetchCover();
    }, [album.coverImageId]);

    const fetchCover = useCallback(async () => {
        if (!album.coverImageId) {
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
    }, [album.coverImageId]);

    return (
        <div className="albums-container">
            <Card component={Link} to={`/album/${album.id}`}>
                <CardMedia
                    component="img"
                    image={albumCoverUrl}
                    alt={album.title}
                />
                <CardContent>
                    <Typography variant="h6">{album.title}</Typography>
                    <Typography variant="body2">
                        {album.artists.join(', ')}
                    </Typography>
                </CardContent>
                {onAddTrack && (
                    <IconButton
                        aria-label="add track"
                        onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            onAddTrack();
                        }}
                    >
                        <Add />
                    </IconButton>
                )}
            </Card>
        </div>
    );
};

export default AlbumCard;