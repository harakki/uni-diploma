"use client";

import { useState, useEffect, use } from "react";
import Link from "next/link";
import { ArrowLeft, Edit, Globe, Lock, Plus, Share2, Trash2, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { TitleCard, TitleCardSkeleton } from "@/components/title-card";
import { collectionsApi, titlesApi } from "@/lib/api";
import {
  UserCollectionResponse,
  CollectionUpdateRequest,
  TitleResponse,
} from "@/types";

interface CollectionDetailPageProps {
  params: Promise<{ id: string }>;
}

export default function CollectionDetailPage({
  params,
}: CollectionDetailPageProps) {
  const { id } = use(params);

  const [loading, setLoading] = useState(true);
  const [collection, setCollection] = useState<UserCollectionResponse | null>(
    null
  );
  const [titles, setTitles] = useState<TitleResponse[]>([]);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editData, setEditData] = useState<CollectionUpdateRequest>({});

  useEffect(() => {
    fetchCollection();
  }, [id]);

  const fetchCollection = async () => {
    setLoading(true);
    try {
      const data = await collectionsApi.getById(id);
      setCollection(data);
      setEditData({
        name: data.name,
        description: data.description,
        isPublic: data.isPublic,
      });

      // Fetch titles
      const titlePromises = data.titleIds.map((titleId) =>
        titlesApi.getById(titleId).catch(() => null)
      );
      const titlesData = await Promise.all(titlePromises);
      setTitles(titlesData.filter((t): t is TitleResponse => t !== null));
    } catch (error) {
      console.error("Failed to fetch collection:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    if (!collection) return;
    try {
      const updated = await collectionsApi.update(collection.id, editData);
      setCollection(updated);
      setEditDialogOpen(false);
    } catch (error) {
      console.error("Failed to update collection:", error);
    }
  };

  const handleRemoveTitle = async (titleId: string) => {
    if (!collection) return;
    try {
      const updated = await collectionsApi.removeTitle(collection.id, titleId);
      setCollection(updated);
      setTitles((prev) => prev.filter((t) => t.id !== titleId));
    } catch (error) {
      console.error("Failed to remove title:", error);
    }
  };

  const handleShare = async () => {
    if (!collection) return;
    try {
      if (collection.shareToken) {
        const url = `${window.location.origin}/collections/shared/${collection.shareToken}`;
        await navigator.clipboard.writeText(url);
        alert("Ссылка скопирована!");
      } else {
        const result = await collectionsApi.generateShareLink(collection.id);
        const url = `${window.location.origin}/collections/shared/${result.shareToken}`;
        await navigator.clipboard.writeText(url);
        alert("Ссылка создана и скопирована!");
        fetchCollection();
      }
    } catch (error) {
      console.error("Failed to share collection:", error);
    }
  };

  if (loading) {
    return (
      <div className="container py-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 w-48 bg-muted rounded" />
          <div className="h-6 w-96 bg-muted rounded" />
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {Array.from({ length: 10 }).map((_, i) => (
              <TitleCardSkeleton key={i} />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (!collection) {
    return (
      <div className="container py-6">
        <div className="text-center py-12">
          <p className="text-muted-foreground">Коллекция не найдена</p>
          <Button asChild className="mt-4">
            <Link href="/collections">Назад к коллекциям</Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-6">
      {/* Header */}
      <Button variant="ghost" size="sm" asChild className="mb-4">
        <Link href="/collections">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Назад к коллекциям
        </Link>
      </Button>

      <div className="flex flex-col gap-6">
        <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold flex items-center gap-2">
              {collection.name}
              <Badge variant="outline">
                {collection.isPublic ? (
                  <>
                    <Globe className="h-3 w-3 mr-1" />
                    Публичная
                  </>
                ) : (
                  <>
                    <Lock className="h-3 w-3 mr-1" />
                    Приватная
                  </>
                )}
              </Badge>
            </h1>
            {collection.description && (
              <p className="text-muted-foreground mt-2">
                {collection.description}
              </p>
            )}
            <p className="text-sm text-muted-foreground mt-2">
              {titles.length} тайтлов
            </p>
          </div>

          <div className="flex gap-2">
            <Button variant="outline" onClick={handleShare}>
              <Share2 className="h-4 w-4 mr-2" />
              Поделиться
            </Button>
            <Button variant="outline" onClick={() => setEditDialogOpen(true)}>
              <Edit className="h-4 w-4 mr-2" />
              Редактировать
            </Button>
          </div>
        </div>

        {/* Titles Grid */}
        {titles.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {titles.map((title) => (
              <div key={title.id} className="relative group">
                <TitleCard title={title} />
                <Button
                  variant="destructive"
                  size="icon"
                  className="absolute top-2 right-2 h-8 w-8 opacity-0 group-hover:opacity-100 transition-opacity"
                  onClick={() => handleRemoveTitle(title.id)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-muted-foreground mb-4">
              В коллекции пока нет тайтлов
            </p>
            <Button asChild>
              <Link href="/search">
                <Plus className="h-4 w-4 mr-2" />
                Найти тайтлы
              </Link>
            </Button>
          </div>
        )}
      </div>

      {/* Edit Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Редактировать коллекцию</DialogTitle>
            <DialogDescription>
              Измените название, описание или настройки приватности
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="edit-name">Название</Label>
              <Input
                id="edit-name"
                value={editData.name || ""}
                onChange={(e) =>
                  setEditData((prev) => ({ ...prev, name: e.target.value }))
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="edit-description">Описание</Label>
              <Textarea
                id="edit-description"
                value={editData.description || ""}
                onChange={(e) =>
                  setEditData((prev) => ({
                    ...prev,
                    description: e.target.value,
                  }))
                }
              />
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox
                id="edit-public"
                checked={editData.isPublic}
                onCheckedChange={(checked) =>
                  setEditData((prev) => ({
                    ...prev,
                    isPublic: checked as boolean,
                  }))
                }
              />
              <Label htmlFor="edit-public">Публичная коллекция</Label>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditDialogOpen(false)}>
              Отмена
            </Button>
            <Button onClick={handleUpdate}>Сохранить</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
