import React, { useState, useEffect } from 'react';
import { userApi } from '../api/api';
import './styles/userinfo.css';

interface Album {
    id: number;
    title: string;
    // добавьте другие поля альбома по необходимости
}

interface UserData {
    id: number;
    username: string;
    email: string;
    role: string;
    albums: Album[];
}

const UserInfoPage = () => {
    const [user, setUser] = useState<UserData>({
        id: 0,
        username: '',
        email: '',
        role: '',
        albums: [],
    });
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
    });
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    useEffect(() => {
        const fetchUser = async () => {
            setLoading(true);
            try {
                const response = await userApi.getCurrentUser();

                if (!response.data) {
                    throw new Error('Пустой ответ от сервера');
                }

                const userData: UserData = {
                    id: response.data.id || 0,
                    username: response.data.username || '',
                    email: response.data.email || '',
                    role: response.data.role || 'USER',
                    albums: response.data.albums || []
                };

                setUser(userData);
                setFormData({
                    username: userData.username,
                    email: userData.email,
                    password: ''
                });

            } catch (error: any) {
                console.error('Ошибка получения данных:', error);
                setErrorMessage(error.message || 'Ошибка загрузки данных');
            } finally {
                setLoading(false);
            }
        };

        fetchUser();
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setErrorMessage(null);
        setSuccessMessage(null);

        try {
            await userApi.updateUser(user.id, formData);
            // Обновляем данные пользователя после успешного обновления
            const updatedUser = await userApi.getCurrentUser();
            setUser(updatedUser.data);
            setSuccessMessage('Данные пользователя успешно обновлены');
        } catch (error: any) {
            setErrorMessage('Ошибка при обновлении данных пользователя');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="user-info-page">
            <h1>Информация о пользователе</h1>
            {loading && <p>Загрузка данных...</p>}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            {successMessage && <p className="success-message">{successMessage}</p>}

            <div className="user-info-section">
                <h2>Текущие данные</h2>
                <div className="user-info-grid">
                    <div className="info-item">
                        <span className="info-label">ID:</span>
                        <span className="info-value">{user.id}</span>
                    </div>
                    <div className="info-item">
                        <span className="info-label">Имя пользователя:</span>
                        <span className="info-value">{user.username}</span>
                    </div>
                    <div className="info-item">
                        <span className="info-label">Email:</span>
                        <span className="info-value">{user.email}</span>
                    </div>
                    <div className="info-item">
                        <span className="info-label">Роль:</span>
                        <span className="info-value">{user.role}</span>
                    </div>
                </div>

                {user.albums && user.albums.length > 0 && (
                    <div className="albums-section">
                        <h3>Альбомы пользователя</h3>
                        <ul className="albums-list">
                            {user.albums.map(album => (
                                <li key={album.id} className="album-item">
                                    {album.title}
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>

            <div className="update-form-section">
                <h2>Обновить данные</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="username">Имя пользователя</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            minLength={3}
                            maxLength={50}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Новый пароль (оставьте пустым, чтобы не менять)</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            minLength={6}
                            maxLength={50}
                        />
                    </div>

                    <button type="submit" disabled={loading}>
                        {loading ? 'Обновление...' : 'Обновить данные'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default UserInfoPage;