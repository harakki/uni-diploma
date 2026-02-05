"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { ArrowLeft, Edit, Plus, Search, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { authorsApi } from "@/lib/api";
import { AuthorResponse, AuthorCreateRequest, AuthorUpdateRequest } from "@/types";

export default function AdminAuthorsPage() {
  const [loading, setLoading] = useState(true);
  const [authors, setAuthors] = useState<AuthorResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");

  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedAuthor, setSelectedAuthor] = useState<AuthorResponse | null>(null);

  const [formData, setFormData] = useState<AuthorCreateRequest>({
    name: "",
    description: "",
    countryIsoCode: "",
    websiteUrls: [],
  });

  useEffect(() => {
    fetchAuthors();
  }, [page, search]);

  const fetchAuthors = async () => {
    setLoading(true);
    try {
      const data = await authorsApi.getAll({
        page,
        size: 20,
        search: search || undefined,
      });
      setAuthors(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error("Failed to fetch authors:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    try {
      await authorsApi.create(formData);
      setCreateDialogOpen(false);
      resetForm();
      fetchAuthors();
    } catch (error) {
      console.error("Failed to create author:", error);
      alert("Ошибка при создании автора");
    }
  };

  const handleUpdate = async () => {
    if (!selectedAuthor) return;
    try {
      const updateData: AuthorUpdateRequest = {
        name: formData.name,
        description: formData.description,
        countryIsoCode: formData.countryIsoCode,
        websiteUrls: formData.websiteUrls,
      };
      await authorsApi.update(selectedAuthor.id, updateData);
      setEditDialogOpen(false);
      resetForm();
      fetchAuthors();
    } catch (error) {
      console.error("Failed to update author:", error);
      alert("Ошибка при обновлении автора");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Удалить этого автора?")) return;
    try {
      await authorsApi.delete(id);
      fetchAuthors();
    } catch (error) {
      console.error("Failed to delete author:", error);
      alert("Ошибка при удалении автора");
    }
  };

  const openEditDialog = (author: AuthorResponse) => {
    setSelectedAuthor(author);
    setFormData({
      name: author.name,
      description: author.description || "",
      countryIsoCode: author.countryIsoCode || "",
      websiteUrls: author.websiteUrls || [],
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
      countryIsoCode: "",
      websiteUrls: [],
    });
    setSelectedAuthor(null);
  };

  const FormFields = () => (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="name">Имя *</Label>
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

      <div className="space-y-2">
        <Label htmlFor="country">Код страны</Label>
        <Input
          id="country"
          value={formData.countryIsoCode || ""}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, countryIsoCode: e.target.value }))
          }
          maxLength={2}
          placeholder="JP"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="websites">Сайты (через запятую)</Label>
        <Input
          id="websites"
          value={(formData.websiteUrls || []).join(", ")}
          onChange={(e) =>
            setFormData((prev) => ({
              ...prev,
              websiteUrls: e.target.value
                .split(",")
                .map((s) => s.trim())
                .filter(Boolean),
            }))
          }
          placeholder="https://twitter.com/author"
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
            <h1 className="text-3xl font-bold">Управление авторами</h1>
          </div>
          <Button onClick={openCreateDialog}>
            <Plus className="h-4 w-4 mr-2" />
            Добавить автора
          </Button>
        </div>

        {/* Search */}
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Поиск по имени..."
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
                <TableHead>Имя</TableHead>
                <TableHead>Страна</TableHead>
                <TableHead>Сайты</TableHead>
                <TableHead className="text-right">Действия</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                Array.from({ length: 10 }).map((_, i) => (
                  <TableRow key={i}>
                    <TableCell colSpan={4} className="h-12">
                      <div className="h-4 bg-muted rounded animate-pulse" />
                    </TableCell>
                  </TableRow>
                ))
              ) : authors.length > 0 ? (
                authors.map((author) => (
                  <TableRow key={author.id}>
                    <TableCell className="font-medium">{author.name}</TableCell>
                    <TableCell>{author.countryIsoCode || "-"}</TableCell>
                    <TableCell>
                      {author.websiteUrls?.length || 0} ссылок
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => openEditDialog(author)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-destructive"
                          onClick={() => handleDelete(author.id)}
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
                    Авторы не найдены
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
            <DialogTitle>Добавить автора</DialogTitle>
            <DialogDescription>
              Заполните информацию об авторе
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
            <DialogTitle>Редактировать автора</DialogTitle>
            <DialogDescription>Измените информацию об авторе</DialogDescription>
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
