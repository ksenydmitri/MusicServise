// types/track.ts
export interface Track {
    id: number;
    title: string;
    duration: string;
    albumId?: number;
    usernames: string;
}