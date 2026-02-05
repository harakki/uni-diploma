"use client";

import { useState, useEffect, use } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  ArrowLeft,
  BookOpen,
  Calendar,
  Clock,
  Globe,
  Heart,
  HeartOff,
  Library,
  Play,
  User,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  titlesApi,
  chaptersApi,
  libraryApi,
  readingProgressApi,
  analyticsApi,
  getMediaUrl,
} from "@/lib/api";
import {
  TitleResponse,
  TitleType,
  TitleStatus,
  ContentRating,
  ChapterSummaryResponse,
  LibraryEntryResponse,
  ReadingStatus,
  VoteType,
  TitleAnalyticsResponse,
} from "@/types";

const typeLabels: Record<TitleType, string> = {
  [TitleType.MANGA]: "Манга",
  [TitleType.MANHWA]: "Манхва",
  [TitleType.MANHUA]: "Маньхуа",
  [TitleType.COMIC]: "Комикс",
  [TitleType.ARTBOOK]: "Артбук",
  [TitleType.NOVEL]: "Новелла",
};

const statusLabels: Record<TitleStatus, string> = {
  [TitleStatus.ONGOING]: "Выходит",
  [TitleStatus.COMPLETED]: "Завершен",
  [TitleStatus.ANNOUNCED]: "Анонс",
  [TitleStatus.SUSPENDED]: "Приостановлен",
  [TitleStatus.DISCONTINUED]: "Прекращен",
};

const ratingLabels: Record<ContentRating, string> = {
  [ContentRating.SIX_PLUS]: "6+",
  [ContentRating.TWELVE_PLUS]: "12+",
  [ContentRating.SIXTEEN_PLUS]: "16+",
  [ContentRating.EIGHTEEN_PLUS]: "18+",
};

const readingStatusOptions = [
  { value: "TO_READ", label: "Буду читать" },
  { value: "READING", label: "Читаю" },
  { value: "ON_HOLD", label: "В паузе" },
  { value: "DROPPED", label: "Брошено" },
  { value: "COMPLETED", label: "Прочитано" },
  { value: "RE_READING", label: "Перечитываю" },
];

interface TitlePageProps {
  params: Promise<{ slug: string }>;
}

