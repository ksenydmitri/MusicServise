// types/album.ts
import {Track} from "./track";

export interface Album {
    id: number;
    title: string;
    artists: string[];
    coverImageId?: string;
    tracks: Track[];
}

interface CreateAlbumRequest {
    name: string;  // Изменил title на name для соответствия DTO
    userId: number;
    collaborators: string[];  // Изменил на string[] для username
}