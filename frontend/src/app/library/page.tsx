"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import Image from "next/image";
import { BookOpen, Library, Play, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { libraryApi, titlesApi, getMediaUrl } from "@/lib/api";
import {
  LibraryEntryResponse,
  TitleResponse,
  ReadingStatus,
  VoteType,
} from "@/types";

const statusLabels: Record<ReadingStatus, string> = {
  [ReadingStatus.TO_READ]: "Буду читать",
  [ReadingStatus.READING]: "Читаю",
  [ReadingStatus.ON_HOLD]: "В паузе",
  [ReadingStatus.DROPPED]: "Брошено",
  [ReadingStatus.COMPLETED]: "Прочитано",
  [ReadingStatus.RE_READING]: "Перечитываю",
};

const statusColors: Record<ReadingStatus, string> = {
  [ReadingStatus.TO_READ]: "bg-blue-500",
  [ReadingStatus.READING]: "bg-green-500",
  [ReadingStatus.ON_HOLD]: "bg-yellow-500",
  [ReadingStatus.DROPPED]: "bg-red-500",
  [ReadingStatus.COMPLETED]: "bg-purple-500",
  [ReadingStatus.RE_READING]: "bg-cyan-500",
};

interface LibraryItem {
  entry: LibraryEntryResponse;
  title: TitleResponse | null;
}

export default function LibraryPage() {
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState<LibraryItem[]>([]);
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL");

  useEffect(() => {
    fetchLibrary();
  }, []);

  const fetchLibrary = async () => {
    setLoading(true);
    try {
      const data = await libraryApi.getAll({ size: 100 });

      // Fetch title details for each entry
      const itemsWithTitles = await Promise.all(
        data.content.map(async (entry) => {
          try {
            const title = await titlesApi.getById(entry.titleId);
            return { entry, title };
          } catch {
            return { entry, title: null };
          }
        })
      );

      setItems(itemsWithTitles);
    } catch (error) {
      console.error("Failed to fetch library:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (
    entryId: string,
    status: ReadingStatus
  ) => {
    try {
      await libraryApi.update(entryId, { status });
      setItems((prev) =>
        prev.map((item) =>
          item.entry.id === entryId
            ? { ...item, entry: { ...item.entry, status } }
            : item
        )
      );
    } catch (error) {
      console.error("Failed to update status:", error);
    }
  };

  const handleRemove = async (entryId: string) => {
    if (!confirm("Удалить из библиотеки?")) return;
    try {
      await libraryApi.delete(entryId);
      setItems((prev) => prev.filter((item) => item.entry.id !== entryId));
    } catch (error) {
      console.error("Failed to remove from library:", error);
    }
  };

  const filteredItems =
    selectedStatus === "ALL"
      ? items
      : items.filter((item) => item.entry.status === selectedStatus);

  const statusCounts = items.reduce(
    (acc, item) => {
      acc[item.entry.status] = (acc[item.entry.status] || 0) + 1;
      return acc;
    },
    {} as Record<string, number>
  );

  return (
    <div className="container py-6">
      <div className="flex flex-col gap-6">
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold flex items-center gap-2">
              <Library className="h-8 w-8 text-primary" />
              Моя библиотека
            </h1>
            <p className="text-muted-foreground">
              {items.length} тайтлов в библиотеке
            </p>
          </div>
        </div>

        {/* Filter Tabs */}
        <Tabs
          value={selectedStatus}
          onValueChange={setSelectedStatus}
          className="w-full"
        >
          <TabsList className="w-full justify-start overflow-x-auto flex-wrap h-auto gap-1">
            <TabsTrigger value="ALL">Все ({items.length})</TabsTrigger>
            {Object.entries(statusLabels).map(([status, label]) => (
              <TabsTrigger key={status} value={status}>
                {label} ({statusCounts[status] || 0})
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>

        {/* Library Grid */}
        {loading ? (
          <div className="grid gap-4">
            {Array.from({ length: 10 }).map((_, i) => (
              <Card key={i} className="animate-pulse">
                <CardContent className="flex gap-4 p-4">
                  <div className="w-20 h-28 bg-muted rounded" />
                  <div className="flex-1 space-y-2">
                    <div className="h-5 bg-muted rounded w-2/3" />
                    <div className="h-4 bg-muted rounded w-1/2" />
                    <div className="h-4 bg-muted rounded w-1/4" />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : filteredItems.length > 0 ? (
          <div className="grid gap-4">
            {filteredItems.map(({ entry, title }) => (
              <Card key={entry.id}>
                <CardContent className="flex gap-4 p-4">
                  {/* Cover */}
                  <Link
                    href={title ? `/titles/${title.slug}` : "#"}
                    className="shrink-0"
                  >
                    <div className="relative w-20 h-28 rounded overflow-hidden">
                      {title?.mainCoverMediaId ? (
                        <Image
                          src={getMediaUrl(title.mainCoverMediaId) || ""}
                          alt={title.name}
                          fill
                          className="object-cover"
                        />
                      ) : (
                        <div className="w-full h-full bg-muted flex items-center justify-center">
                          <BookOpen className="h-8 w-8 text-muted-foreground" />
                        </div>
                      )}
                    </div>
                  </Link>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <Link
                      href={title ? `/titles/${title.slug}` : "#"}
                      className="hover:underline"
                    >
                      <h3 className="font-semibold truncate">
                        {title?.name || "Неизвестный тайтл"}
                      </h3>
                    </Link>

                    <div className="flex flex-wrap gap-2 mt-2">
                      <Badge
                        className={`${statusColors[entry.status]} text-white`}
                      >
                        {statusLabels[entry.status]}
                      </Badge>
                      {entry.vote && (
                        <Badge
                          variant={
                            entry.vote === VoteType.LIKE
                              ? "default"
                              : "destructive"
                          }
                        >
                          {entry.vote === VoteType.LIKE
                            ? "Нравится"
                            : "Не нравится"}
                        </Badge>
                      )}
                    </div>

                    <p className="text-xs text-muted-foreground mt-2">
                      Добавлено:{" "}
                      {new Date(entry.createdAt).toLocaleDateString("ru-RU")}
                    </p>
                  </div>

                  {/* Actions */}
                  <div className="flex flex-col gap-2 shrink-0">
                    {title && (
                      <Button variant="outline" size="sm" asChild>
                        <Link href={`/titles/${title.slug}`}>
                          <Play className="h-4 w-4 mr-1" />
                          Читать
                        </Link>
                      </Button>
                    )}

                    <Select
                      value={entry.status}
                      onValueChange={(v) =>
                        handleUpdateStatus(entry.id, v as ReadingStatus)
                      }
                    >
                      <SelectTrigger className="w-[140px]">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.entries(statusLabels).map(([value, label]) => (
                          <SelectItem key={value} value={value}>
                            {label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>

                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive"
                      onClick={() => handleRemove(entry.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <Library className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground mb-4">
              {selectedStatus === "ALL"
                ? "Ваша библиотека пуста"
                : "Нет тайтлов с таким статусом"}
            </p>
            <Button asChild>
              <Link href="/search">Найти тайтлы</Link>
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
