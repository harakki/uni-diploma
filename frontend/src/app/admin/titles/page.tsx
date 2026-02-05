"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { ArrowLeft, Edit, Plus, Search, Trash2 } from "lucide-react";
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
import { titlesApi, tagsApi } from "@/lib/api";
import {
  TitleResponse,
  TitleCreateRequest,
  TitleUpdateRequest,
  TitleType,
  TitleStatus,
  ContentRating,
  TagResponse,
} from "@/types";

const typeOptions = Object.values(TitleType);
const statusOptions = Object.values(TitleStatus);
const ratingOptions = Object.values(ContentRating);

export default function AdminTitlesPage() {
  const [loading, setLoading] = useState(true);
  const [titles, setTitles] = useState<TitleResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [tags, setTags] = useState<TagResponse[]>([]);

  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedTitle, setSelectedTitle] = useState<TitleResponse | null>(null);

  const [formData, setFormData] = useState<TitleCreateRequest>({
    name: "",
    description: "",
    type: TitleType.MANGA,
    titleStatus: TitleStatus.ONGOING,
    contentRating: ContentRating.TWELVE_PLUS,
    countryIsoCode: "JP",
  });

  useEffect(() => {
    fetchTitles();
    fetchTags();
  }, [page, search]);

  const fetchTitles = async () => {
    setLoading(true);
    try {
      const data = await titlesApi.getAll({
        page,
        size: 20,
        search: search || undefined,
      });
      setTitles(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error("Failed to fetch titles:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchTags = async () => {
    try {
      const data = await tagsApi.getAll({ size: 100 });
      setTags(data.content);
    } catch (error) {
      console.error("Failed to fetch tags:", error);
    }
  };

  const handleCreate = async () => {
    try {
      await titlesApi.create(formData);
      setCreateDialogOpen(false);
      resetForm();
      fetchTitles();
    } catch (error) {
      console.error("Failed to create title:", error);
      alert("Ошибка при создании тайтла");
    }
  };

  const handleUpdate = async () => {
    if (!selectedTitle) return;
    try {
      const updateData: TitleUpdateRequest = {
        name: formData.name,
        description: formData.description,
        type: formData.type,
        titleStatus: formData.titleStatus,
        contentRating: formData.contentRating,
        countryIsoCode: formData.countryIsoCode,
        releaseYear: formData.releaseYear,
      };
      await titlesApi.update(selectedTitle.id, updateData);
      setEditDialogOpen(false);
      resetForm();
      fetchTitles();
    } catch (error) {
      console.error("Failed to update title:", error);
      alert("Ошибка при обновлении тайтла");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Удалить этот тайтл?")) return;
    try {
      await titlesApi.delete(id);
      fetchTitles();
    } catch (error) {
      console.error("Failed to delete title:", error);
      alert("Ошибка при удалении тайтла");
    }
  };

  const openEditDialog = (title: TitleResponse) => {
    setSelectedTitle(title);
    setFormData({
      name: title.name,
      description: title.description || "",
      type: title.type,
      titleStatus: title.titleStatus,
      contentRating: title.contentRating,
      countryIsoCode: title.countryIsoCode,
      releaseYear: title.releaseYear,
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
      description: "",
      type: TitleType.MANGA,
      titleStatus: TitleStatus.ONGOING,
      contentRating: ContentRating.TWELVE_PLUS,
      countryIsoCode: "JP",
    });
    setSelectedTitle(null);
  };

  const FormFields = () => (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="name">Название *</Label>
        <Input
          id="name"
          value={formData.name}
          onChange={(e) => setFormData((prev) => ({ ...prev, name: e.target.value }))}
          required
        />
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

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label>Тип *</Label>
          <Select
            value={formData.type}
            onValueChange={(v) =>
              setFormData((prev) => ({ ...prev, type: v as TitleType }))
            }
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {typeOptions.map((type) => (
                <SelectItem key={type} value={type}>
                  {type}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label>Статус *</Label>
          <Select
            value={formData.titleStatus}
            onValueChange={(v) =>
              setFormData((prev) => ({ ...prev, titleStatus: v as TitleStatus }))
            }
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {statusOptions.map((status) => (
                <SelectItem key={status} value={status}>
                  {status}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label>Возрастной рейтинг *</Label>
          <Select
            value={formData.contentRating}
            onValueChange={(v) =>
              setFormData((prev) => ({ ...prev, contentRating: v as ContentRating }))
            }
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {ratingOptions.map((rating) => (
                <SelectItem key={rating} value={rating}>
                  {rating}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="country">Код страны *</Label>
          <Input
            id="country"
            value={formData.countryIsoCode}
            onChange={(e) =>
              setFormData((prev) => ({ ...prev, countryIsoCode: e.target.value }))
            }
            maxLength={2}
            placeholder="JP"
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="year">Год выпуска</Label>
        <Input
          id="year"
          type="number"
          value={formData.releaseYear || ""}
          onChange={(e) =>
            setFormData((prev) => ({
              ...prev,
              releaseYear: e.target.value ? Number(e.target.value) : undefined,
            }))
          }
          placeholder="2024"
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
            <h1 className="text-3xl font-bold">Управление тайтлами</h1>
          </div>
          <Button onClick={openCreateDialog}>
            <Plus className="h-4 w-4 mr-2" />
            Добавить тайтл
          </Button>
        </div>

        {/* Search */}
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Поиск по названию..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="pl-10"
            />
          </div>
        </div>

        {/* Table */}
        <div className="border rounded-lg">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Название</TableHead>
                <TableHead>Тип</TableHead>
                <TableHead>Статус</TableHead>
                <TableHead>Рейтинг</TableHead>
                <TableHead>Год</TableHead>
                <TableHead className="text-right">Действия</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                Array.from({ length: 10 }).map((_, i) => (
                  <TableRow key={i}>
                    <TableCell colSpan={6} className="h-12">
                      <div className="h-4 bg-muted rounded animate-pulse" />
                    </TableCell>
                  </TableRow>
                ))
              ) : titles.length > 0 ? (
                titles.map((title) => (
                  <TableRow key={title.id}>
                    <TableCell className="font-medium">
                      <Link
                        href={`/titles/${title.slug}`}
                        className="hover:underline"
                      >
                        {title.name}
                      </Link>
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{title.type}</Badge>
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary">{title.titleStatus}</Badge>
                    </TableCell>
                    <TableCell>{title.contentRating}</TableCell>
                    <TableCell>{title.releaseYear || "-"}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => openEditDialog(title)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-destructive"
                          onClick={() => handleDelete(title.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-8">
                    Тайтлы не найдены
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
            <DialogTitle>Создать тайтл</DialogTitle>
            <DialogDescription>
              Заполните информацию о новом тайтле
            </DialogDescription>
          </DialogHeader>
          <FormFields />
          <DialogFooter>
            <Button variant="outline" onClick={() => setCreateDialogOpen(false)}>
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
            <DialogTitle>Редактировать тайтл</DialogTitle>
            <DialogDescription>Измените информацию о тайтле</DialogDescription>
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
