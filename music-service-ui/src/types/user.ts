export interface User {
    id: number;
    username: string;
    email: string;
    password?: string;
    albumIds: number[];
}