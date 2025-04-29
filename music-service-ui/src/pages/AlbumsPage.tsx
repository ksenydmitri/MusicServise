import './styles/home.css';
import AlbumCard from "../components/AlbumCard";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";
import { albumApi } from "../api/api";
import { Album } from "../types/album";

const AlbumsPage = () => {
    const { user, isAuthenticated } = useAuth();
    const [albums, setAlbums] = useState<Album[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);

    const fetchAlbums = async (page: number, signal?: AbortSignal) => {
        if (!isAuthenticated || !user) {
            setErrorMessage("Требуется авторизация");
            setAlbums([]);
            return;
        }

        setLoading(true);
        setErrorMessage(null);

        try {
            const response = await albumApi.getAlbums({
                page,
                user: user.username
            });

            if (response.data.content) {
                setAlbums(response.data.content);
                setTotalPages(response.data.totalPages);
                setCurrentPage(page);
            } else {
                setAlbums([]);
                setTotalPages(0);
            }
        } catch (error: any) {
            if (!signal?.aborted) {
                setErrorMessage('Ошибка загрузки альбомов. Попробуйте снова.');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const controller = new AbortController();
        fetchAlbums(0, controller.signal);

        return () => controller.abort();
    }, [isAuthenticated, user]); // Добавляем зависимости

    const handlePageChange = async (page: number) => {
        const controller = new AbortController();
        await fetchAlbums(page, controller.signal);
    };

    if (!isAuthenticated) {
        return <div className="auth-message">Пожалуйста, авторизуйтесь для просмотра альбомов</div>;
    }

    return (
        <div className="albums-page">
            {loading ? (
                <p className="loading-indicator">Загрузка...</p>
            ) : errorMessage ? (
                <p className="error-message">{errorMessage}</p>
            ) : (
                <>
                    <div className="album-list">
                        {albums.map(album => (
                            <AlbumCard
                                key={album.id}
                                album={album}
                            />
                        ))}
                    </div>

                    {totalPages > 1 && (
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
                    )}
                </>
            )}
        </div>
    );
};

export default AlbumsPage;