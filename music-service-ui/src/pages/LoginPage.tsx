import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/api'; // Подключаем ваш API
import { useAuth } from '../context/AuthContext'; // Подключаем контекст авторизации
import { AuthRequest } from '../types/auth';
import './styles/login.css';

const LoginPage = () => {
    const [formData, setFormData] = useState<AuthRequest>({
        username: '',
        password: '',
    });
    const { login } = useAuth(); // Получаем функцию login из контекста
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await authApi.login(formData.username, formData.password);

            if (response.data.token) {
                localStorage.setItem('token', response.data.token); // Сохраняем токен
                login(); // Обновляем состояние авторизации в контексте
                navigate('/'); // Перенаправляем на главную страницу
            } else {
                console.error('Токен не получен:', response.data);
            }
        } catch (error) {
            if (error instanceof Error) {
                console.error('Ошибка при входе:', (error as any)?.response?.data || error.message);
            } else {
                console.error('Неизвестная ошибка:', error);
            }
        }

    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    return (
        <div className="login-page">
            <h1>Вход</h1>
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
