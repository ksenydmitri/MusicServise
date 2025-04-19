import { Track } from '../types/track';
import './styles/trackList.css';

interface TrackListProps {
    tracks: Track[];
}

const TrackList = ({ tracks }: TrackListProps) => {
    return (
        <div className="track-list">
            <h3>Треки</h3>
            <ul>
                {tracks.map((track: Track, index: number) => (
                    <li key={track.id}>
                        <span className="track-number">{index + 1}</span>
                        <span className="track-title">{track.title}</span>
                        <span className="track-duration">{track.duration}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default TrackList;