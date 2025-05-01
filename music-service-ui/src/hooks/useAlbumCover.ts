import { useState, useCallback, useEffect } from "react";
import { mediaApi } from "../api/api";
import defaultAvatar from "../cover_image.jpg";

const useAlbumCover = (coverImageId?: string) => {
    const [albumCoverUrl, setAlbumCoverUrl] = useState<string>(defaultAvatar);

    const fetchCover = useCallback(async () => {
        if (!coverImageId) {
            setAlbumCoverUrl(defaultAvatar);
            return;
        }

        try {
            const response = await mediaApi.downloadMedia(coverImageId);
            const contentType = response.headers["content-type"] || "image/jpeg";
            const blob = new Blob([response.data], { type: contentType });
            const url = URL.createObjectURL(blob);
            setAlbumCoverUrl(url);
        } catch (error) {
            console.error("Ошибка загрузки обложки:", error);
            setAlbumCoverUrl(defaultAvatar);
        }
    }, [coverImageId]);

    useEffect(() => {
        fetchCover();
    }, [fetchCover]);

    return albumCoverUrl;
};

export default useAlbumCover;
