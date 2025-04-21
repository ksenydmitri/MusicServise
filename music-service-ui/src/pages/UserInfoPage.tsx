import React, { useState, useEffect } from 'react';
import { userApi } from '../api/api';
import './styles/userinfo.css';

const UserInfoPage = () => {
    const [user, setUser] = useState({
        id: 0,
        username: '',
        email: '',
    });
    const [formData, setFormData] = useState({
        username: '',
        email: '',
    });
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    // Fetch authorized user data from /auth/me
    useEffect(() => {
        const fetchUser = async () => {
            setLoading(true);
            try {
                const response = await userApi.getCurrentUser(); // Call /auth/me
                setUser(response.data);
                setFormData({
                    username: response.data.username,
                    email: response.data.email,
                });
            } catch (error: any) {
                setErrorMessage('Не удалось загрузить данные пользователя');
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
            await userApi.updateUser(user.id, formData); // Update authorized user data
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
            {!loading && (
                <form onSubmit={handleSubmit}>
                    <label htmlFor="username">Имя пользователя</label>
                    <input
                        type="text"
                        id="username"
                        name="username"
                        value={formData.username}
                        onChange={handleChange}
                        required
                    />
                    <label htmlFor="email">Email</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />
                    <button type="submit" disabled={loading}>
                        {loading ? 'Обновление...' : 'Обновить'}
                    </button>
                </form>
            )}
        </div>
    );
};

export default UserInfoPage;
