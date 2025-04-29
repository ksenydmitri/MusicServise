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
        <Card className="album-card" sx={{ maxWidth: 250, position: 'relative' }}>
            <Link to={`/album/${album.id}`} className="album-link">
                <CardMedia
                    component="img"
                    height="200"
                    image={albumCoverUrl}
                    alt={album.title}
                    sx={{ objectFit: 'cover' }}
                />
                <CardContent>
                    <Typography gutterBottom variant="h6" component="div" noWrap>
                        {album.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" noWrap>
                        {album.artists.join(', ')}
                    </Typography>
                </CardContent>
            </Link>
            {onAddTrack && (
                <IconButton
                    aria-label="add track"
                    onClick={onAddTrack}
                    sx={{
                        position: 'absolute',
                        bottom: 16,
                        right: 16,
                        backgroundColor: 'primary.main',
                        color: 'white',
                        '&:hover': {
                            backgroundColor: 'primary.dark'
                        }
                    }}
                >
                    <Add />
                </IconButton>
            )}
        </Card>
    );
};

export default AlbumCard;