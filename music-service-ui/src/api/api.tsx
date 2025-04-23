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

export const authApi = {
    login: (username: string, password: string) =>
        api.post('/auth/login', { username, password }),
    register: (username: string, email: string, password: string) =>
        api.post('/auth/register', { username, email, password }),
};

export const albumApi = {
    getAlbums: (params: any) => api.get('/albums', { params }),
    getAlbumCover: (albumId: number) => api.get(`/albums/${albumId}`),
    getAlbum: (id: number) => api.get(`/albums/${id}`),
    createAlbum: (formData: FormData) => {
        return axios.post('/albums', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
    },
    updateAlbum: (id: number, data: any) => api.patch(`/albums/${id}`, data),
    deleteAlbum: (id: number) => api.delete(`/albums/${id}`),
};

export const trackApi = {
    getTracks: (params: any) => api.get('/tracks', { params }),
    getTrack: (id: number) => api.get(`/tracks/${id}`),
    createTrack: (data: any) => api.post('/tracks', data),
    createTracksBulk: (data: any[]) => api.post('/tracks/bulk', data),
    updateTrack: (id: number, data: any) => api.patch(`/tracks/${id}`, data),
    deleteTrack: (id: number) => api.delete(`/tracks/${id}`),
    downloadTrack: (mediaFileId: string) => api.get(`/media/download/${mediaFileId}`, { responseType: 'blob' }),
};

export const mediaApi = {
    downloadMedia: (mediaFileId: string) => api.get(`/media/download/${mediaFileId}`, { responseType: 'blob' }),
};

export const playlistApi = {
    getPlaylists: (params: any) => api.get('/playlists', { params }),
    getPlaylist: (id: number) => api.get(`/playlists/${id}`),
    createPlaylist: (data: any) => api.post('/playlists', data),
    updatePlaylist: (id: number, data: any) => api.patch(`/playlists/${id}`, data),
    deletePlaylist: (id: number) => api.delete(`/playlists/${id}`),
};

export const userApi = {
    getCurrentUser: () => api.get('/auth/me'),
    getUsers: () => api.get('/users'),
    getUser: (id: number) => api.get(`/users/${id}`),
    searchUser: (query: string) => api.get('/users/search', { params: { query } }),
    createUser: (data: any) => api.post('/users', data),
    updateUser: (id: number, data: any) => api.patch(`/users/${id}`, data),
    deleteUser: (id: number) => api.delete(`/users/${id}`),
};