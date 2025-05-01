import {Track} from "./track";

export interface Album {
    id: number;
    title: string;
    artists: string[];
    coverImageId?: string;
    tracks: Track[];
    userIds: number[];
}
