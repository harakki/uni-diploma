"use client";

import { useState, useEffect, use, useCallback } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  ArrowLeft,
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  Home,
  List,
  Settings,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { chaptersApi, readingProgressApi, titlesApi, getMediaUrl } from "@/lib/api";
import {
  ChapterDetailsResponse,
  ChapterSummaryResponse,
  TitleResponse,
} from "@/types";

interface ChapterPageProps {
  params: Promise<{ id: string }>;
}

export default function ChapterPage({ params }: ChapterPageProps) {
  const { id } = use(params);
  const router = useRouter();

  const [loading, setLoading] = useState(true);
  const [chapter, setChapter] = useState<ChapterDetailsResponse | null>(null);
  const [title, setTitle] = useState<TitleResponse | null>(null);
  const [chapters, setChapters] = useState<ChapterSummaryResponse[]>([]);
  const [currentPageIndex, setCurrentPageIndex] = useState(0);
  const [showControls, setShowControls] = useState(true);
  const [readingMode, setReadingMode] = useState<"page" | "scroll">("page");

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const chapterData = await chaptersApi.getById(id);
        setChapter(chapterData);

        // Fetch title info
        const titleData = await titlesApi.getById(chapterData.titleId);
        setTitle(titleData);

        // Fetch all chapters for navigation
        const chaptersData = await chaptersApi.getAllByTitle(chapterData.titleId);
        setChapters(chaptersData);
      } catch (error) {
        console.error("Failed to fetch chapter:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  // Mark chapter as read when finished
  const markAsRead = useCallback(async () => {
    if (!chapter || !title) return;
    try {
      await readingProgressApi.setReadStatus(title.id, chapter.id, {
        isRead: true,
        pageNumber: chapter.pages.length,
      });
    } catch (error) {
      console.error("Failed to mark as read:", error);
    }
  }, [chapter, title]);

  // Update reading progress
  const updateProgress = useCallback(async (pageNumber: number) => {
    if (!chapter || !title) return;
    try {
      await readingProgressApi.setReadStatus(title.id, chapter.id, {
        isRead: false,
        pageNumber,
      });
    } catch (error) {
      console.error("Failed to update progress:", error);
    }
  }, [chapter, title]);

  // Navigation
  const currentChapterIndex = chapters.findIndex((c) => c.id === chapter?.id);
  const prevChapter = currentChapterIndex > 0 ? chapters[currentChapterIndex - 1] : null;
  const nextChapter =
    currentChapterIndex < chapters.length - 1
      ? chapters[currentChapterIndex + 1]
      : null;

  const goToPrevPage = useCallback(() => {
    if (currentPageIndex > 0) {
      setCurrentPageIndex((p) => p - 1);
    } else if (prevChapter) {
      router.push(`/chapters/${prevChapter.id}`);
    }
  }, [currentPageIndex, prevChapter, router]);

  const goToNextPage = useCallback(() => {
    if (!chapter) return;
    if (currentPageIndex < chapter.pages.length - 1) {
      const newIndex = currentPageIndex + 1;
      setCurrentPageIndex(newIndex);
      updateProgress(newIndex + 1);
    } else {
      markAsRead();
      if (nextChapter) {
        router.push(`/chapters/${nextChapter.id}`);
      }
    }
  }, [chapter, currentPageIndex, nextChapter, router, markAsRead, updateProgress]);

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "ArrowLeft" || e.key === "a") {
        goToPrevPage();
      } else if (e.key === "ArrowRight" || e.key === "d" || e.key === " ") {
        goToNextPage();
      } else if (e.key === "Escape") {
        if (title) {
          router.push(`/titles/${title.slug}`);
        }
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [goToPrevPage, goToNextPage, title, router]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-black">
        <div className="animate-spin h-8 w-8 border-2 border-white border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!chapter || !title) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-black text-white">
        <p>Глава не найдена</p>
        <Button asChild variant="outline" className="mt-4">
          <Link href="/">На главную</Link>
        </Button>
      </div>
    );
  }

  const currentPage = chapter.pages[currentPageIndex];
  const pageUrl = getMediaUrl(currentPage?.mediaId);

  return (
    <div
      className="min-h-screen bg-black text-white"
      onClick={() => setShowControls(!showControls)}
    >
      {/* Top Controls */}
      {showControls && (
        <div
          className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-b from-black/80 to-transparent p-4"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="container flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button variant="ghost" size="icon" asChild>
                <Link href={`/titles/${title.slug}`}>
                  <X className="h-5 w-5" />
                </Link>
              </Button>
              <div>
                <h1 className="font-medium truncate max-w-[200px] sm:max-w-none">
                  {title.name}
                </h1>
                <p className="text-sm text-white/60">
                  Глава {chapter.displayNumber}
                  {chapter.name && ` — ${chapter.name}`}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Select
                value={readingMode}
                onValueChange={(v) => setReadingMode(v as "page" | "scroll")}
              >
                <SelectTrigger className="w-[120px] bg-black/50">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="page">Постранично</SelectItem>
                  <SelectItem value="scroll">Лента</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </div>
      )}

      {/* Reader Content */}
      {readingMode === "page" ? (
        // Page Mode
        <div className="min-h-screen flex items-center justify-center">
          {pageUrl ? (
            <div className="relative w-full max-w-4xl mx-auto">
              <Image
                src={pageUrl}
                alt={`Page ${currentPageIndex + 1}`}
                width={1000}
                height={1400}
                className="w-full h-auto"
                priority
              />

              {/* Click areas for navigation */}
              <div
                className="absolute inset-y-0 left-0 w-1/3 cursor-pointer"
                onClick={(e) => {
                  e.stopPropagation();
                  goToPrevPage();
                }}
              />
              <div
                className="absolute inset-y-0 right-0 w-1/3 cursor-pointer"
                onClick={(e) => {
                  e.stopPropagation();
                  goToNextPage();
                }}
              />
            </div>
          ) : (
            <div className="text-white/60">Изображение не найдено</div>
          )}
        </div>
      ) : (
        // Scroll Mode
        <div className="max-w-4xl mx-auto py-16">
          {chapter.pages.map((page, index) => {
            const url = getMediaUrl(page.mediaId);
            return url ? (
              <Image
                key={page.id}
                src={url}
                alt={`Page ${index + 1}`}
                width={1000}
                height={1400}
                className="w-full h-auto"
                loading={index < 3 ? "eager" : "lazy"}
              />
            ) : null;
          })}
        </div>
      )}

      {/* Bottom Controls */}
      {showControls && (
        <div
          className="fixed bottom-0 left-0 right-0 z-50 bg-gradient-to-t from-black/80 to-transparent p-4"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="container">
            {/* Page Navigation (Page Mode) */}
            {readingMode === "page" && (
              <div className="flex items-center justify-between">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={goToPrevPage}
                  disabled={currentPageIndex === 0 && !prevChapter}
                >
                  <ChevronLeft className="h-4 w-4 mr-1" />
                  {currentPageIndex === 0 && prevChapter
                    ? "Пред. глава"
                    : "Назад"}
                </Button>

                <div className="flex items-center gap-4">
                  <span className="text-sm">
                    {currentPageIndex + 1} / {chapter.pages.length}
                  </span>
                  <Select
                    value={String(currentPageIndex)}
                    onValueChange={(v) => setCurrentPageIndex(Number(v))}
                  >
                    <SelectTrigger className="w-[100px] bg-black/50">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {chapter.pages.map((_, index) => (
                        <SelectItem key={index} value={String(index)}>
                          Стр. {index + 1}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <Button
                  variant="ghost"
                  size="sm"
                  onClick={goToNextPage}
                  disabled={
                    currentPageIndex === chapter.pages.length - 1 && !nextChapter
                  }
                >
                  {currentPageIndex === chapter.pages.length - 1 && nextChapter
                    ? "След. глава"
                    : "Вперед"}
                  <ChevronRight className="h-4 w-4 ml-1" />
                </Button>
              </div>
            )}

            {/* Chapter Navigation */}
            <Separator className="my-3 bg-white/20" />
            <div className="flex items-center justify-between">
              <Button
                variant="outline"
                size="sm"
                asChild
                disabled={!prevChapter}
                className="bg-transparent border-white/30"
              >
                {prevChapter ? (
                  <Link href={`/chapters/${prevChapter.id}`}>
                    <ArrowLeft className="h-4 w-4 mr-1" />
                    Глава {prevChapter.displayNumber}
                  </Link>
                ) : (
                  <span>
                    <ArrowLeft className="h-4 w-4 mr-1" />
                    Нет
                  </span>
                )}
              </Button>

              <Select
                value={chapter.id}
                onValueChange={(v) => router.push(`/chapters/${v}`)}
              >
                <SelectTrigger className="w-[150px] bg-black/50">
                  <List className="h-4 w-4 mr-2" />
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {chapters.map((c) => (
                    <SelectItem key={c.id} value={c.id}>
                      Глава {c.displayNumber}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Button
                variant="outline"
                size="sm"
                asChild
                disabled={!nextChapter}
                className="bg-transparent border-white/30"
              >
                {nextChapter ? (
                  <Link href={`/chapters/${nextChapter.id}`}>
                    Глава {nextChapter.displayNumber}
                    <ArrowRight className="h-4 w-4 ml-1" />
                  </Link>
                ) : (
                  <span>
                    Нет
                    <ArrowRight className="h-4 w-4 ml-1" />
                  </span>
                )}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
