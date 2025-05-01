import { useEffect, useState } from 'react';
import {albumApi, trackApi} from '../api/api';
import TrackCard from '../components/TrackCard';
import { Track } from '../types/track';
import { Album } from  '../types/album'
import './styles/global.css';
import AlbumCard from "../components/AlbumCard";

const HomePage = () => {
    const [tracks, setTracks] = useState<Track[]>([]);
    const [albums, setAlbums] = useState<Album[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);


    const fetchTracks = async (page: number, term: string, signal: AbortSignal) => {
        setLoading(true);
        setErrorMessage(null);
        try {
            const response = await trackApi.getTracks({page, term, signal, title: searchTerm });
            console.log('Ответ сервера:', response.data);

            if (response.data.content) {
                setTracks(response.data.content);
                setTotalPages(response.data.totalPages);
                setCurrentPage(page);
            } else {
                console.error('Поле "content" отсутствует:', response.data);
                setTracks([]);
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
    };

    useEffect(() => {
        const fetchTracks = async () => {
            try {
                const response = await trackApi.getTracks({});
                console.log('Ответ сервера:', response.data);

                if (response.data.content) {
                    setTracks(response.data.content);
                } else {
                    console.error('Поле "content" отсутствует:', response.data);
                    setTracks([]);
                }
            } catch (error) {
                console.error('Error fetching tracks:', error);
            }
        };
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
                console.error('Ошибка загрузки альбомов:', error);
            }
        };

        fetchTracks().catch((error) => console.error('Promise Error:', error));
        fetchAlbums().catch((error) => console.error('Promise Error:', error));
    }, []);

    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await trackApi.getTracks({ title: searchTerm });

            if (response.data.content) {
                setTracks(response.data.content);
            } else {
                console.error('Поле "content" отсутствует:', response.data);
                setTracks([]);
            }
        } catch (error) {
            console.error('Error searching tracks:', error);
        }
    };

    const handlePageChange = async (page: number) => {
        const controller = new AbortController();
        const { signal } = controller;

        await fetchTracks(page, searchTerm, signal);// Загрузка новой страницы с текущим запросом
    };

    return (
        <div className="page-container">
            <form onSubmit={handleSearch} className="search-form-container">
                <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Search tracks..."
                />
                <button type="submit">Search</button>
            </form>

            {loading ? (
                <p className="loading-indicator">Loading...</p>
            ) : errorMessage ? (
                <p className="error-message">{errorMessage}</p>
            ) : (
                <>
                    {/* Список треков */}
                    <div className="horizontal-scroll-list">
                        <h3>Popular Tracks</h3>
                        {tracks.length === 0 ? (
                            <p>No tracks found</p>
                        ) : (
                            <div className="horizontal-scroll-container">
                                {tracks.map((track) => (
                                    <TrackCard key={`track-${track.id}`} track={track} />
                                ))}
                            </div>
                        )}
                    </div>
                    <div className="horizontal-scroll-list">
                        <h3>Featured Albums</h3>
                        {albums.length === 0 ? (
                            <p>No albums found</p>
                        ) : (
                            <div className="horizontal-scroll-container">
                                {albums.map((album) => (
                                    <AlbumCard key={`album-${album.id}`} album={album} />
                                ))}
                            </div>
                        )}
                    </div>
                </>
            )}

            <div className="pagination">
                {Array.from({ length: totalPages }, (_, index) => (
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
};

export default HomePage;
