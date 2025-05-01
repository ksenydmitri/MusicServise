import React, { useState, useEffect } from 'react';
import { authApi, userApi } from '../api/api';
import './styles/login.css';
import { useAuth } from '../context/AuthContext'; // Импортируем контекст аутентификации

interface UserData {
    id: number;
    username: string;
    email: string;
    role: string;
}

const UserInfoPage = () => {
    const { user: authUser, login } = useAuth(); // Получаем данные и методы из контекста
    const [user, setUser] = useState<UserData>({
        id: 0,
        username: '',
        email: '',
        role: '',
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
                const token = localStorage.getItem('token');
                if (!token) {
                    throw new Error('Токен не найден');
                }

                const response = await authApi.getCurrentUser(token);

                if (!response.data) {
                    throw new Error('Пустой ответ от сервера');
                }

                const userData: UserData = {
                    id: response.data.id || 0,
                    username: response.data.username || '',
                    email: response.data.email || '',
                    role: response.data.role || 'USER',
                };

                setUser(userData);
                setFormData({
                    username: userData.username,
                    email: userData.email,
                    password: '', // Пароль остается пустым для безопасности
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

            // Обновляем данные пользователя
            const updateResponse = await userApi.updateUser(user.id, dataToUpdate, token);

            // После успешного обновления получаем новый токен (если сервер его возвращает)
            // или используем старый, если сервер не возвращает новый токен
            const newToken = updateResponse.data?.token || token;

            // Если сервер возвращает обновленные данные пользователя, используем их
            const updatedUserData = updateResponse.data?.user || {
                id: user.id,
                username: formData.username,
                email: formData.email,
            };

            // Обновляем контекст аутентификации
            login({
                id: updatedUserData.id,
                username: updatedUserData.username,
                email: updatedUserData.email,
            }, newToken);

            // Обновляем локальное состояние
            setUser(prev => ({
                ...prev,
                username: updatedUserData.username,
                email: updatedUserData.email,
            }));

            setSuccessMessage('Данные пользователя успешно обновлены');
        } catch (error: any) {
            console.error('Ошибка обновления:', error);
            setErrorMessage(error.response?.data?.message || 'Ошибка при обновлении данных пользователя');
        } finally {
            setLoading(false);
        }
    };

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
                    <div>
                        <span>Роль:</span>
                        <span>{user.role}</span>
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