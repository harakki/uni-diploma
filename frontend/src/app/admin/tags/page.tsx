"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { ArrowLeft, Edit, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
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
import { tagsApi } from "@/lib/api";
import { TagResponse, TagCreateRequest, TagUpdateRequest, TagType } from "@/types";

const tagTypeLabels: Record<TagType, string> = {
  [TagType.GENRE]: "Жанр",
  [TagType.THEME]: "Тема",
  [TagType.CONTENT_WARNING]: "Предупреждение",
};

const tagTypeColors: Record<TagType, string> = {
  [TagType.GENRE]: "bg-blue-500",
  [TagType.THEME]: "bg-green-500",
  [TagType.CONTENT_WARNING]: "bg-red-500",
};

export default function AdminTagsPage() {
  const [loading, setLoading] = useState(true);
  const [tags, setTags] = useState<TagResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);

  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedTag, setSelectedTag] = useState<TagResponse | null>(null);

  const [formData, setFormData] = useState<TagCreateRequest>({
    name: "",
    type: TagType.GENRE,
    description: "",
  });

  useEffect(() => {
    fetchTags();
  }, [page]);

  const fetchTags = async () => {
    setLoading(true);
    try {
      const data = await tagsApi.getAll({ page, size: 20 });
      setTags(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error("Failed to fetch tags:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    try {
      await tagsApi.create(formData);
      setCreateDialogOpen(false);
      resetForm();
      fetchTags();
    } catch (error) {
      console.error("Failed to create tag:", error);
      alert("Ошибка при создании тега");
    }
  };

  const handleUpdate = async () => {
    if (!selectedTag) return;
    try {
      const updateData: TagUpdateRequest = {
        name: formData.name,
        type: formData.type,
        description: formData.description,
      };
      await tagsApi.update(selectedTag.id, updateData);
      setEditDialogOpen(false);
      resetForm();
      fetchTags();
    } catch (error) {
      console.error("Failed to update tag:", error);
      alert("Ошибка при обновлении тега");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Удалить этот тег?")) return;
    try {
      await tagsApi.delete(id);
      fetchTags();
    } catch (error) {
      console.error("Failed to delete tag:", error);
      alert("Ошибка при удалении тега");
    }
  };

  const openEditDialog = (tag: TagResponse) => {
    setSelectedTag(tag);
    setFormData({
      name: tag.name,
      type: tag.type,
      description: tag.description || "",
    });
    setEditDialogOpen(true);
  };

  const openCreateDialog = () => {
    resetForm();
    setCreateDialogOpen(true);
  };

  const resetForm = () => {
    setFormData({
      name: "",
      type: TagType.GENRE,
      description: "",
    });
    setSelectedTag(null);
  };

  const FormFields = () => (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="name">Название *</Label>
        <Input
          id="name"
          value={formData.name}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, name: e.target.value }))
          }
          required
        />
      </div>

      <div className="space-y-2">
        <Label>Тип *</Label>
        <Select
          value={formData.type}
          onValueChange={(v) =>
            setFormData((prev) => ({ ...prev, type: v as TagType }))
          }
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {Object.entries(tagTypeLabels).map(([value, label]) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Описание</Label>
        <Textarea
          id="description"
          value={formData.description || ""}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, description: e.target.value }))
          }
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
            <h1 className="text-3xl font-bold">Управление тегами</h1>
          </div>
          <Button onClick={openCreateDialog}>
            <Plus className="h-4 w-4 mr-2" />
            Добавить тег
          </Button>
        </div>

        {/* Table */}
        <div className="border rounded-lg">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Название</TableHead>
                <TableHead>Тип</TableHead>
                <TableHead>Slug</TableHead>
                <TableHead>Описание</TableHead>
                <TableHead className="text-right">Действия</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                Array.from({ length: 10 }).map((_, i) => (
                  <TableRow key={i}>
                    <TableCell colSpan={5} className="h-12">
                      <div className="h-4 bg-muted rounded animate-pulse" />
                    </TableCell>
                  </TableRow>
                ))
              ) : tags.length > 0 ? (
                tags.map((tag) => (
                  <TableRow key={tag.id}>
                    <TableCell className="font-medium">{tag.name}</TableCell>
                    <TableCell>
                      <Badge className={`${tagTypeColors[tag.type]} text-white`}>
                        {tagTypeLabels[tag.type]}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {tag.slug}
                    </TableCell>
                    <TableCell className="max-w-xs truncate">
                      {tag.description || "-"}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => openEditDialog(tag)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-destructive"
                          onClick={() => handleDelete(tag.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={5} className="text-center py-8">
                    Теги не найдены
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex justify-center gap-2">
            <Button
              variant="outline"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
            >
              Назад
            </Button>
            <span className="flex items-center px-4">
              Страница {page + 1} из {totalPages}
            </span>
            <Button
              variant="outline"
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
            >
              Вперед
            </Button>
          </div>
        )}
      </div>

      {/* Create Dialog */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Добавить тег</DialogTitle>
            <DialogDescription>Создайте новый тег для каталога</DialogDescription>
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
            <DialogTitle>Редактировать тег</DialogTitle>
            <DialogDescription>Измените информацию о теге</DialogDescription>
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
