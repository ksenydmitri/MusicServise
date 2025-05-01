import React, { useState, useEffect } from 'react';
import { userApi } from '../api/api';
import './styles/login.css';
import { useAuth } from '../context/AuthContext';

interface UserData {
    id: number;
    username: string;
    email: string;
    role: string;
}

const UserInfoPage = () => {
    const { user, login } = useAuth();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
    });
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    useEffect(() => {
        if (user) {
            setFormData({
                username: user.username,
                email: user.email || '',
                password: '',
            });
        }
    }, [user]);

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
            if (!user) {
                throw new Error('Пользователь не авторизован');
            }

            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('Токен не найден');
            }

            const dataToUpdate: Record<string, string> = {
                username: formData.username,
                email: formData.email,
            };

            if (formData.password) {
                dataToUpdate.password = formData.password;
            }

            const updateResponse = await userApi.updateUser(user.id, dataToUpdate, token);
            const newToken = updateResponse.data?.token || token;
            const updatedUserData = updateResponse.data?.user || {
                id: user.id,
                username: formData.username,
                email: formData.email,
            };

            login(updatedUserData, newToken);
            setSuccessMessage('Данные пользователя успешно обновлены');
        } catch (error: any) {
            console.error('Ошибка обновления:', error);
            setErrorMessage(error.response?.data?.message || 'Ошибка при обновлении данных пользователя');
        } finally {
            setLoading(false);
        }
    };

    if (!user) {
        return <div className="loginContainer">Пользователь не авторизован</div>;
    }

    return (
        <div className="loginContainer">
            <h1>Информация о пользователе</h1>
            {loading && <p>Загрузка данных...</p>}
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            {successMessage && <p className="success-message">{successMessage}</p>}

            <div className="loginPaper">
                <h2>Текущие данные</h2>
                <div className="info-grid">
                    <div>
                        <span>Имя пользователя:</span>
                        <span>{user.username}</span>
                    </div>
                    <div>
                        <span>Email:</span>
                        <span>{user.email}</span>
                    </div>
                </div>
            </div>

            <div className="loginPaper">
                <h2>Обновить данные</h2>
                <form onSubmit={handleSubmit}>
                    <div>
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

                    <div>
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

                    <div>
                        <label htmlFor="password">Новый пароль</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            minLength={6}
                            maxLength={50}
                            placeholder="Оставьте пустым, чтобы не менять"
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