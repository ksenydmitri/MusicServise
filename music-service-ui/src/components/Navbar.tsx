import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './styles/navbar.css'; // Подключение стилей

const Navbar = () => {
    const { isAuthenticated, logout } = useAuth();

    return (
        <nav className="navbar">
            <Link to="/">Главная</Link>
            {isAuthenticated ? (
                <>
                    <span>Добро пожаловать!</span>
                    <Link to="/create-album">Создать альбом</Link>
                    <Link to="/user-info">Информация о пользователе</Link>
                    <button onClick={logout}>Выйти</button>
                </>
            ) : (
                <>
                    <Link to="/login">Вход</Link>
                    <Link to="/register">Регистрация</Link>
                </>
            )}
        </nav>
    );
};

export default Navbar;