export default function TitlePage({ params }: TitlePageProps) {
  const { slug } = use(params);
  const router = useRouter();

  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState<TitleResponse | null>(null);
  const [chapters, setChapters] = useState<ChapterSummaryResponse[]>([]);
  const [libraryEntry, setLibraryEntry] = useState<LibraryEntryResponse | null>(
    null
  );
  const [analytics, setAnalytics] = useState<TitleAnalyticsResponse | null>(
    null
  );
  const [nextChapterId, setNextChapterId] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const titleData = await titlesApi.getBySlug(slug);
        setTitle(titleData);

        // Fetch chapters
        const chaptersData = await chaptersApi.getAllByTitle(titleData.id);
        setChapters(chaptersData);

        // Fetch analytics
        try {
          const analyticsData = await analyticsApi.getTitleAnalytics(
            titleData.id
          );
          setAnalytics(analyticsData);
        } catch {
          // Analytics might not be available
        }

        // Fetch library entry (if authenticated)
        try {
          const libraryData = await libraryApi.getAll({ size: 100 });
          const entry = libraryData.content.find(
            (e) => e.titleId === titleData.id
          );
          if (entry) setLibraryEntry(entry);
        } catch {
          // User might not be authenticated
        }

        // Fetch next chapter
        try {
          const nextChapter = await readingProgressApi.getNextChapter(
            titleData.id
          );
          if (nextChapter.chapterId) {
            setNextChapterId(nextChapter.chapterId);
          }
        } catch {
          // User might not be authenticated
        }
      } catch (error) {
        console.error("Failed to fetch title:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [slug]);

  const handleAddToLibrary = async (status: ReadingStatus) => {
    if (!title) return;
    try {
      const entry = await libraryApi.create({
        titleId: title.id,
        status,
      });
      setLibraryEntry(entry);
    } catch (error) {
      console.error("Failed to add to library:", error);
    }
  };

  const handleUpdateLibraryStatus = async (status: ReadingStatus) => {
    if (!libraryEntry) return;
    try {
      const entry = await libraryApi.update(libraryEntry.id, { status });
      setLibraryEntry(entry);
    } catch (error) {
      console.error("Failed to update library:", error);
    }
  };

  const handleVote = async (vote: VoteType) => {
    if (!title) return;
    try {
      if (libraryEntry) {
        const newVote = libraryEntry.vote === vote ? undefined : vote;
        const entry = await libraryApi.update(libraryEntry.id, {
          vote: newVote,
        });
        setLibraryEntry(entry);
      } else {
        const entry = await libraryApi.create({
          titleId: title.id,
          status: ReadingStatus.TO_READ,
          vote,
        });
        setLibraryEntry(entry);
      }
    } catch (error) {
      console.error("Failed to vote:", error);
    }
  };

  const handleRemoveFromLibrary = async () => {
    if (!libraryEntry) return;
    try {
      await libraryApi.delete(libraryEntry.id);
      setLibraryEntry(null);
    } catch (error) {
      console.error("Failed to remove from library:", error);
    }
  };

  if (loading) {
    return (
      <div className="container py-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 w-48 bg-muted rounded" />
          <div className="grid md:grid-cols-[300px,1fr] gap-6">
            <div className="aspect-[3/4] bg-muted rounded" />
            <div className="space-y-4">
              <div className="h-8 w-3/4 bg-muted rounded" />
              <div className="h-4 w-1/2 bg-muted rounded" />
              <div className="h-24 bg-muted rounded" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!title) {
    return (
      <div className="container py-6">
        <div className="text-center py-12">
          <p className="text-muted-foreground">Тайтл не найден</p>
          <Button asChild className="mt-4">
            <Link href="/search">Вернуться к поиску</Link>
          </Button>
        </div>
      </div>
    );
  }

  const coverUrl = getMediaUrl(title.mainCoverMediaId);
  const firstChapter = chapters[0];

  return (
    <div className="container py-6">
      {/* Back Button */}
      <Button variant="ghost" size="sm" asChild className="mb-4">
        <Link href="/search">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Назад к каталогу
        </Link>
      </Button>

      {/* Title Header */}
      <div className="grid md:grid-cols-[300px,1fr] gap-6">
        {/* Cover */}
        <div className="space-y-4">
          <div className="relative aspect-[3/4] rounded-lg overflow-hidden">
            {coverUrl ? (
              <Image
                src={coverUrl}
                alt={title.name}
                fill
                className="object-cover"
                priority
              />
            ) : (
              <div className="w-full h-full bg-muted flex items-center justify-center">
                <span className="text-muted-foreground">Нет обложки</span>
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="space-y-2">
            {nextChapterId ? (
              <Button asChild className="w-full">
                <Link href={`/chapters/${nextChapterId}`}>
                  <Play className="h-4 w-4 mr-2" />
                  Продолжить чтение
                </Link>
              </Button>
            ) : firstChapter ? (
              <Button asChild className="w-full">
                <Link href={`/chapters/${firstChapter.id}`}>
                  <BookOpen className="h-4 w-4 mr-2" />
                  Начать читать
                </Link>
              </Button>
            ) : (
              <Button disabled className="w-full">
                Нет глав
              </Button>
            )}

            {libraryEntry ? (
              <Select
                value={libraryEntry.status}
                onValueChange={(v) =>
                  handleUpdateLibraryStatus(v as ReadingStatus)
                }
              >
                <SelectTrigger className="w-full">
                  <Library className="h-4 w-4 mr-2" />
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {readingStatusOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            ) : (
              <Select
                onValueChange={(v) => handleAddToLibrary(v as ReadingStatus)}
              >
                <SelectTrigger className="w-full">
                  <Library className="h-4 w-4 mr-2" />
                  <SelectValue placeholder="Добавить в библиотеку" />
                </SelectTrigger>
                <SelectContent>
                  {readingStatusOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}

            <div className="flex gap-2">
              <Button
                variant={
                  libraryEntry?.vote === VoteType.LIKE ? "default" : "outline"
                }
                size="sm"
                className="flex-1"
                onClick={() => handleVote(VoteType.LIKE)}
              >
                <Heart
                  className={`h-4 w-4 mr-1 ${
                    libraryEntry?.vote === VoteType.LIKE ? "fill-current" : ""
                  }`}
                />
                Нравится
              </Button>
              <Button
                variant={
                  libraryEntry?.vote === VoteType.DISLIKE
                    ? "destructive"
                    : "outline"
                }
                size="sm"
                className="flex-1"
                onClick={() => handleVote(VoteType.DISLIKE)}
              >
                <HeartOff className="h-4 w-4 mr-1" />
                Не нравится
              </Button>
            </div>

            {libraryEntry && (
              <Button
                variant="ghost"
                size="sm"
                className="w-full text-destructive"
                onClick={handleRemoveFromLibrary}
              >
                Удалить из библиотеки
              </Button>
            )}
          </div>
        </div>

        {/* Info */}
        <div className="space-y-4">
          <div>
            <h1 className="text-3xl font-bold">{title.name}</h1>
            <div className="flex flex-wrap gap-2 mt-2">
              <Badge>{typeLabels[title.type]}</Badge>
              <Badge variant="outline">{statusLabels[title.titleStatus]}</Badge>
              <Badge variant="outline">{ratingLabels[title.contentRating]}</Badge>
            </div>
          </div>

          {/* Stats */}
          <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
            {title.releaseYear && (
              <div className="flex items-center gap-1">
                <Calendar className="h-4 w-4" />
                {title.releaseYear}
              </div>
            )}
            {title.countryIsoCode && (
              <div className="flex items-center gap-1">
                <Globe className="h-4 w-4" />
                {title.countryIsoCode}
              </div>
            )}
            <div className="flex items-center gap-1">
              <BookOpen className="h-4 w-4" />
              {chapters.length} глав
            </div>
            {analytics?.totalViews !== undefined && (
              <div className="flex items-center gap-1">
                <User className="h-4 w-4" />
                {analytics.totalViews} читателей
              </div>
            )}
          </div>

          {/* Description */}
          {title.description && (
            <p className="text-muted-foreground">{title.description}</p>
          )}

          {/* Authors */}
          {title.authors && title.authors.length > 0 && (
            <div>
              <h3 className="font-semibold mb-2">Авторы</h3>
              <div className="flex flex-wrap gap-2">
                {title.authors.map((ta) => (
                  <Badge key={ta.author.id} variant="secondary">
                    {ta.author.name}
                    <span className="ml-1 opacity-60">({ta.role})</span>
                  </Badge>
                ))}
              </div>
            </div>
          )}

          {/* Publisher */}
          {title.publisher && (
            <div>
              <h3 className="font-semibold mb-2">Издатель</h3>
              <Badge variant="secondary">{title.publisher.name}</Badge>
            </div>
          )}

          {/* Tags */}
          {title.tags && title.tags.length > 0 && (
            <div>
              <h3 className="font-semibold mb-2">Теги</h3>
              <div className="flex flex-wrap gap-2">
                {Array.from(title.tags).map((tag) => (
                  <Link key={tag.id} href={`/search?tags=${tag.slug}`}>
                    <Badge variant="outline" className="cursor-pointer hover:bg-accent">
                      {tag.name}
                    </Badge>
                  </Link>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      <Separator className="my-6" />

      {/* Chapters */}
      <Tabs defaultValue="chapters">
        <TabsList>
          <TabsTrigger value="chapters">Главы ({chapters.length})</TabsTrigger>
        </TabsList>
        <TabsContent value="chapters" className="mt-4">
          {chapters.length > 0 ? (
            <div className="grid gap-2">
              {chapters.map((chapter) => (
                <Link key={chapter.id} href={`/chapters/${chapter.id}`}>
                  <Card className="hover:bg-accent transition-colors">
                    <CardContent className="flex items-center justify-between p-4">
                      <div className="flex items-center gap-3">
                        <BookOpen className="h-4 w-4 text-muted-foreground" />
                        <div>
                          <span className="font-medium">
                            Глава {chapter.displayNumber}
                          </span>
                          {chapter.name && (
                            <span className="text-muted-foreground ml-2">
                              — {chapter.name}
                            </span>
                          )}
                        </div>
                      </div>
                      {chapter.volume && (
                        <Badge variant="outline">Том {chapter.volume}</Badge>
                      )}
                    </CardContent>
                  </Card>
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <p className="text-muted-foreground">Главы еще не добавлены</p>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
