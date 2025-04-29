import './styles/home.css';
import { useNavigate } from 'react-router-dom';
import AlbumCard  from "../components/AlbumCard"
import {useAuth} from "../context/AuthContext";
import {useEffect, useState} from "react";
import {albumApi, trackApi} from "../api/api";
import {Album} from "../types/album";
import TrackCard from "../components/TrackCard";

const AlbumsPage = () => {
    const { user } = useAuth()
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const [albums, setAlbums] = useState<Album[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);

    const fetchAlbums = async (page: number, user: string, signal: AbortSignal)=> {
        setLoading(true);
        setErrorMessage(null);
        try {
            const response = await albumApi.getAlbums({page, user, signal});
            console.log('Ответ сервера:', response.data);

            if (response.data.content) {
                setAlbums(response.data.content);
                setTotalPages(response.data.totalPages);
                setCurrentPage(page);
            } else {
                console.error('Поле "content" отсутствует:', response.data);
                setAlbums([]);
                setTotalPages(0);
            }
        } catch (error: any) {
            if (signal.aborted) {
                console.log('Запрос был отменен');
            } else {
                console.error('Ошибка загрузки треков:', error);
                setErrorMessage('Ошибка загрузки треков. Попробуйте снова.');
            }
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        const fetchAlbums = async () => {
            try {
                const response = await albumApi.getAlbums({});
                console.log('Ответ сервера:', response.data);

                if (response.data.content) {
                    setAlbums(response.data.content);
                } else {
                    console.error('Поле "content" отсутствует:', response.data);
                    setAlbums([]);
                }
            } catch (error) {
                console.error('Error fetching tracks:', error);
            }
        };

        fetchAlbums().catch((error) => console.error('Promise Error:', error));
    },[]);

    const handlePageChange = async (page: number) => {
        const controller = new AbortController();
        const { signal } = controller;

        if(!user){
            return <div>Проблема авторизации</div>;
        }
        await fetchAlbums(page,user?.username, signal);
    };

    return (
        <div className="albums-page">

            {loading ? (
                <p className="loading-indicator">Loading...</p>
            ) : errorMessage ? (
                <p className="error-message">{errorMessage}</p>
            ) : (
                <div className="album-list">
                    {albums.map(album => (
                        <AlbumCard
                            key={album.id}
                            album={album}
                        />
                    ))}
                </div>
            )}

            <div className="pagination">
                {Array.from({length: totalPages}, (_, index) => (
                    <button
                        key={index}
                        onClick={() => handlePageChange(index)}
                        disabled={currentPage === index}
                        className={`pagination-button ${currentPage === index ? 'active' : ''}`}
                    >
                        {index + 1}
                    </button>
                ))}
            </div>
        </div>
    );
}

export default AlbumsPage;