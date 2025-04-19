import './styles/trackCard.css';
import {Track} from "../types/track";

interface TrackCardProps {
    track: Track;
}

const TrackCard = ({ track }: TrackCardProps) => {
    return (
        <div className="track-card">
            <h3>{track.title}</h3>
            <p>Duration: {track.duration}</p>
            <p>Artist: {track.artist}</p>
        </div>
    );
};

export default TrackCard;
