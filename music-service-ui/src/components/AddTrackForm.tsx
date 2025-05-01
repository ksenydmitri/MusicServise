import React, { useState, useCallback } from 'react';
import {
    Box,
    Button,
    TextField,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    CircularProgress,
    Alert,
    Input,
    FormControl,
    FormHelperText
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { trackApi } from '../api/api';
import './styles/dialog.css'

interface AddTrackFormProps {
    albumId: number;
    onClose: () => void;
    onSuccess: () => void;
}

interface TrackData {
    title: string;
    duration: number;
    genre: string;
    albumId: number;
    userId: number;
}

const AddTrackForm: React.FC<AddTrackFormProps> = ({ albumId, onClose, onSuccess }) => {
    const { user } = useAuth();
    const [title, setTitle] = useState('');
    const [duration, setDuration] = useState(0);
    const [genre, setGenre] = useState('');
    const [mediaFile, setMediaFile] = useState<File | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [fileError, setFileError] = useState<string | null>(null);

    const validateFile = useCallback((file: File) => {
        const validTypes = ['audio/mpeg', 'audio/wav', 'audio/ogg'];
        const maxSize = 20 * 1024 * 1024; // 20MB

        if (!validTypes.includes(file.type)) {
            setFileError('Поддерживаются только файлы MP3, WAV или OGG');
            return false;
        }

        if (file.size > maxSize) {
            setFileError('Файл слишком большой (макс. 20MB)');
            return false;
        }

        setFileError(null);
        return true;
    }, []);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            if (validateFile(file)) {
                setMediaFile(file);
            }
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!title || !duration || !genre) {
            setError('Все поля обязательны для заполнения');
            return;
        }

        if (!user) {
            setError('Требуется авторизация');
            return;
        }

        if (!mediaFile) {
            setFileError('Необходимо выбрать аудиофайл');
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const trackData: TrackData = {
                title,
                duration,
                genre,
                albumId,
                userId: user.id
            };

            const formData = new FormData();
            formData.append('request', new Blob([JSON.stringify(trackData)], {
                type: 'application/json'
            }));
            formData.append('mediaFile', mediaFile);

            // Используем trackApi вместо прямого fetch
            await trackApi.createTrack(formData);

            onSuccess();
            onClose();
        } catch (err) {
            console.error('Ошибка при добавлении трека:', err);
            setError(err instanceof Error ? err.message : 'Ошибка при добавлении трека');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="dialog-container">
            <Dialog open={true} onClose={onClose} maxWidth="sm" fullWidth className="dialog-container">
                <DialogTitle className="dialog-title">Добавить новый трек</DialogTitle>
                <Box component="form" onSubmit={handleSubmit} className="dialog-form">
                    <DialogContent className="dialog-content">
                        {error && (
                            <Alert severity="error" className="dialog-error">
                                {error}
                            </Alert>
                        )}

                        <TextField
                            autoFocus
                            margin="dense"
                            label="Название трека"
                            fullWidth
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            required
                            disabled={isLoading}
                            className="dialog-field"
                        />

                        <TextField
                            margin="dense"
                            label="Длительность (секунды)"
                            type="number"
                            fullWidth
                            value={duration || ''}
                            onChange={(e) => setDuration(Number(e.target.value))}
                            required
                            disabled={isLoading}
                            inputProps={{ min: 1 }}
                            className="dialog-field"
                        />

                        <TextField
                            margin="dense"
                            label="Жанр"
                            fullWidth
                            value={genre}
                            onChange={(e) => setGenre(e.target.value)}
                            required
                            disabled={isLoading}
                            className="dialog-field"
                        />

                        <FormControl fullWidth error={!!fileError} className="dialog-file-upload">
                            <Input
                                type="file"
                                inputProps={{
                                    accept: 'audio/mpeg, audio/wav, audio/ogg'
                                }}
                                onChange={handleFileChange}
                                disabled={isLoading}
                                className="dialog-file-input"
                            />
                            {fileError && (
                                <FormHelperText className="dialog-error-text">{fileError}</FormHelperText>
                            )}
                            {mediaFile && !fileError && (
                                <FormHelperText className="dialog-file-info">
                                    Выбран файл: {mediaFile.name} ({(mediaFile.size / 1024 / 1024).toFixed(2)} MB)
                                </FormHelperText>
                            )}
                        </FormControl>
                    </DialogContent>
                    <DialogActions className="dialog-actions">
                        <Button
                            onClick={onClose}
                            disabled={isLoading}
                            className="dialog-cancel-button"
                        >
                            Отмена
                        </Button>
                        <Button
                            type="submit"
                            variant="contained"
                            disabled={isLoading}
                            className="dialog-submit-button"
                            endIcon={isLoading ? <CircularProgress size={20} className="dialog-spinner" /> : null}
                        >
                            {isLoading ? 'Добавление...' : 'Добавить трек'}
                        </Button>
                    </DialogActions>
                </Box>
            </Dialog>
        </div>
    );
};

            export default AddTrackForm;