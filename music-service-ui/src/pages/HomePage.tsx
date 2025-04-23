import { useEffect, useState } from 'react';
import { trackApi } from '../api/api';
import TrackCard from '../components/TrackCard';
import { Track } from '../types/track';
import './styles/home.css';

const HomePage = () => {
    const [tracks, setTracks] = useState<Track[]>([]);
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

                // Получаем треки из поля "content"
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

        fetchTracks().catch((error) => console.error('Promise Error:', error));
    }, []);


    // Обработчик поиска
    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await trackApi.getTracks({ title: searchTerm });

            // Получаем треки из поля "content"
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


    // Обработчик переключения страницы
    const handlePageChange = async (page: number) => {
        const controller = new AbortController();
        const { signal } = controller;

        await fetchTracks(page, searchTerm, signal); // Загрузка новой страницы с текущим запросом
    };

    return (
        <div className="home-page">
            <form onSubmit={handleSearch} className="search-form">
                <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Search tracks..."
                    className="search-input"
                />
                <button type="submit" className="search-button">Search</button>
            </form>

            {loading ? (
                <p className="loading-indicator">Loading...</p>
            ) : errorMessage ? (
                <p className="error-message">{errorMessage}</p>
            ) : (
                <div className="track-list">
                    {tracks.length === 0 ? (
                        <p>No tracks found</p>
                    ) : (
                        tracks.map((track) => <TrackCard key={track.id} track={track} />)
                    )}
                </div>
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
