// types/album.ts
import {Track} from "./track";
import { User } from "./user"; // Добавляем тип User

export interface Album {
    id: number;
    title: string;
    artists: string[];
    coverImageId?: string;
    tracks: Track[];
    userIds: number[];
}
