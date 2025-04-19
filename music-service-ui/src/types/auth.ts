// types/auth.ts
export interface AuthResponse {
    token: string;
    username: string;
}

export interface AuthRequest {
    username: string;
    password: string;
}

export interface RegisterRequest extends AuthRequest {
    email: string;
}