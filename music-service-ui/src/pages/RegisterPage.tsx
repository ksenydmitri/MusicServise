import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './styles/register.css';
import { authApi } from '../api/api';
import { RegisterRequest } from "../types/auth";
import { useAuth } from "../context/AuthContext";
import './styles/login.css'
import Typography from "@mui/material/Typography";
import Paper from "@mui/material/Paper";
import TextField from "@mui/material/TextField";

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

        if (!formData.username || !formData.password || !formData.email) {
            setErrorMessage("Все поля обязательны для заполнения!");
            return;
        }

        setIsLoading(true); // Включение состояния загрузки
        setErrorMessage(null); // Очистка предыдущих сообщений об ошибке

        try {
            const response = await authApi.register({
                username: formData.username,
                email: formData.email,
                password: formData.password
            });

            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
                login(response.data.user, response.data.token);
                navigate('/');
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
        <div className="loginContainer">
            <Paper className="loginPaper" elevation={3}>
                {errorMessage && (
                    <Typography >
                        {errorMessage}
                    </Typography>
                )}
                <h1>Регистрация</h1>
                <form onSubmit={handleSubmit}  >
                    <TextField
                        fullWidth
                        label="Имя пользователя"
                        name="username"
                        variant="outlined"
                        value={formData.username}
                        onChange={handleChange}
                        required
                    />
                    <TextField
                        fullWidth
                        label="Почта"
                        name="email"
                        variant="outlined"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />
                    <TextField
                        fullWidth
                        label="Пароль"
                        name="password"
                        variant="outlined"
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
            </Paper>
        </div>
    );
};

export default RegisterPage;
