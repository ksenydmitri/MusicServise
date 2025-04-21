import React, { useState } from 'react';
import { albumApi } from '../api/api'; // Подключаем API
import './styles/createalbum.css'; // Подключите стили, если нужны

const CreateAlbumPage = () => {
    const [formData, setFormData] = useState({
        title: '',
        artist: '',
        description: '',
    });

    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setSuccessMessage(null);
        setErrorMessage(null);

        try {
            const response = await albumApi.createAlbum(formData); // Отправляем запрос для создания альбома
            setSuccessMessage('Альбом успешно создан!');
            console.log('API Response:', response.data);
            setFormData({ title: '', artist: '', description: '' });// Сбрасываем форму
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
                <input
                    type="text"
                    name="title"
                    placeholder="Название альбома"
                    value={formData.title}
                    onChange={handleChange}
                    required
                />
                <input
                    type="text"
                    name="artist"
                    placeholder="Исполнитель"
                    value={formData.artist}
                    onChange={handleChange}
                    required
                />
                <textarea
                    name="description"
                    placeholder="Описание"
                    value={formData.description}
                    onChange={handleChange}
                    required
                ></textarea>
                <button type="submit" disabled={loading}>
                    {loading ? 'Создание...' : 'Создать'}
                </button>
            </form>
            {successMessage && <p className="success-message">{successMessage}</p>}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
        </div>
    );
};

export default CreateAlbumPage;
