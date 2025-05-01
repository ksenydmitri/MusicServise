import React, { useState, useEffect } from "react";
import { albumApi, userApi } from "../api/api";
import { Chip, Autocomplete, TextField, Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";
import "./styles/dialog.css";
import { Album } from "../types/album";

interface UserOption {
    id: number;
    name: string;
}

interface Props {
    album: Album;
    open: boolean;
    handleClose: () => void;
    onAlbumUpdated: (updatedAlbum: Album) => void;
}

const EditAlbumModal: React.FC<Props> = ({ album, open, handleClose, onAlbumUpdated }) => {
    const [formData, setFormData] = useState({
        title: album.title,
        artists: album.artists.join(", "),
        coverImage: null as File | null,
        userIds: album.userIds,
    });

    const [userOptions, setUserOptions] = useState<UserOption[]>([]);
    const [selectedUsers, setSelectedUsers] = useState<UserOption[]>([]);
    const [loading, setLoading] = useState(false);
    const [previewImage, setPreviewImage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    useEffect(() => {
        const loadUsers = async () => {
            try {
                const response = await userApi.getUsers();
                const options = response.data.map((user: { id: number; username: string }) => ({
                    id: user.id,
                    name: user.username,
                }));
                setUserOptions(options);

                const selected = options.filter((option: UserOption) =>
                    album.userIds.includes(option.id)
                );
                setSelectedUsers(selected);
            } catch (error) {
                console.error("Ошибка загрузки пользователей:", error);
            }
        };
        loadUsers();
    }, [album]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    };

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setFormData((prev) => ({ ...prev, coverImage: file }));

            const reader = new FileReader();
            reader.onloadend = () => setPreviewImage(reader.result as string);
            reader.readAsDataURL(file);
        }
    };

    const handleUserSelect = (event: React.SyntheticEvent, newValue: UserOption[]) => {
        setSelectedUsers(newValue);
        setFormData((prev) => ({
            ...prev,
            userIds: newValue.map((u) => u.id),
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setErrorMessage(null);

        try {
            const formDataToSend = new FormData();
            const requestData = {
                name: formData.title,
                artists: formData.artists.split(",").map(artist => artist.trim()),
                userIds: formData.userIds,
            };

            formDataToSend.append("request", new Blob([JSON.stringify(requestData)], { type: "application/json" }));
            if (formData.coverImage) {
                formDataToSend.append("file", formData.coverImage);
            }

            const response = await albumApi.updateAlbum(album.id, formDataToSend);
            onAlbumUpdated(response.data);
            handleClose();
        } catch (error) {
            setErrorMessage("Ошибка при обновлении альбома");
            console.error("Ошибка при обновлении альбома:", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth className="dialog-container">
            <DialogTitle>Редактировать альбом</DialogTitle>
            <DialogContent>
                {errorMessage && <div className="error-message">{errorMessage}</div>}

                <TextField
                    fullWidth
                    label="Название альбома"
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    required
                    margin="normal"
                    sx={{ mt: 2 }}
                />

                <TextField
                    fullWidth
                    label="Исполнители (через запятую)"
                    name="artists"
                    value={formData.artists}
                    onChange={handleChange}
                    required
                    margin="normal"
                />

                <Button variant="contained" component="label" sx={{ mt: 2 }}>
                    Изменить обложку
                    <input type="file" hidden accept="image/*" onChange={handleImageChange} />
                </Button>

                {previewImage && (
                    <img
                        src={previewImage}
                        alt="Предпросмотр обложки"
                        className="preview-image-container"
                        style={{ marginTop: 16, maxHeight: 200 }}
                    />
                )}

                <Autocomplete
                    multiple
                    options={userOptions}
                    getOptionLabel={(option: UserOption) => option.name}
                    value={selectedUsers}
                    onChange={handleUserSelect}
                    filterSelectedOptions
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            placeholder="Добавьте пользователей"
                            margin="normal"
                            sx={{ mt: 2 }}
                        />
                    )}
                    renderTags={(value: UserOption[], getTagProps) =>
                        value.map((option: UserOption, index: number) => (
                            <Chip label={option.name} {...getTagProps({ index })} key={option.id} />
                        ))
                    }
                />
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose}>Отмена</Button>
                <Button
                    onClick={handleSubmit}
                    color="primary"
                    variant="contained"
                    disabled={loading}
                >
                    {loading ? "Сохранение..." : "Сохранить"}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default EditAlbumModal;