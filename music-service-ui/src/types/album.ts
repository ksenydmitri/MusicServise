// types/album.ts
import {Track} from "./track";

export interface Album {
    id: number;
    title: string;
    artist: string;
    year: number;
    genre: string;
    coverImage: string;
    tracks: Track[];
}