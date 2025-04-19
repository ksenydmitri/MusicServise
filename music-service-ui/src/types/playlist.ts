export interface Playlist {
    id: number;
    name: string;
    tracks: number[];
    createdAt: string;
}

export interface Track {
    id: number;
    title: string;
    duration: string; // in HH:MM:SS format
    albumId: number;
}

export type PlaylistWithTracks = Playlist & { tracksDetails: Track[] };
