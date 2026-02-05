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
import { publishersApi } from "@/lib/api";
import {
  PublisherResponse,
  PublisherCreateRequest,
  PublisherUpdateRequest,
} from "@/types";

export default function AdminPublishersPage() {
  const [loading, setLoading] = useState(true);
  const [publishers, setPublishers] = useState<PublisherResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");

  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedPublisher, setSelectedPublisher] =
    useState<PublisherResponse | null>(null);

  const [formData, setFormData] = useState<PublisherCreateRequest>({
    name: "",
    description: "",
    websiteUrl: "",
    countryIsoCode: "",
  });

  useEffect(() => {
    fetchPublishers();
  }, [page, search]);

  const fetchPublishers = async () => {
    setLoading(true);
    try {
      const data = await publishersApi.getAll({
        page,
        size: 20,
        search: search || undefined,
      });
      setPublishers(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error("Failed to fetch publishers:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    try {
      await publishersApi.create(formData);
      setCreateDialogOpen(false);
      resetForm();
      fetchPublishers();
    } catch (error) {
      console.error("Failed to create publisher:", error);
      alert("Ошибка при создании издателя");
    }
  };

  const handleUpdate = async () => {
    if (!selectedPublisher) return;
    try {
      const updateData: PublisherUpdateRequest = {
        name: formData.name,
        description: formData.description,
        websiteUrl: formData.websiteUrl,
        countryIsoCode: formData.countryIsoCode,
      };
      await publishersApi.update(selectedPublisher.id, updateData);
      setEditDialogOpen(false);
      resetForm();
      fetchPublishers();
    } catch (error) {
      console.error("Failed to update publisher:", error);
      alert("Ошибка при обновлении издателя");
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Удалить этого издателя?")) return;
    try {
      await publishersApi.delete(id);
      fetchPublishers();
    } catch (error) {
      console.error("Failed to delete publisher:", error);
      alert("Ошибка при удалении издателя");
    }
  };

  const openEditDialog = (publisher: PublisherResponse) => {
    setSelectedPublisher(publisher);
    setFormData({
      name: publisher.name,
      description: publisher.description || "",
      websiteUrl: publisher.websiteUrl || "",
      countryIsoCode: publisher.countryIsoCode || "",
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
      websiteUrl: "",
      countryIsoCode: "",
    });
    setSelectedPublisher(null);
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
        <Label htmlFor="website">Сайт</Label>
        <Input
          id="website"
          value={formData.websiteUrl || ""}
          onChange={(e) =>
            setFormData((prev) => ({ ...prev, websiteUrl: e.target.value }))
          }
          placeholder="https://publisher.com"
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
            <h1 className="text-3xl font-bold">Управление издателями</h1>
          </div>
          <Button onClick={openCreateDialog}>
            <Plus className="h-4 w-4 mr-2" />
            Добавить издателя
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
                <TableHead>Страна</TableHead>
                <TableHead>Сайт</TableHead>
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
              ) : publishers.length > 0 ? (
                publishers.map((publisher) => (
                  <TableRow key={publisher.id}>
                    <TableCell className="font-medium">
                      {publisher.name}
                    </TableCell>
                    <TableCell>{publisher.countryIsoCode || "-"}</TableCell>
                    <TableCell>
                      {publisher.websiteUrl ? (
                        <a
                          href={publisher.websiteUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-primary hover:underline"
                        >
                          Ссылка
                        </a>
                      ) : (
                        "-"
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => openEditDialog(publisher)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-destructive"
                          onClick={() => handleDelete(publisher.id)}
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
                    Издатели не найдены
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
            <DialogTitle>Добавить издателя</DialogTitle>
            <DialogDescription>
              Заполните информацию об издателе
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
            <DialogTitle>Редактировать издателя</DialogTitle>
            <DialogDescription>
              Измените информацию об издателе
            </DialogDescription>
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
