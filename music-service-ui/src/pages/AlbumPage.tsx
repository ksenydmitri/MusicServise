import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { albumApi } from '../api/api';
import TrackList from '../components/TrackList';
import { Album } from '../types/album';
import './styles/albumPage.css';

const AlbumPage = () => {
    const { id } = useParams<{ id: string }>();
    const [album, setAlbum] = useState<Album | null>(null);

    useEffect(() => {
        const fetchAlbum = async () => {
            if (id) {
                try {
                    const response = await albumApi.getAlbum(parseInt(id));
                    setAlbum(response.data);
                } catch (error) {
                    console.error('Error fetching album:', error);
                }
            }
        };
        fetchAlbum();
    }, [id]);

    if (!album) return <div>Загрузка...</div>;

    return (
        <div className="album-page">
            <h1>{album.title}</h1>
            <TrackList tracks={album.tracks} />
        </div>
    );
};

export default AlbumPage;