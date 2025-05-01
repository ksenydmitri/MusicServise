import React, { useCallback, useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Album } from "../types/album";
import { albumApi } from "../api/api";
import "./styles/albumPage.css";
import AddTrackForm from "../components/AddTrackForm";
import DeleteModal from "../components/DeleteModal";
import { useAuth } from "../context/AuthContext";
import useAlbumCover from "../hooks/useAlbumCover";

const AlbumPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth();

    const [album, setAlbum] = useState<Album | null>(null);
    const [isLoadingAlbum, setIsLoadingAlbum] = useState<boolean>(false);
    const [showModal, setShowModal] = useState(false);
    const [refreshTracks, setRefreshTracks] = useState<boolean>(false);
    const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);

    const numericId = Number(id);
    const albumCoverUrl = useAlbumCover(album?.coverImageId); // Используем кастомный хук

    const fetchAlbum = useCallback(async () => {
        setIsLoadingAlbum(true);
        try {
            const response = await albumApi.getAlbum(numericId);
            setAlbum(response.data || null);
        } catch (error) {
            console.error("Ошибка загрузки альбома:", error);
            setAlbum(null);
        } finally {
            setIsLoadingAlbum(false);
        }
    }, [numericId, refreshTracks]);

    useEffect(() => {
        fetchAlbum();
    }, [fetchAlbum]);

    const toggleModal = () => setShowModal((prev) => !prev);

    const handleTrackAdded = () => {
        setRefreshTracks((prev) => !prev);
        toggleModal();
    };

    const handleDeleteClick = () => setDeleteModalOpen(true);

    const handleConfirmDelete = async () => {
        try {
            await albumApi.deleteAlbum(album!.id);
            console.log("Альбом удален");
            setDeleteModalOpen(false);
            navigate("/albums");
        } catch (error) {
            console.error("Ошибка удаления альбома:", error);
        }
    };

    if (isLoadingAlbum) return <div className="page-container">Загрузка альбома...</div>;
    if (!album) return <div className="page-container">Альбом не найден</div>;

    return (
        <div className="page-container">
            <div className="content-card">
                <div className="album-page">
                    <div className="album-info">
                        <h1 className="album-title">{album.title}</h1>
                        {user && album.userIds.includes(user.id) && (
                            <>
                                <button onClick={toggleModal} className="custom-button">
                                    Добавить трек
                                </button>
                                <button onClick={handleDeleteClick} className="custom-button error">
                                    Удалить
                                </button>
                            </>
                        )}
                        <h2>Исполнители</h2>
                        <div className="album-title">{album.artists.join(", ")}</div>

                        <DeleteModal open={isDeleteModalOpen} onClose={() => setDeleteModalOpen(false)} onConfirm={handleConfirmDelete} />

                        <div className="track-list-section">
                            <h3>Треки</h3>
                            <div className="horizontal-scroll-list">
                                <div className="horizontal-scroll-container">
                                    {album.tracks.map((track, index) => (
                                        <div key={track.id} className="track-card">
                                            <span className="track-number">{index + 1}</span>
                                            <h4 className="track-title">{track.title}</h4>
                                            <p className="track-duration">{track.duration}</p>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {showModal && <AddTrackForm albumId={album.id} onClose={() => setShowModal(false)} onSuccess={handleTrackAdded} />}
                    </div>

                    <div className="album-cover-container">
                        <img src={albumCoverUrl} alt={`Обложка альбома ${album.title}`} className="album-cover" />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AlbumPage;
