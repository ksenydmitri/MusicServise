// src/api/api.ts
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export const userApi = {
    getCurrentUser: (token: string) =>
        api.get('/auth/me', { headers: { Authorization: `Bearer ${token}` } }),

    getUsers: () =>
        api.get('/users'),

    getUser: (id: number) =>
        api.get(`/users/${id}`),

    searchUser: (query: string) =>
        api.get('/users/search', { params: { query } }),

    createUser: (data: { username: string; email: string; password: string; role?: string }) =>
        api.post('/users', data),

    updateUser: (id: number, data: Partial<{ username: string; email: string; password: string; role: string }>, token: string) =>
        api.patch(`/users/${id}`, data, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        }).catch(error => {
            console.error('Ошибка при обновлении пользователя:', error.response?.data || error.message);
            throw error;
        }),
    deleteUser: (id: number) =>
        api.delete(`/users/${id}`)
};

export const albumApi = {
    getAlbums: (params: any) => api.get('/albums', { params }),
    getAlbumCover: (albumId: number) => api.get(`/albums/${albumId}`),
    getAlbum: (id: number) => api.get(`/albums/${id}`),
    createAlbum: (formData: FormData) => {
        return api.post('/albums', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        });
    },
    updateAlbum: (id: number, formData: FormData) => {
        return api.patch(`/albums/${id}`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        });
    },
    deleteAlbum: (id: number) => api.delete(`/albums/${id}`),
};

export const trackApi = {
    getTracks: (params: any) => api.get('/tracks', { params }),
    getTrack: (id: number) => api.get(`/tracks/${id}`),
    createTrack: (formData: FormData) => api.post('/tracks', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    }),
    createTracksBulk: (data: any[]) => api.post('/tracks/bulk', data),
    updateTrack: (id: number, data: any) => api.patch(`/tracks/${id}`, data),
    deleteTrack: (id: number) => api.delete(`/tracks/${id}`),
    downloadTrack: (mediaFileId: string) => api.get(`/media/download/${mediaFileId}`, { responseType: 'blob' }),
};

export const mediaApi = {
    downloadMedia: (mediaFileId: string) => api.get(`/media/download/${mediaFileId}`, { responseType: 'blob' }),
    uploadMedia: (formData: FormData) => {
        return api.post('/media/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            }
        });
    },
    streamMedia: (mediaFileId: string) => api.get(`/media/stream/${mediaFileId}`, { responseType: 'blob' }),
};

export const playlistApi = {
    getPlaylists: (params: any) => api.get('/playlists', { params }),
    getPlaylist: (id: number) => api.get(`/playlists/${id}`),
    createPlaylist: (data: any) => api.post('/playlists', data),
    updatePlaylist: (id: number, data: any) => api.patch(`/playlists/${id}`, data),
    deletePlaylist: (id: number) => api.delete(`/playlists/${id}`),
};

export const authApi = {
    login: (username: string, password: string) =>
        api.post('/auth/login', { username, password }),

    register: (data: { username: string; email: string; password: string; role?: string }) =>
        api.post('/auth/register', data),

    getCurrentUser: (token: string) =>
        api.get('/auth/me', { headers: { Authorization: `Bearer ${token}` } })
};
