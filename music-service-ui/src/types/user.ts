// types/user.ts
export interface User {
    id: number;
    username: string;
    email: string;
    password?: string; // Обычно не включаем пароль в ответы
}