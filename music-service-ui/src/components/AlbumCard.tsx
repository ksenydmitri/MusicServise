import { Link } from 'react-router-dom';
import { Album } from '../types/album';
import './styles/albumCard.css';

interface AlbumCardProps {
    album: Album;
}

const AlbumCard = ({ album }: AlbumCardProps) => {
    return (
        <div className="album-card">
            <Link to={`/album/${album.id}`}>
                <img src={album.coverImageId} alt={album.title} />
                <h3>{album.title}</h3>
                <p>{album.artist}</p>
            </Link>
        </div>
    );
};

export default AlbumCard;