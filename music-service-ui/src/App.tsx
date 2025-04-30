import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AlbumPage from './pages/AlbumPage';
import UserInfoPage from './pages/UserInfoPage';
import AlbumsPage from "./pages/AlbumsPage";
import ErrorBoundary from "./components/ErrorBoundary";

function App() {
    return (
        <BrowserRouter>
        <AuthProvider>
            <ErrorBoundary>
                <Navbar />
                <div className="container">
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/album/:id" element={<AlbumPage />} />
                        <Route path="/user-info" element={<UserInfoPage />} />
                        <Route path="/albums" element={<AlbumsPage />} />
                    </Routes>
                </div>

            </ErrorBoundary>
        </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
