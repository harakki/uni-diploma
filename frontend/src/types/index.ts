// Enums matching backend
export enum TitleType {
  COMIC = "COMIC",
  MANGA = "MANGA",
  MANHWA = "MANHWA",
  MANHUA = "MANHUA",
  ARTBOOK = "ARTBOOK",
  NOVEL = "NOVEL",
}

export enum TitleStatus {
  ONGOING = "ONGOING",
  COMPLETED = "COMPLETED",
  ANNOUNCED = "ANNOUNCED",
  SUSPENDED = "SUSPENDED",
  DISCONTINUED = "DISCONTINUED",
}

export enum ContentRating {
  SIX_PLUS = "SIX_PLUS",
  TWELVE_PLUS = "TWELVE_PLUS",
  SIXTEEN_PLUS = "SIXTEEN_PLUS",
  EIGHTEEN_PLUS = "EIGHTEEN_PLUS",
}

export enum TagType {
  CONTENT_WARNING = "CONTENT_WARNING",
  GENRE = "GENRE",
  THEME = "THEME",
}

export enum AuthorRole {
  STORY = "STORY",
  ART = "ART",
  STORY_AND_ART = "STORY_AND_ART",
}

export enum ReadingStatus {
  TO_READ = "TO_READ",
  READING = "READING",
  ON_HOLD = "ON_HOLD",
  DROPPED = "DROPPED",
  COMPLETED = "COMPLETED",
  RE_READING = "RE_READING",
}

export enum VoteType {
  LIKE = "LIKE",
  DISLIKE = "DISLIKE",
}

// DTOs
export interface TagResponse {
  id: string;
  name: string;
  slug: string;
  type: TagType;
  description?: string;
}

export interface PublisherResponse {
  id: string;
  name: string;
  slug: string;
  description?: string;
  websiteUrl?: string;
  countryIsoCode?: string;
  logoMediaId?: string;
}

export interface AuthorResponse {
  id: string;
  name: string;
  slug: string;
  description?: string;
  websiteUrls?: string[];
  countryIsoCode?: string;
  mainCoverMediaId?: string;
}

export interface TitleAuthorResponse {
  author: AuthorResponse;
  role: AuthorRole;
}

export interface TitleResponse {
  id: string;
  name: string;
  slug: string;
  description?: string;
  type: TitleType;
  titleStatus: TitleStatus;
  releaseYear?: number;
  contentRating: ContentRating;
  isLicensed?: boolean;
  countryIsoCode: string;
  mainCoverMediaId?: string;
  authors?: TitleAuthorResponse[];
  publisher?: PublisherResponse;
  tags?: TagResponse[];
}

export interface TitleCreateRequest {
  name: string;
  description?: string;
  type: TitleType;
  titleStatus: TitleStatus;
  releaseYear?: number;
  contentRating: ContentRating;
  countryIsoCode: string;
  mainCoverMediaId?: string;
  authorIds?: Record<string, AuthorRole>;
  publisherId?: string;
  tagIds?: string[];
}

export interface TitleUpdateRequest {
  name?: string;
  description?: string;
  type?: TitleType;
  titleStatus?: TitleStatus;
  releaseYear?: number;
  contentRating?: ContentRating;
  isLicensed?: boolean;
  countryIsoCode?: string;
  mainCoverMediaId?: string;
  publisherId?: string;
}

export interface AuthorCreateRequest {
  name: string;
  description?: string;
  websiteUrls?: string[];
  countryIsoCode?: string;
  mainCoverMediaId?: string;
}

export interface AuthorUpdateRequest {
  name?: string;
  description?: string;
  websiteUrls?: string[];
  countryIsoCode?: string;
  mainCoverMediaId?: string;
}

export interface PublisherCreateRequest {
  name: string;
  description?: string;
  websiteUrl?: string;
  countryIsoCode?: string;
  logoMediaId?: string;
}

export interface PublisherUpdateRequest {
  name?: string;
  description?: string;
  websiteUrl?: string;
  countryIsoCode?: string;
  logoMediaId?: string;
}

export interface TagCreateRequest {
  name: string;
  type: TagType;
  description?: string;
}

export interface TagUpdateRequest {
  name?: string;
  type?: TagType;
  description?: string;
}

export interface ReplaceSlugRequest {
  slug: string;
}

export interface ReplaceTagsRequest {
  tagIds: string[];
}

export interface TitleAddAuthorRequest {
  authorId: string;
  role: AuthorRole;
}

// Chapter DTOs
export interface PageResponse {
  id: string;
  mediaId: string;
  pageNumber: number;
}

export interface ChapterSummaryResponse {
  id: string;
  displayNumber: string;
  name?: string;
  volume?: number;
}

export interface ChapterDetailsResponse {
  id: string;
  titleId: string;
  displayNumber: string;
  name?: string;
  pages: PageResponse[];
}

export interface ChapterCreateRequest {
  displayNumber: string;
  name?: string;
  volume?: number;
  pageMediaIds: string[];
}

export interface ChapterUpdateRequest {
  displayNumber?: string;
  name?: string;
  volume?: number;
}

export interface ChapterPagesUpdateRequest {
  pageMediaIds: string[];
}

export interface ChapterReadRequest {
  isRead: boolean;
  pageNumber?: number;
}

export interface ChapterReadStatusResponse {
  isRead: boolean;
  lastPageNumber?: number;
}

export interface NextChapterResponse {
  chapterId?: string;
  displayNumber?: string;
  name?: string;
}

// Collection DTOs
export interface UserCollectionResponse {
  id: string;
  authorId: string;
  name: string;
  description?: string;
  isPublic: boolean;
  shareToken?: string;
  titleIds: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CollectionCreateRequest {
  name: string;
  description?: string;
  isPublic: boolean;
}

export interface CollectionUpdateRequest {
  name?: string;
  description?: string;
  isPublic?: boolean;
}

export interface ShareLinkResponse {
  shareToken: string;
  shareUrl: string;
}

// Library DTOs
export interface LibraryEntryResponse {
  id: string;
  userId: string;
  titleId: string;
  status: ReadingStatus;
  vote?: VoteType;
  lastReadChapterId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LibraryEntryCreateRequest {
  titleId: string;
  status: ReadingStatus;
  vote?: VoteType;
  lastReadChapterId?: string;
}

export interface LibraryEntryUpdateRequest {
  status?: ReadingStatus;
  vote?: VoteType;
  lastReadChapterId?: string;
}

// Analytics DTOs
export interface TitleAnalyticsResponse {
  titleId: string;
  averageRating?: number;
  totalViews?: number;
  lastUpdated?: string;
}

// Media DTOs
export interface MediaUploadUrlRequest {
  filename: string;
  contentType: string;
}

export interface MediaUploadUrlResponse {
  mediaId: string;
  uploadUrl: string;
}

// Pageable response
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
