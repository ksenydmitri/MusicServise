import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './styles/register.css';
import { authApi } from '../api/api';
import { RegisterRequest } from "../types/auth";
import { useAuth } from "../context/AuthContext";

const RegisterPage = () => {
    const [formData, setFormData] = useState<RegisterRequest>({
        username: '',
        email: '',
        password: '',
    });

    const { login } = useAuth();
    const navigate = useNavigate();

    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Базовая валидация на клиенте
        if (!formData.username || !formData.password || !formData.email) {
            setErrorMessage("Все поля обязательны для заполнения!");
            return;
        }

        setIsLoading(true); // Включение состояния загрузки
        setErrorMessage(null); // Очистка предыдущих сообщений об ошибке

        try {
            // Отправка данных на сервер
            const response = await authApi.register(
                formData.username, formData.email,formData.password
            );

            if (response.data.token) {
                localStorage.setItem('token', response.data.token); // Сохранение токена
                login(); // Обновление состояния авторизации
                navigate('/'); // Перенаправление на главную страницу
            } else {
                setErrorMessage('Не удалось получить токен. Попробуйте снова.');
            }
        } catch (error: any) {
            const errorResponse = error?.response?.data?.error || "Неизвестная ошибка сервера";
            setErrorMessage(errorResponse);
        } finally {
            setIsLoading(false);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    return (
        <div className="register-page">
            <h1>Регистрация</h1>
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
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={formData.email}
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
                <button type="submit" disabled={isLoading}>
                    {isLoading ? 'Регистрация...' : 'Зарегистрироваться'}
                </button>
            </form>
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            <p>
                Уже есть аккаунт? <Link to="/login">Войдите</Link>
            </p>
        </div>
    );
};

export default RegisterPage;
