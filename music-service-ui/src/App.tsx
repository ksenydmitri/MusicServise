import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AlbumPage from './pages/AlbumPage';
import CreateAlbumPage from './pages/CreateAlbumPage';
import UserInfoPage from './pages/UserInfoPage';
import AlbumsPage from "./pages/AlbumsPage";

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Navbar />
                <div className="container">
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/album/:id" element={<AlbumPage />} />
                        <Route path="/create-album" element={<CreateAlbumPage />} />
                        <Route path="/user-info" element={<UserInfoPage />} />
                        <Route path="/albums" element={<AlbumsPage />} />
                    </Routes>
                </div>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
