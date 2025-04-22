import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/api';
import { useAuth } from '../context/AuthContext';
import { AuthRequest } from '../types/auth';
import './styles/login.css';

const LoginPage = () => {
    const [formData, setFormData] = useState<AuthRequest>({
        username: '',
        password: '',
    });
    const { login } = useAuth();
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMessage(null);
        try {
            const response = await authApi.login(
                formData.username,
                formData.password
            );

            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
                login();
                navigate('/');
            } else {
                console.error('Токен не получен:', response.data);
                setErrorMessage('Ошибка: Токен не получен от сервера.');
            }
        } catch (error) {
            if (error instanceof Error) {
                setErrorMessage((error as any)?.response?.data?.error || 'Ошибка входа.');
                console.error('Ошибка при входе:', error.message);
            } else {
                setErrorMessage('Неизвестная ошибка.');
                console.error('Неизвестная ошибка:', error);
            }
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };
// Удаляем токен при выходе
    return (
        <div className="login-page">
            <h1>Вход</h1>
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    name="username"
                    placeholder="Имя пользователя"
                    value={formData.username}
                    onChange={handleChange}
                    required
                />
                <input
                    type="password"
                    name="password"
                    placeholder="Пароль"
                    value={formData.password}
                    onChange={handleChange}
                    required
                />
                <button type="submit">Войти</button>
            </form>
            <p>
                Нет аккаунта? <Link to="/register">Зарегистрируйтесь</Link>
            </p>
        </div>
    );
};

export default LoginPage;
