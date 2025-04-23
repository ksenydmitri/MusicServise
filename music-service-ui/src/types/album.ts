// types/album.ts
import {Track} from "./track";

export interface Album {
    id: number;
    title: string;
    artist: string;
    coverImageId?: string;
    tracks: Track[];
}