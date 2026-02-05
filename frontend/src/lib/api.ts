import type {
  TitleResponse,
  TitleCreateRequest,
  TitleUpdateRequest,
  AuthorResponse,
  AuthorCreateRequest,
  AuthorUpdateRequest,
  PublisherResponse,
  PublisherCreateRequest,
  PublisherUpdateRequest,
  TagResponse,
  TagCreateRequest,
  TagUpdateRequest,
  ReplaceSlugRequest,
  ReplaceTagsRequest,
  TitleAddAuthorRequest,
  ChapterSummaryResponse,
  ChapterDetailsResponse,
  ChapterCreateRequest,
  ChapterUpdateRequest,
  ChapterPagesUpdateRequest,
  ChapterReadRequest,
  ChapterReadStatusResponse,
  NextChapterResponse,
  UserCollectionResponse,
  CollectionCreateRequest,
  CollectionUpdateRequest,
  ShareLinkResponse,
  LibraryEntryResponse,
  LibraryEntryCreateRequest,
  LibraryEntryUpdateRequest,
  TitleAnalyticsResponse,
  MediaUploadUrlRequest,
  MediaUploadUrlResponse,
  Page,
} from "@/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

class ApiError extends Error {
  constructor(
    public status: number,
    message: string
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };

  // Get auth token if available (client-side only)
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("auth_token");
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new ApiError(response.status, errorText || response.statusText);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

// Query string builder
function buildQueryString(
  params: Record<string, string | number | boolean | string[] | undefined>
): string {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      if (Array.isArray(value)) {
        value.forEach((v) => searchParams.append(key, v));
      } else {
        searchParams.append(key, String(value));
      }
    }
  });
  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

