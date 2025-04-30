import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/api';
import { useAuth } from '../context/AuthContext';
import { AuthRequest } from '../types/auth';
import './styles/login.css';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';

const LoginPage = () => {
    const [formData, setFormData] = useState<AuthRequest>({
        username: '',
        password: '',
    });
    const { login } = useAuth();
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMessage(null);
        setIsLoading(true);

        try {
            const response = await authApi.login(
                formData.username,
                formData.password
            );

            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
                login(response.data.user, response.data.token);
                navigate('/');
            } else {
                setErrorMessage('Ошибка: Токен не получен от сервера.');
            }
        } catch (error) {
            setErrorMessage('Неверное имя пользователя или пароль');
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
                <Typography variant="h5" component="h1">
                    Вход
                </Typography>

                {errorMessage && (
                    <Typography>
                        {errorMessage}
                    </Typography>
                )}

                <form onSubmit={handleSubmit}>
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
                        type="password"
                        label="Пароль"
                        name="password"
                        variant="outlined"
                        value={formData.password}
                        onChange={handleChange}
                        required
                    />

                    <Button
                        fullWidth
                        variant="contained"
                        type="submit"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Вход...' : 'Войти'}
                    </Button>
                </form>

                <Typography variant="body2" style={{ marginTop: '2rem' }}>
                    Нет аккаунта?{' '}
                    <Link to="/register">
                        Зарегистрируйтесь
                    </Link>
                </Typography>
            </Paper>
        </div>
    );
};

export default LoginPage;