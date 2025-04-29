import React, { useState, useEffect } from 'react';
import { albumApi, userApi } from '../api/api';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './styles/createalbum.css';
import { Chip, Autocomplete, TextField } from '@mui/material';

interface UserOption {
    id: number;
    name: string;
}

const CreateAlbumPage = () => {
    const { user, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: '',
        coverImage: null as File | null,
        collaborators: [] as string[]
    });


    const [userOptions, setUserOptions] = useState<UserOption[]>([]);
    const [selectedUsers, setSelectedUsers] = useState<UserOption[]>([]);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [previewImage, setPreviewImage] = useState<string | null>(null);


    useEffect(() => {
        const loadUsers = async () => {
            try {
                const response = await userApi.getUsers();
                setUserOptions(response.data.map((user: { id: number; username: string }) => ({
                    id: user.id,
                    name: user.username
                })));

            } catch (error) {
                console.error('Ошибка загрузки пользователей:', error);
            }
        };
        loadUsers();
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setFormData(prev => ({ ...prev, coverImage: file }));

            const reader = new FileReader();
            reader.onloadend = () => {
                setPreviewImage(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleUserSelect = (event: React.SyntheticEvent, newValue: UserOption[]) => {
        setSelectedUsers(newValue);
        setFormData(prev => ({
            ...prev,
            collaborators: newValue.map(u => u.name)  // Используем name (username)
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!isAuthenticated || !user) {
            setErrorMessage('Требуется авторизация');
            return;
        }

        setLoading(true);
        setSuccessMessage(null);
        setErrorMessage(null);

        try {
            const formDataToSend = new FormData();

            const requestData = {
                name: formData.name,
                userId: user.id,
                collaborators: formData.collaborators
            };
            console.log('Отправляемые данные:', requestData);

            formDataToSend.append(
                'request',
                new Blob([JSON.stringify(requestData)], {
                    type: 'application/json'
                })
            );

            if (formData.coverImage) {
                formDataToSend.append('file', formData.coverImage);
            }

            const response = await albumApi.createAlbum(formDataToSend);

            setSuccessMessage('Альбом успешно создан!');
            setFormData({ name: '', coverImage: null, collaborators: [] });
            setSelectedUsers([]);
            setPreviewImage(null);
            navigate(`/album/${response.data.id}`);
        } catch (error: any) {
            setErrorMessage(error?.response?.data?.message || 'Ошибка при создании альбома');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="create-album-page">
            <h1>Создать новый альбом</h1>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Название альбома:</label>
                    <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label>Обложка альбома:</label>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageChange}
                    />
                    {previewImage && (
                        <div className="image-preview">
                            <img src={previewImage} alt="Предпросмотр обложки" />
                        </div>
                    )}
                </div>

                <div className="form-group">
                    <label>Соавторы (могут редактировать альбом):</label>
                    <Autocomplete
                        multiple
                        options={userOptions}
                        getOptionLabel={(option) => option.name}
                        value={selectedUsers}
                        onChange={handleUserSelect}
                        filterSelectedOptions
                        renderInput={(params) => (
                            <TextField
                                {...params}
                                placeholder="Добавьте пользователей"
                            />
                        )}
                        renderTags={(value, getTagProps) =>
                            value.map((option, index) => (
                                <Chip
                                    label={option.name}
                                    {...getTagProps({ index })}
                                />
                            ))
                        }
                    />
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? 'Создание...' : 'Создать альбом'}
                </button>
            </form>

            {successMessage && <div className="alert success">{successMessage}</div>}
            {errorMessage && <div className="alert error">{errorMessage}</div>}
        </div>
    );
};

export default CreateAlbumPage;