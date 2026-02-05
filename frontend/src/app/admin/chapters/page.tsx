"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { ArrowLeft, Edit, Plus, Search, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { chaptersApi, titlesApi } from "@/lib/api";
import {
  ChapterSummaryResponse,
  ChapterCreateRequest,
  ChapterUpdateRequest,
  TitleResponse,
} from "@/types";

export default function AdminChaptersPage() {
  const [loading, setLoading] = useState(true);
  const [titles, setTitles] = useState<TitleResponse[]>([]);
  const [selectedTitleId, setSelectedTitleId] = useState<string>("");
  const [selectedTitle, setSelectedTitle] = useState<TitleResponse | null>(null);
  const [chapters, setChapters] = useState<ChapterSummaryResponse[]>([]);

  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedChapter, setSelectedChapter] =
    useState<ChapterSummaryResponse | null>(null);

  const [formData, setFormData] = useState<
    Omit<ChapterCreateRequest, "pageMediaIds">
  >({
    displayNumber: "",
    name: "",
    volume: undefined,
  });

  useEffect(() => {
    fetchTitles();
  }, []);

  useEffect(() => {
    if (selectedTitleId) {
      fetchChapters();
      const title = titles.find((t) => t.id === selectedTitleId);
      setSelectedTitle(title || null);
    } else {
      setChapters([]);
      setSelectedTitle(null);
    }
  }, [selectedTitleId, titles]);

  const fetchTitles = async () => {
    try {
      const data = await titlesApi.getAll({ size: 100 });
      setTitles(data.content);
    } catch (error) {
      console.error("Failed to fetch titles:", error);
    }
  };

  const fetchChapters = async () => {
    if (!selectedTitleId) return;
    setLoading(true);
    try {
      const data = await chaptersApi.getAllByTitle(selectedTitleId);
      setChapters(data);
    } catch (error) {
      console.error("Failed to fetch chapters:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    if (!selectedTitleId) return;
    try {
      await chaptersApi.create(selectedTitleId, {
        ...formData,
        pageMediaIds: [], // Initially empty, pages can be added later
      });
      setCreateDialogOpen(false);
      resetForm();
      fetchChapters();
    } catch (error) {
      console.error("Failed to create chapter:", error);
      alert("Ошибка при создании главы");
    }
  };

  const handleUpdate = async () => {
    if (!selectedChapter) return;
    try {
      const updateData: ChapterUpdateRequest = {
        displayNumber: formData.displayNumber,
        name: formData.name || undefined,
        volume: formData.volume,
      };
      await chaptersApi.update(selectedChapter.id, updateData);
      setEditDialogOpen(false);
      resetForm();
      fetchChapters();
    } catch (error) {
      console.error("Failed to update chapter:", error);
      alert("Ошибка при обновлении главы");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Удалить эту главу?")) return;
    try {
      await chaptersApi.delete(id);
      fetchChapters();
    } catch (error) {
      console.error("Failed to delete chapter:", error);
      alert("Ошибка при удалении главы");
    }
  };

  const openEditDialog = (chapter: ChapterSummaryResponse) => {
    setSelectedChapter(chapter);
    setFormData({
      displayNumber: chapter.displayNumber,
      name: chapter.name || "",
      volume: chapter.volume,
    });
    setEditDialogOpen(true);
  };

  const openCreateDialog = () => {
    resetForm();
    setCreateDialogOpen(true);
  };

  const resetForm = () => {
    setFormData({
      displayNumber: "",
      name: "",
      volume: undefined,
    });
    setSelectedChapter(null);
  };

  const FormFields = () => (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="displayNumber">Номер главы *</Label>
        <Input
          id="displayNumber"
          value={formData.displayNumber}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, displayNumber: e.target.value }))
          }
          placeholder="1"
          required
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="name">Название</Label>
        <Input
          id="name"
          value={formData.name || ""}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, name: e.target.value }))
          }
          placeholder="Начало"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="volume">Том</Label>
        <Input
          id="volume"
          type="number"
          value={formData.volume || ""}
          onChange={(e) =>
            setFormData((prev) => ({
              ...prev,
              volume: e.target.value ? Number(e.target.value) : undefined,
            }))
          }
          placeholder="1"
        />
      </div>
    </div>
  );

  return (
    <div className="container py-6">
      <div className="flex flex-col gap-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" asChild>
              <Link href="/admin">
                <ArrowLeft className="h-5 w-5" />
              </Link>
            </Button>
            <h1 className="text-3xl font-bold">Управление главами</h1>
          </div>
          {selectedTitleId && (
            <Button onClick={openCreateDialog}>
              <Plus className="h-4 w-4 mr-2" />
              Добавить главу
            </Button>
          )}
        </div>

        {/* Title Selection */}
        <div className="flex gap-4 items-end">
          <div className="flex-1 space-y-2">
            <Label>Выберите тайтл</Label>
            <Select value={selectedTitleId} onValueChange={setSelectedTitleId}>
              <SelectTrigger>
                <SelectValue placeholder="Выберите тайтл..." />
              </SelectTrigger>
              <SelectContent>
                {titles.map((title) => (
                  <SelectItem key={title.id} value={title.id}>
                    {title.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {selectedTitle && (
            <Button variant="outline" asChild>
              <Link href={`/titles/${selectedTitle.slug}`}>
                Открыть тайтл
              </Link>
            </Button>
          )}
        </div>

        {/* Chapters Table */}
        {selectedTitleId && (
          <div className="border rounded-lg">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Номер</TableHead>
                  <TableHead>Название</TableHead>
                  <TableHead>Том</TableHead>
                  <TableHead className="text-right">Действия</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={i}>
                      <TableCell colSpan={4} className="h-12">
                        <div className="h-4 bg-muted rounded animate-pulse" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : chapters.length > 0 ? (
                  chapters.map((chapter) => (
                    <TableRow key={chapter.id}>
                      <TableCell className="font-medium">
                        <Link
                          href={`/chapters/${chapter.id}`}
                          className="hover:underline"
                        >
                          Глава {chapter.displayNumber}
                        </Link>
                      </TableCell>
                      <TableCell>{chapter.name || "-"}</TableCell>
                      <TableCell>
                        {chapter.volume ? (
                          <Badge variant="outline">Том {chapter.volume}</Badge>
                        ) : (
                          "-"
                        )}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => openEditDialog(chapter)}
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="text-destructive"
                            onClick={() => handleDelete(chapter.id)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-8">
                      Главы не найдены
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>
        )}

        {!selectedTitleId && (
          <div className="text-center py-12 text-muted-foreground">
            Выберите тайтл для управления главами
          </div>
        )}
      </div>

      {/* Create Dialog */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Добавить главу</DialogTitle>
            <DialogDescription>
              Создайте новую главу для тайтла &quot;{selectedTitle?.name}&quot;
            </DialogDescription>
          </DialogHeader>
          <FormFields />
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

      {/* Edit Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Редактировать главу</DialogTitle>
            <DialogDescription>Измените информацию о главе</DialogDescription>
          </DialogHeader>
          <FormFields />
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
