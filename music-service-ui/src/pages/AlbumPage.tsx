import React, { useCallback, useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Album } from "../types/album";
import { albumApi, trackApi } from "../api/api"; // Добавили trackApi для удаления треков
import "./styles/albumPage.css";
import AddTrackForm from "../components/AddTrackForm";
import DeleteModal from "../components/DeleteModal";
import { useAuth } from "../context/AuthContext";
import useAlbumCover from "../hooks/useAlbumCover";
import {Track} from "../types/track";
import CreateAlbumModal from "../components/CreateAlbumForm";
import EditAlbumModal from "../components/EditAlbumModal";

const AlbumPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth();

    const [album, setAlbum] = useState<Album | null>(null);
    const [isLoadingAlbum, setIsLoadingAlbum] = useState<boolean>(false);
    const [showModal, setShowModal] = useState(false);
    const [refreshTracks, setRefreshTracks] = useState<boolean>(false);
    const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
    const [isTrackDeleteModalOpen, setTrackDeleteModalOpen] = useState(false);
    const [trackToDelete, setTrackToDelete] = useState<Track | null>(null);
    const [isAlbumEditModalOpen, setAlbumEditModalOpen] = useState(false);

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

    const handleEditClick = () => setAlbumEditModalOpen(true);

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

    const handleTrackDeleteClick = (track: Track) => {
        setTrackToDelete(track);
        setTrackDeleteModalOpen(true);
    };

    const handleConfirmTrackDelete = async () => {
        if (!trackToDelete) return;

        try {
            await trackApi.deleteTrack(trackToDelete.id);
            console.log(`Трек удален: ${trackToDelete.title}`);
            setTrackDeleteModalOpen(false);
            setRefreshTracks((prev) => !prev);
        } catch (error) {
            console.error("Ошибка удаления трека:", error);
        }
    };

    if (isLoadingAlbum) return <div className="page-container">Загрузка альбома...</div>;
    if (!album) return <div className="page-container">Альбом не найден</div>;

    return (
        <div className="page-container">
            <div className="content-card album-grid">
                <div className="album-title">{album.title}
                    {user && album.userIds.includes(user.id) && (
                        <div className="button-container">
                            <button onClick={handleDeleteClick} className="custom-button add-track-button">Удалить</button>
                            <button onClick={handleEditClick} className="custom-button add-track-button">Изменить</button>
                        </div>
                    )}
                </div>

                <div className="album-authors">
                    <DeleteModal open={isDeleteModalOpen} onClose={() => setDeleteModalOpen(false)} onConfirm={handleConfirmDelete} />
                    <EditAlbumModal
                        album={album}
                        open={isAlbumEditModalOpen}
                        handleClose={() => setAlbumEditModalOpen(false)}
                        onAlbumUpdated={(updatedAlbum) => setAlbum(updatedAlbum)}
                    />

                    <h2>Исполнители</h2>

                    <p>{album.artists.join(", ")}</p>
                </div>

                <div className="album-cover-container">
                    <img src={albumCoverUrl} alt={`Обложка альбома ${album.title}`} className="album-cover" />
                </div>

                <div className="track-header">
                    <h3>Треки</h3>
                </div>
                <div className="add-track-button">
                    {user && album.userIds.includes(user.id) && (
                        <button onClick={toggleModal} className="custom-button add-track-btn">Добавить трек</button>
                    )}
                </div>

                <div className="track-table-section">
                <table className="track-table">
                        <thead>
                        <tr>
                            <th>№</th>
                            <th>Название</th>
                            <th>Длительность</th>
                            {user && album.userIds.includes(user.id) && <th>Действия</th>}
                        </tr>
                        </thead>
                        <tbody>
                        {album.tracks.map((track, index) => (
                            <tr key={track.id}>
                                <td>{index + 1}</td>
                                <td>{track.title}</td>
                                <td>{track.duration}</td>
                                {user && album.userIds.includes(user.id) && (
                                    <td>
                                        <button onClick={() => handleTrackDeleteClick(track)} className="custom-button error">
                                            Удалить
                                        </button>
                                    </td>
                                )}
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {showModal && <AddTrackForm albumId={album.id} onClose={() => setShowModal(false)} onSuccess={handleTrackAdded} />}
            </div>
        </div>
    );
};

export default AlbumPage;
