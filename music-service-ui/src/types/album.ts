// types/album.ts
import {Track} from "./track";

export interface Album {
    id: number;
    title: string;
    artists: [];
    coverImageId?: string;
    tracks: Track[];
}