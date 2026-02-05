"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Plus, Share2, Trash2, Lock, Globe, FolderHeart } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { collectionsApi } from "@/lib/api";
import { UserCollectionResponse, CollectionCreateRequest } from "@/types";

export default function CollectionsPage() {
  const [loading, setLoading] = useState(true);
  const [collections, setCollections] = useState<UserCollectionResponse[]>([]);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [newCollection, setNewCollection] = useState<CollectionCreateRequest>({
    name: "",
    description: "",
    isPublic: false,
  });

  useEffect(() => {
    fetchCollections();
  }, []);

  const fetchCollections = async () => {
    setLoading(true);
    try {
      const data = await collectionsApi.getMy();
      setCollections(data);
    } catch (error) {
      console.error("Failed to fetch collections:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    if (!newCollection.name.trim()) return;
    try {
      const created = await collectionsApi.create(newCollection);
      setCollections((prev) => [...prev, created]);
      setCreateDialogOpen(false);
      setNewCollection({ name: "", description: "", isPublic: false });
    } catch (error) {
      console.error("Failed to create collection:", error);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Вы уверены, что хотите удалить эту коллекцию?")) return;
    try {
      await collectionsApi.delete(id);
      setCollections((prev) => prev.filter((c) => c.id !== id));
    } catch (error) {
      console.error("Failed to delete collection:", error);
    }
  };

  const handleShare = async (collection: UserCollectionResponse) => {
    try {
      if (collection.shareToken) {
        // Copy existing link
        const url = `${window.location.origin}/collections/shared/${collection.shareToken}`;
        await navigator.clipboard.writeText(url);
        alert("Ссылка скопирована!");
      } else {
        // Generate new share link
        const result = await collectionsApi.generateShareLink(collection.id);
        const url = `${window.location.origin}/collections/shared/${result.shareToken}`;
        await navigator.clipboard.writeText(url);
        alert("Ссылка создана и скопирована!");
        fetchCollections();
      }
    } catch (error) {
      console.error("Failed to share collection:", error);
    }
  };

  return (
    <div className="container py-6">
      <div className="flex flex-col gap-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Мои коллекции</h1>
            <p className="text-muted-foreground">
              Создавайте коллекции тайтлов и делитесь ими
            </p>
          </div>
          <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Создать коллекцию
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Новая коллекция</DialogTitle>
                <DialogDescription>
                  Создайте коллекцию для организации ваших тайтлов
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Название</Label>
                  <Input
                    id="name"
                    value={newCollection.name}
                    onChange={(e) =>
                      setNewCollection((prev) => ({
                        ...prev,
                        name: e.target.value,
                      }))
                    }
                    placeholder="Моя коллекция"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">Описание</Label>
                  <Textarea
                    id="description"
                    value={newCollection.description || ""}
                    onChange={(e) =>
                      setNewCollection((prev) => ({
                        ...prev,
                        description: e.target.value,
                      }))
                    }
                    placeholder="Описание коллекции..."
                  />
                </div>
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="public"
                    checked={newCollection.isPublic}
                    onCheckedChange={(checked) =>
                      setNewCollection((prev) => ({
                        ...prev,
                        isPublic: checked as boolean,
                      }))
                    }
                  />
                  <Label htmlFor="public">Публичная коллекция</Label>
                </div>
              </div>
              <DialogFooter>
                <Button
                  variant="outline"
                  onClick={() => setCreateDialogOpen(false)}
                >
                  Отмена
                </Button>
                <Button onClick={handleCreate}>Создать</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>

        {/* Collections Grid */}
        {loading ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <Card key={i} className="animate-pulse">
                <CardHeader>
                  <div className="h-6 bg-muted rounded w-2/3" />
                  <div className="h-4 bg-muted rounded w-1/2 mt-2" />
                </CardHeader>
                <CardContent>
                  <div className="h-4 bg-muted rounded w-1/3" />
                </CardContent>
              </Card>
            ))}
          </div>
        ) : collections.length > 0 ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {collections.map((collection) => (
              <Card key={collection.id}>
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div>
                      <CardTitle className="flex items-center gap-2">
                        <FolderHeart className="h-5 w-5 text-primary" />
                        <Link
                          href={`/collections/${collection.id}`}
                          className="hover:underline"
                        >
                          {collection.name}
                        </Link>
                      </CardTitle>
                      {collection.description && (
                        <CardDescription className="mt-1 line-clamp-2">
                          {collection.description}
                        </CardDescription>
                      )}
                    </div>
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
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">
                      {collection.titleIds.length} тайтлов
                    </span>
                    <div className="flex gap-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleShare(collection)}
                      >
                        <Share2 className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleDelete(collection.id)}
                        className="text-destructive"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <FolderHeart className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground mb-4">
              У вас пока нет коллекций
            </p>
            <Button onClick={() => setCreateDialogOpen(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Создать первую коллекцию
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
