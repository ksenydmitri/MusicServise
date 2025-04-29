import React, { useState} from 'react';
import {albumApi} from '../api/api';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './styles/createalbum.css';

const CreateAlbumPage = () => {
    const {user, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        title: '',
        coverImage: null as File | null
    });

    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [previewImage, setPreviewImage] = useState<string | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setFormData(prev => ({ ...prev, coverImage: file }));

            const reader = new FileReader();
            reader.onloadend = () => {
                setPreviewImage(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!isAuthenticated) {
            setErrorMessage('Требуется авторизация');
            return;
        }

        setLoading(true);
        setSuccessMessage(null);
        setErrorMessage(null);

        if (!user) return <div>Пожалуйста, войдите в систему</div>;

        try {
            const formDataToSend = new FormData();

            const requestData = {
                name: formData.title,
                userId: user?.id,
            };

            formDataToSend.append(
                'request',
                new Blob([JSON.stringify(requestData)], {
                    type: 'application/json; charset=UTF-8'
                }),
                'request.json'
            );

            if (formData.coverImage) {
                formDataToSend.append('file', formData.coverImage);
            }

            const response = await albumApi.createAlbum(formDataToSend);

            setSuccessMessage('Альбом успешно создан!');
            setFormData({ title: '', coverImage: null });
            setPreviewImage(null);
            navigate(`/album/${response.data.id}`);
        } catch (error: any) {
            setErrorMessage(error?.response?.data?.message || 'Ошибка при создании альбома');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="create-album-page">
            <h1>Создать новый альбом</h1>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Название альбома:</label>
                    <input
                        type="text"
                        name="title"
                        value={formData.title}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label>Обложка альбома:</label>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageChange}
                    />
                    {previewImage && (
                        <div className="image-preview">
                            <img src={previewImage} alt="Предпросмотр обложки" />
                        </div>
                    )}
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? 'Создание...' : 'Создать альбом'}
                </button>
            </form>

            {successMessage && <div className="alert success">{successMessage}</div>}
            {errorMessage && <div className="alert error">{errorMessage}</div>}
        </div>
    );
};

export default CreateAlbumPage;