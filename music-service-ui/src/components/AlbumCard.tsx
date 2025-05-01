import { Link } from "react-router-dom";
import { Album } from "../types/album";
import "./styles/albumCard.css";
import useAlbumCover from "../hooks/useAlbumCover";
import { Card, CardMedia, CardContent, Typography, IconButton } from "@mui/material";
import { Add } from "@mui/icons-material";

interface AlbumCardProps {
    album: Album;
    onAddTrack?: () => void;
}

const AlbumCard = ({ album, onAddTrack }: AlbumCardProps) => {
    const albumCoverUrl = useAlbumCover(album.coverImageId); // Используем хук

    return (
        <div className="albums-container">
            <Card component={Link} to={`/album/${album.id}`}>
                <CardMedia component="img" image={albumCoverUrl} alt={album.title} />
                <CardContent>
                    <Typography variant="h6">{album.title}</Typography>
                    <Typography variant="body2">{album.artists.join(", ")}</Typography>
                </CardContent>
                {onAddTrack && (
                    <IconButton
                        aria-label="add track"
                        onClick={(e) => {
                            e.preventDefault();
                            e.stopPropagation();
                            onAddTrack();
                        }}
                    >
                        <Add />
                    </IconButton>
                )}
            </Card>
        </div>
    );
};

export default AlbumCard;
