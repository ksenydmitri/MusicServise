// types/track.ts
import {Album} from "./album";

export interface Track {
    id: number;
    title: string;
    duration: string;
    album: Album;
    usernames: string;
    albumCoverId?: string; // Ссылка на обложку альбома
    mediaFileId: string
}