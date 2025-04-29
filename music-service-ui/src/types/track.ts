// types/track.ts
import {Album} from "./album";

export interface Track {
    id: number;
    title: string;
    duration: number;
    album: Album;
    usernames: string;
    albumCoverId?: string; // Ссылка на обложку альбома
    mediaFileId: string;
    genre: string
    albumId: number;
    userId: number;
}

export interface CreateTrack {
    title: string;
    duration: number;
    albumId: number;
    userId: number;
    mediaFileId: string;
    genre: string;
}