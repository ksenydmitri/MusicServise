import React, {useEffect, useState} from 'react';
import {
    Box,
    Button,
    TextField,
    Typography,
    Stepper,
    Step,
    StepLabel,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    IconButton
} from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { trackApi } from '../api/api';
import { useSnackbar } from 'notistack';
import {CreateTrack, Track} from "../types/track";
import { useAuth } from '../context/AuthContext';

interface AddTracksFormProps {
    albumId?: number;
    onAddTrack?: () => void;
}

const AddTracksForm: React.FC<AddTracksFormProps> = ({ albumId }) => {
    const { user } = useAuth();
    const [activeStep, setActiveStep] = useState(0);
    const [tracks, setTracks] = useState<Array<Partial<CreateTrack>>>([]);
    const [currentTrack, setCurrentTrack] = useState<Partial<CreateTrack>>({
        title: '',
        duration: 0,
        genre: '',
        userId: user?.id,
        albumId: albumId,
    });
    const { enqueueSnackbar } = useSnackbar();

    useEffect(() => {
        if (albumId !== undefined && user?.id) {
            setCurrentTrack(prev => ({
                ...prev,
                albumId,
                userId: user.id
            }));
        }
    }, [albumId, user]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setCurrentTrack(prev => ({
            ...prev,
            [name]: name === 'duration'
                ? Number(value)
                : value
        }));
    };

    const addTrack = () => {
        if (!currentTrack.title || !currentTrack.genre || !currentTrack.albumId || !currentTrack.userId) {
            enqueueSnackbar('Заполните обязательные поля', { variant: 'warning' });
            return;
        }
        setTracks([...tracks, currentTrack]);
        setCurrentTrack(prev => ({
            ...prev,
            title: '',
            duration: 0,
            genre: ''
        }));
    };

    const removeTrack = (index: number) => {
        setTracks(tracks.filter((_, i) => i !== index));
    };

    const submitTracks = async () => {
        try {
            if (tracks.length === 0) {
                enqueueSnackbar('Добавьте хотя бы один трек', { variant: 'warning' });
                return;
            }

            if (tracks.length === 1) {
                await trackApi.createTrack(tracks[0]);
            } else {
                await trackApi.createTracksBulk(tracks);
            }

            enqueueSnackbar('Треки успешно добавлены', { variant: 'success' });
            setTracks([]);
            setActiveStep(0);
        } catch (error) {
            enqueueSnackbar('Ошибка при добавлении треков', { variant: 'error' });
        }
    };

    const steps = ['Добавление треков', 'Просмотр и подтверждение'];

    return (
        <Box sx={{ maxWidth: 800, mx: 'auto', p: 3 }}>
            <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
                {steps.map((label) => (
                    <Step key={label}>
                        <StepLabel>{label}</StepLabel>
                    </Step>
                ))}
            </Stepper>

            {activeStep === 0 && (
                <Paper sx={{ p: 3, mb: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        Добавить новый трек
                    </Typography>
                    <Box component="form" sx={{ display: 'grid', gap: 2 }}>
                        <TextField
                            name="title"
                            label="Название трека"
                            value={currentTrack.title}
                            onChange={handleInputChange}
                            fullWidth
                            required
                        />
                        <TextField
                            name="duration"
                            label="Длительность (сек)"
                            type="number"
                            value={currentTrack.duration || ''}
                            onChange={handleInputChange}
                            fullWidth
                            required
                        />
                        <TextField
                            name="genre"
                            label="Жанр"
                            value={currentTrack.genre}
                            onChange={handleInputChange}
                            fullWidth
                            required
                        />
                        <Button
                            variant="contained"
                            onClick={addTrack}
                            sx={{ mt: 2 }}
                        >
                            Добавить трек
                        </Button>
                    </Box>
                </Paper>
            )}

            {activeStep === 1 && (
                <Paper sx={{ p: 3, mb: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        Список треков для добавления
                    </Typography>
                    <TableContainer>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Название</TableCell>
                                    <TableCell>Длительность</TableCell>
                                    <TableCell>Жанр</TableCell>
                                    <TableCell>Действия</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {tracks.map((track, index) => (
                                    <TableRow key={index}>
                                        <TableCell>{track.title}</TableCell>
                                        <TableCell>{track.duration} сек</TableCell>
                                        <TableCell>{track.genre}</TableCell>
                                        <TableCell>
                                            <IconButton onClick={() => removeTrack(index)}>
                                                <DeleteIcon color="error" />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Paper>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Button
                    disabled={activeStep === 0}
                    onClick={() => setActiveStep(activeStep - 1)}
                >
                    Назад
                </Button>
                {activeStep === steps.length - 1 ? (
                    <Button variant="contained" onClick={submitTracks}>
                        Сохранить треки
                    </Button>
                ) : (
                    <Button
                        variant="contained"
                        onClick={() => setActiveStep(activeStep + 1)}
                        disabled={tracks.length === 0}
                    >
                        Далее
                    </Button>
                )}
            </Box>
        </Box>
    );
};

export default AddTracksForm;