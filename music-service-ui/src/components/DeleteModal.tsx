import React from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from "@mui/material";

interface DeleteConfirmationModalProps {
    open: boolean;
    onClose: () => void;
    onConfirm: () => void;
}

const DeleteConfirmationModal: React.FC<DeleteConfirmationModalProps> = ({ open, onClose, onConfirm}) => {
    return (
        <Dialog open={open} onClose={onClose} className= "dialog-container">
            <DialogTitle>Подтвердите удаление</DialogTitle>
            <DialogContent>
                <Typography variant="body1">
                    Вы уверены? Это действие нельзя отменить.
                </Typography>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} color="secondary">
                    Отмена
                </Button>
                <Button onClick={onConfirm} color="error" variant="contained">
                    Удалить
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default DeleteConfirmationModal;
