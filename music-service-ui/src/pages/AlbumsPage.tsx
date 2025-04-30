import AlbumCard from "../components/AlbumCard";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";
import { albumApi } from "../api/api";
import { Album } from "../types/album";
import { Button } from "@mui/material";
import CreateAlbumModal from "../components/CreateAlbumForm";
import "./styles/global.css"


const AlbumsPage = () => {
    const { user, isAuthenticated } = useAuth();
    const [albums, setAlbums] = useState<Album[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [openModal, setOpenModal] = useState<boolean>(false); // Управление модальным окном

    const fetchAlbums = async (page: number) => {
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
                user: user.username,
            });

            if (response.data.content) {
                setAlbums(response.data.content);
                setTotalPages(response.data.totalPages);
                setCurrentPage(page);
            } else {
                setAlbums([]);
                setTotalPages(0);
            }
        } catch (error) {
            setErrorMessage("Ошибка загрузки альбомов. Попробуйте снова.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAlbums(0);
    }, [isAuthenticated, user]);

    const handlePageChange = async (page: number) => {
        await fetchAlbums(page);
    };

    if (!isAuthenticated) {
        return <div className="auth-message">Пожалуйста, авторизуйтесь для просмотра альбомов</div>;
    }

    return (
        <div className="page-container">
            <div>
                <Button variant="contained" color="primary" onClick={() => setOpenModal(true)} className="">
                    Создать альбом
                </Button>
            </div>

            {loading ? (
                <p className="loading-indicator">Загрузка...</p>
            ) : errorMessage ? (
                <p className="error-message">{errorMessage}</p>
            ) : (
                <>
                    <div>
                        {albums.map((album) => (
                            <AlbumCard key={album.id} album={album} />
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div>
                            {Array.from({ length: totalPages }, (_, index) => (
                                <button
                                    key={index}
                                    onClick={() => handlePageChange(index)}
                                    disabled={currentPage === index}
                                    className={`pagination-button ${currentPage === index ? "active" : ""}`}
                                >
                                    {index + 1}
                                </button>
                            ))}
                        </div>
                    )}
                </>
            )}

            <CreateAlbumModal open={openModal} handleClose={() => setOpenModal(false)} />
        </div>
    );
};

export default AlbumsPage;
