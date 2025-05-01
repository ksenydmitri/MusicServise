import React, { useState, useEffect } from "react";
import { albumApi, userApi } from "../api/api";
import { useAuth } from "../context/AuthContext";
import { Chip, Autocomplete, TextField, Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";
import "./styles/dialog.css";

interface UserOption {
    id: number;
    name: string;
}

interface Props {
    open: boolean;
    handleClose: () => void;
}

const CreateAlbumModal: React.FC<Props> = ({ open, handleClose }) => {
    const { user } = useAuth();
    const [formData, setFormData] = useState({
        name: "",
        coverImage: null as File | null,
        collaborators: [] as string[],
    });

    const [userOptions, setUserOptions] = useState<UserOption[]>([]);
    const [selectedUsers, setSelectedUsers] = useState<UserOption[]>([]);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [previewImage, setPreviewImage] = useState<string | null>(null);

    useEffect(() => {
        const loadUsers = async () => {
            try {
                const response = await userApi.getUsers();
                setUserOptions(response.data.map((user: { id: number; username: string }) => ({
                    id: user.id,
                    name: user.username,
                })));
            } catch (error) {
                console.error("Ошибка загрузки пользователей:", error);
            }
        };
        loadUsers();
    }, []);

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
            collaborators: newValue.map((u) => u.name),
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setErrorMessage(null);

        try {
            const formDataToSend = new FormData();
            const requestData = {
                name: formData.name,
                userId: user?.id,
                collaborators: formData.collaborators,
            };

            formDataToSend.append("request", new Blob([JSON.stringify(requestData)], { type: "application/json" }));
            if (formData.coverImage) {
                formDataToSend.append("file", formData.coverImage);
            }

            await albumApi.createAlbum(formDataToSend);
            handleClose(); // Закрываем окно после создания альбома
        } catch (error) {
            setErrorMessage("Ошибка при создании альбома");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth className="dialog-container">
            <DialogTitle>Создать новый альбом</DialogTitle>
            <DialogContent>
                <TextField fullWidth label="Название альбома" name="name" onChange={handleChange} required />
                <Button variant="contained" component="label">
                    Загрузить обложку
                    <input type="file" hidden accept="image/*" onChange={handleImageChange} />
                </Button>
                {previewImage && <img src={previewImage} alt="Предпросмотр" className="preview-image-container"/>}

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
                <Button onClick={handleSubmit} color="primary" variant="contained" disabled={loading}>
                    {loading ? "Создание..." : "Создать"}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default CreateAlbumModal;
