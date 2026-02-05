"use client";

import { useState, useRef } from "react";
import Link from "next/link";
import { ArrowLeft, Upload, Copy, Check, Image as ImageIcon, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { mediaApi } from "@/lib/api";

interface UploadedMedia {
  mediaId: string;
  filename: string;
  url?: string;
}

export default function AdminMediaPage() {
  const [uploading, setUploading] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState<UploadedMedia[]>([]);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setUploading(true);
    const newUploads: UploadedMedia[] = [];

    for (const file of Array.from(files)) {
      try {
        // Get presigned upload URL
        const { mediaId, uploadUrl } = await mediaApi.getUploadUrl({
          filename: file.name,
          contentType: file.type,
        });

        // Upload file to the presigned URL
        await fetch(uploadUrl, {
          method: "PUT",
          body: file,
          headers: {
            "Content-Type": file.type,
          },
        });

        // Get the public URL
        const { url } = await mediaApi.getMediaUrl(mediaId);

        newUploads.push({
          mediaId,
          filename: file.name,
          url,
        });
      } catch (error) {
        console.error(`Failed to upload ${file.name}:`, error);
        alert(`Ошибка при загрузке ${file.name}`);
      }
    }

    setUploadedFiles((prev) => [...newUploads, ...prev]);
    setUploading(false);

    // Reset file input
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleCopyId = async (mediaId: string) => {
    await navigator.clipboard.writeText(mediaId);
    setCopiedId(mediaId);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleDelete = async (mediaId: string) => {
    if (!confirm("Удалить этот файл?")) return;
    try {
      await mediaApi.delete(mediaId);
      setUploadedFiles((prev) => prev.filter((f) => f.mediaId !== mediaId));
    } catch (error) {
      console.error("Failed to delete media:", error);
      alert("Ошибка при удалении файла");
    }
  };

  return (
    <div className="container py-6">
      <div className="flex flex-col gap-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href="/admin">
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <h1 className="text-3xl font-bold">Управление медиа</h1>
        </div>

        {/* Upload Section */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Upload className="h-5 w-5" />
              Загрузка файлов
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="file-upload">
                  Выберите изображения для загрузки
                </Label>
                <Input
                  id="file-upload"
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  multiple
                  onChange={handleFileSelect}
                  disabled={uploading}
                />
              </div>
              {uploading && (
                <div className="flex items-center gap-2 text-muted-foreground">
                  <div className="animate-spin h-4 w-4 border-2 border-primary border-t-transparent rounded-full" />
                  Загрузка...
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Uploaded Files */}
        {uploadedFiles.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>Загруженные файлы ({uploadedFiles.length})</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {uploadedFiles.map((file) => (
                  <div
                    key={file.mediaId}
                    className="border rounded-lg p-4 space-y-3"
                  >
                    {/* Preview */}
                    <div className="relative aspect-video bg-muted rounded overflow-hidden">
                      {file.url ? (
                        <img
                          src={file.url}
                          alt={file.filename}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="flex items-center justify-center h-full">
                          <ImageIcon className="h-8 w-8 text-muted-foreground" />
                        </div>
                      )}
                    </div>

                    {/* Info */}
                    <div className="space-y-2">
                      <p className="text-sm font-medium truncate">
                        {file.filename}
                      </p>
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          className="flex-1"
                          onClick={() => handleCopyId(file.mediaId)}
                        >
                          {copiedId === file.mediaId ? (
                            <>
                              <Check className="h-4 w-4 mr-1" />
                              Скопировано
                            </>
                          ) : (
                            <>
                              <Copy className="h-4 w-4 mr-1" />
                              ID
                            </>
                          )}
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          className="text-destructive"
                          onClick={() => handleDelete(file.mediaId)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                      <p className="text-xs text-muted-foreground font-mono break-all">
                        {file.mediaId}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Instructions */}
        <Card>
          <CardHeader>
            <CardTitle>Инструкция</CardTitle>
          </CardHeader>
          <CardContent className="prose prose-sm dark:prose-invert">
            <ol className="list-decimal list-inside space-y-2 text-muted-foreground">
              <li>Загрузите изображения через форму выше</li>
              <li>После загрузки скопируйте ID медиафайла</li>
              <li>
                Используйте ID при создании/редактировании тайтлов, авторов или
                страниц глав
              </li>
              <li>Изображения хранятся в MinIO и доступны по прямой ссылке</li>
            </ol>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