// ==================== TITLES API ====================
export const titlesApi = {
  getAll: (params?: {
    search?: string;
    type?: string[];
    titleStatus?: string[];
    country?: string;
    tags?: string[];
    releaseYear?: number;
    yearFrom?: number;
    yearTo?: number;
    contentRating?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<Page<TitleResponse>> =>
    fetchApi(`/api/v1/titles${buildQueryString(params || {})}`),

  getById: (id: string): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/${id}`),

  getBySlug: (slug: string): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/slug/${slug}`),

  create: (data: TitleCreateRequest): Promise<TitleResponse> =>
    fetchApi("/api/v1/titles", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  update: (id: string, data: TitleUpdateRequest): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (id: string): Promise<void> =>
    fetchApi(`/api/v1/titles/${id}`, {
      method: "DELETE",
    }),

  updateSlug: (id: string, data: ReplaceSlugRequest): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/${id}/slug`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  addAuthor: (
    id: string,
    data: TitleAddAuthorRequest
  ): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/${id}/authors`, {
      method: "POST",
      body: JSON.stringify(data),
    }),

  removeAuthor: (id: string, authorId: string): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/${id}/authors/${authorId}`, {
      method: "DELETE",
    }),

  removePublisher: (id: string): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/${id}/publisher`, {
      method: "DELETE",
    }),

  updateTags: (id: string, data: ReplaceTagsRequest): Promise<TitleResponse> =>
    fetchApi(`/api/v1/titles/${id}/tags`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
};

// ==================== AUTHORS API ====================
export const authorsApi = {
  getAll: (params?: {
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<Page<AuthorResponse>> =>
    fetchApi(`/api/v1/authors${buildQueryString(params || {})}`),

  getById: (id: string): Promise<AuthorResponse> =>
    fetchApi(`/api/v1/authors/${id}`),

  getBySlug: (slug: string): Promise<AuthorResponse> =>
    fetchApi(`/api/v1/authors/slug/${slug}`),

  create: (data: AuthorCreateRequest): Promise<AuthorResponse> =>
    fetchApi("/api/v1/authors", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  update: (id: string, data: AuthorUpdateRequest): Promise<AuthorResponse> =>
    fetchApi(`/api/v1/authors/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (id: string): Promise<void> =>
    fetchApi(`/api/v1/authors/${id}`, {
      method: "DELETE",
    }),

  updateSlug: (
    id: string,
    data: ReplaceSlugRequest
  ): Promise<AuthorResponse> =>
    fetchApi(`/api/v1/authors/${id}/slug`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
};

// ==================== PUBLISHERS API ====================
export const publishersApi = {
  getAll: (params?: {
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<Page<PublisherResponse>> =>
    fetchApi(`/api/v1/publishers${buildQueryString(params || {})}`),

  getById: (id: string): Promise<PublisherResponse> =>
    fetchApi(`/api/v1/publishers/${id}`),

  getBySlug: (slug: string): Promise<PublisherResponse> =>
    fetchApi(`/api/v1/publishers/slug/${slug}`),

  create: (data: PublisherCreateRequest): Promise<PublisherResponse> =>
    fetchApi("/api/v1/publishers", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  update: (
    id: string,
    data: PublisherUpdateRequest
  ): Promise<PublisherResponse> =>
    fetchApi(`/api/v1/publishers/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (id: string): Promise<void> =>
    fetchApi(`/api/v1/publishers/${id}`, {
      method: "DELETE",
    }),

  updateSlug: (
    id: string,
    data: ReplaceSlugRequest
  ): Promise<PublisherResponse> =>
    fetchApi(`/api/v1/publishers/${id}/slug`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
};

// ==================== TAGS API ====================
export const tagsApi = {
  getAll: (params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<Page<TagResponse>> =>
    fetchApi(`/api/v1/tags${buildQueryString(params || {})}`),

  getById: (id: string): Promise<TagResponse> =>
    fetchApi(`/api/v1/tags/${id}`),

  getBySlug: (slug: string): Promise<TagResponse> =>
    fetchApi(`/api/v1/tags/slug/${slug}`),

  create: (data: TagCreateRequest): Promise<TagResponse> =>
    fetchApi("/api/v1/tags", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  update: (id: string, data: TagUpdateRequest): Promise<TagResponse> =>
    fetchApi(`/api/v1/tags/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (id: string): Promise<void> =>
    fetchApi(`/api/v1/tags/${id}`, {
      method: "DELETE",
    }),

  updateSlug: (id: string, data: ReplaceSlugRequest): Promise<TagResponse> =>
    fetchApi(`/api/v1/tags/${id}/slug`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
};

// ==================== CHAPTERS API ====================
export const chaptersApi = {
  getAllByTitle: (titleId: string): Promise<ChapterSummaryResponse[]> =>
    fetchApi(`/api/v1/titles/${titleId}/chapters`),

  getById: (chapterId: string): Promise<ChapterDetailsResponse> =>
    fetchApi(`/api/v1/chapters/${chapterId}`),

  create: (
    titleId: string,
    data: ChapterCreateRequest
  ): Promise<ChapterDetailsResponse> =>
    fetchApi(`/api/v1/titles/${titleId}/chapters`, {
      method: "POST",
      body: JSON.stringify(data),
    }),

  update: (
    chapterId: string,
    data: ChapterUpdateRequest
  ): Promise<ChapterDetailsResponse> =>
    fetchApi(`/api/v1/chapters/${chapterId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (chapterId: string): Promise<void> =>
    fetchApi(`/api/v1/chapters/${chapterId}`, {
      method: "DELETE",
    }),

  updatePages: (
    chapterId: string,
    data: ChapterPagesUpdateRequest
  ): Promise<ChapterDetailsResponse> =>
    fetchApi(`/api/v1/chapters/${chapterId}/pages`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
};

// ==================== READING PROGRESS API ====================
export const readingProgressApi = {
  setReadStatus: (
    titleId: string,
    chapterId: string,
    data: ChapterReadRequest
  ): Promise<ChapterReadStatusResponse> =>
    fetchApi(`/api/v1/titles/${titleId}/chapters/${chapterId}/read-status`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  getReadStatus: (
    titleId: string,
    chapterId: string
  ): Promise<ChapterReadStatusResponse> =>
    fetchApi(`/api/v1/titles/${titleId}/chapters/${chapterId}/read`),

  getNextChapter: (titleId: string): Promise<NextChapterResponse> =>
    fetchApi(`/api/v1/titles/${titleId}/next-chapter`),
};

// ==================== COLLECTIONS API ====================
export const collectionsApi = {
  getAll: (params?: {
    search?: string;
    page?: number;
    size?: number;
  }): Promise<Page<UserCollectionResponse>> =>
    fetchApi(`/api/v1/collections${buildQueryString(params || {})}`),

  getById: (id: string): Promise<UserCollectionResponse> =>
    fetchApi(`/api/v1/collections/${id}`),

  getMy: (): Promise<UserCollectionResponse[]> =>
    fetchApi("/api/v1/collections/my"),

  getByShareToken: (token: string): Promise<UserCollectionResponse> =>
    fetchApi(`/api/v1/collections/shared/${token}`),

  create: (data: CollectionCreateRequest): Promise<UserCollectionResponse> =>
    fetchApi("/api/v1/collections", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  update: (
    id: string,
    data: CollectionUpdateRequest
  ): Promise<UserCollectionResponse> =>
    fetchApi(`/api/v1/collections/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (id: string): Promise<void> =>
    fetchApi(`/api/v1/collections/${id}`, {
      method: "DELETE",
    }),

  generateShareLink: (id: string): Promise<ShareLinkResponse> =>
    fetchApi(`/api/v1/collections/${id}/share`, {
      method: "POST",
    }),

  revokeShareLink: (id: string): Promise<void> =>
    fetchApi(`/api/v1/collections/${id}/share`, {
      method: "DELETE",
    }),

  addTitles: (id: string, titleIds: string[]): Promise<UserCollectionResponse> =>
    fetchApi(`/api/v1/collections/${id}/titles`, {
      method: "POST",
      body: JSON.stringify({ titleIds }),
    }),

  removeTitle: (id: string, titleId: string): Promise<UserCollectionResponse> =>
    fetchApi(`/api/v1/collections/${id}/titles/${titleId}`, {
      method: "DELETE",
    }),
};

// ==================== LIBRARY API ====================
export const libraryApi = {
  getAll: (params?: {
    status?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<Page<LibraryEntryResponse>> =>
    fetchApi(`/api/v1/library${buildQueryString(params || {})}`),

  getById: (entryId: string): Promise<LibraryEntryResponse> =>
    fetchApi(`/api/v1/library/${entryId}`),

  getUserLibrary: (
    userId: string,
    params?: {
      page?: number;
      size?: number;
    }
  ): Promise<Page<LibraryEntryResponse>> =>
    fetchApi(`/api/v1/users/${userId}/library${buildQueryString(params || {})}`),

  create: (data: LibraryEntryCreateRequest): Promise<LibraryEntryResponse> =>
    fetchApi("/api/v1/library", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  update: (
    entryId: string,
    data: LibraryEntryUpdateRequest
  ): Promise<LibraryEntryResponse> =>
    fetchApi(`/api/v1/library/${entryId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  delete: (entryId: string): Promise<void> =>
    fetchApi(`/api/v1/library/${entryId}`, {
      method: "DELETE",
    }),
};

// ==================== ANALYTICS API ====================
export const analyticsApi = {
  getTitleAnalytics: (titleId: string): Promise<TitleAnalyticsResponse> =>
    fetchApi(`/api/v1/analytics/titles/${titleId}`),
};

// ==================== MEDIA API ====================
export const mediaApi = {
  getUploadUrl: (data: MediaUploadUrlRequest): Promise<MediaUploadUrlResponse> =>
    fetchApi("/api/v1/media/upload-url", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  getMediaUrl: (mediaId: string): Promise<{ url: string }> =>
    fetchApi(`/api/v1/media/${mediaId}/url`),

  delete: (mediaId: string): Promise<void> =>
    fetchApi(`/api/v1/media/${mediaId}`, {
      method: "DELETE",
    }),
};

// Helper to get media URL directly
export function getMediaUrl(mediaId?: string): string | undefined {
  if (!mediaId) return undefined;
  return `${API_BASE_URL}/api/v1/media/${mediaId}/url`;
}

export { ApiError };
