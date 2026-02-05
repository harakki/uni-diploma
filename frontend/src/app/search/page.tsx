"use client";

import { useState, useEffect, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { Search, Filter, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { TitleCard, TitleCardSkeleton } from "@/components/title-card";
import { titlesApi, tagsApi } from "@/lib/api";
import {
  TitleResponse,
  TagResponse,
} from "@/types";

const typeOptions = [
  { value: "MANGA", label: "Манга" },
  { value: "MANHWA", label: "Манхва" },
  { value: "MANHUA", label: "Маньхуа" },
  { value: "COMIC", label: "Комикс" },
  { value: "ARTBOOK", label: "Артбук" },
  { value: "NOVEL", label: "Новелла" },
];

const statusOptions = [
  { value: "ONGOING", label: "Выходит" },
  { value: "COMPLETED", label: "Завершен" },
  { value: "ANNOUNCED", label: "Анонс" },
  { value: "SUSPENDED", label: "Приостановлен" },
  { value: "DISCONTINUED", label: "Прекращен" },
];

const sortOptions = [
  { value: "name,asc", label: "По названию (А-Я)" },
  { value: "name,desc", label: "По названию (Я-А)" },
  { value: "releaseYear,desc", label: "По году (новые)" },
  { value: "releaseYear,asc", label: "По году (старые)" },
];

function SearchContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [loading, setLoading] = useState(true);
  const [titles, setTitles] = useState<TitleResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filter state
  const [search, setSearch] = useState(searchParams.get("search") || "");
  const [selectedTypes, setSelectedTypes] = useState<string[]>(
    searchParams.getAll("type")
  );
  const [selectedStatuses, setSelectedStatuses] = useState<string[]>(
    searchParams.getAll("titleStatus")
  );
  const [selectedTags, setSelectedTags] = useState<string[]>(
    searchParams.getAll("tags")
  );
  const [yearFrom, setYearFrom] = useState(searchParams.get("yearFrom") || "");
  const [yearTo, setYearTo] = useState(searchParams.get("yearTo") || "");
  const [sort, setSort] = useState(searchParams.get("sort") || "name,asc");
  const [page, setPage] = useState(Number(searchParams.get("page")) || 0);

  const [availableTags, setAvailableTags] = useState<TagResponse[]>([]);
  const [showFilters, setShowFilters] = useState(false);

  // Load tags
  useEffect(() => {
    tagsApi.getAll({ size: 100 }).then((data) => {
      setAvailableTags(data.content);
    }).catch(console.error);
  }, []);

  // Fetch titles
  useEffect(() => {
    const fetchTitles = async () => {
      setLoading(true);
      try {
        const params: Record<string, string | string[] | number | undefined> = {
          page,
          size: 20,
          sort,
        };
        if (search) params.search = search;
        if (selectedTypes.length > 0) params.type = selectedTypes;
        if (selectedStatuses.length > 0) params.titleStatus = selectedStatuses;
        if (selectedTags.length > 0) params.tags = selectedTags;
        if (yearFrom) params.yearFrom = Number(yearFrom);
        if (yearTo) params.yearTo = Number(yearTo);

        const data = await titlesApi.getAll(params);
        setTitles(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } catch (error) {
        console.error("Failed to fetch titles:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchTitles();
  }, [search, selectedTypes, selectedStatuses, selectedTags, yearFrom, yearTo, sort, page]);

  // Update URL
  const updateUrl = () => {
    const params = new URLSearchParams();
    if (search) params.set("search", search);
    selectedTypes.forEach((t) => params.append("type", t));
    selectedStatuses.forEach((s) => params.append("titleStatus", s));
    selectedTags.forEach((t) => params.append("tags", t));
    if (yearFrom) params.set("yearFrom", yearFrom);
    if (yearTo) params.set("yearTo", yearTo);
    if (sort !== "name,asc") params.set("sort", sort);
    if (page > 0) params.set("page", String(page));

    router.push(`/search?${params.toString()}`);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    updateUrl();
  };

  const toggleType = (type: string) => {
    setSelectedTypes((prev) =>
      prev.includes(type) ? prev.filter((t) => t !== type) : [...prev, type]
    );
    setPage(0);
  };

  const toggleStatus = (status: string) => {
    setSelectedStatuses((prev) =>
      prev.includes(status)
        ? prev.filter((s) => s !== status)
        : [...prev, status]
    );
    setPage(0);
  };

  const toggleTag = (tag: string) => {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
    setPage(0);
  };

  const clearFilters = () => {
    setSearch("");
    setSelectedTypes([]);
    setSelectedStatuses([]);
    setSelectedTags([]);
    setYearFrom("");
    setYearTo("");
    setSort("name,asc");
    setPage(0);
    router.push("/search");
  };

  const hasActiveFilters =
    search ||
    selectedTypes.length > 0 ||
    selectedStatuses.length > 0 ||
    selectedTags.length > 0 ||
    yearFrom ||
    yearTo;

  return (
    <div className="container py-6">
      <div className="flex flex-col gap-6">
        {/* Search Header */}
        <div className="flex flex-col gap-4">
          <h1 className="text-3xl font-bold">Каталог</h1>

          <form onSubmit={handleSearch} className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Поиск по названию..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button type="submit">Найти</Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => setShowFilters(!showFilters)}
            >
              <Filter className="h-4 w-4 mr-2" />
              Фильтры
            </Button>
          </form>

          {/* Active Filters */}
          {hasActiveFilters && (
            <div className="flex flex-wrap gap-2 items-center">
              <span className="text-sm text-muted-foreground">Фильтры:</span>
              {selectedTypes.map((type) => (
                <Badge
                  key={type}
                  variant="secondary"
                  className="cursor-pointer"
                  onClick={() => toggleType(type)}
                >
                  {typeOptions.find((t) => t.value === type)?.label}
                  <X className="h-3 w-3 ml-1" />
                </Badge>
              ))}
              {selectedStatuses.map((status) => (
                <Badge
                  key={status}
                  variant="secondary"
                  className="cursor-pointer"
                  onClick={() => toggleStatus(status)}
                >
                  {statusOptions.find((s) => s.value === status)?.label}
                  <X className="h-3 w-3 ml-1" />
                </Badge>
              ))}
              {selectedTags.map((tag) => (
                <Badge
                  key={tag}
                  variant="secondary"
                  className="cursor-pointer"
                  onClick={() => toggleTag(tag)}
                >
                  {availableTags.find((t) => t.slug === tag)?.name || tag}
                  <X className="h-3 w-3 ml-1" />
                </Badge>
              ))}
              {yearFrom && (
                <Badge variant="secondary">От {yearFrom}</Badge>
              )}
              {yearTo && (
                <Badge variant="secondary">До {yearTo}</Badge>
              )}
              <Button
                variant="ghost"
                size="sm"
                onClick={clearFilters}
                className="text-destructive"
              >
                Сбросить все
              </Button>
            </div>
          )}
        </div>

        {/* Filters Panel */}
        {showFilters && (
          <div className="border rounded-lg p-4 space-y-4">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
              {/* Type Filter */}
              <div className="space-y-2">
                <Label>Тип</Label>
                <div className="space-y-2">
                  {typeOptions.map((option) => (
                    <div key={option.value} className="flex items-center space-x-2">
                      <Checkbox
                        id={`type-${option.value}`}
                        checked={selectedTypes.includes(option.value)}
                        onCheckedChange={() => toggleType(option.value)}
                      />
                      <label
                        htmlFor={`type-${option.value}`}
                        className="text-sm cursor-pointer"
                      >
                        {option.label}
                      </label>
                    </div>
                  ))}
                </div>
              </div>

              {/* Status Filter */}
              <div className="space-y-2">
                <Label>Статус</Label>
                <div className="space-y-2">
                  {statusOptions.map((option) => (
                    <div key={option.value} className="flex items-center space-x-2">
                      <Checkbox
                        id={`status-${option.value}`}
                        checked={selectedStatuses.includes(option.value)}
                        onCheckedChange={() => toggleStatus(option.value)}
                      />
                      <label
                        htmlFor={`status-${option.value}`}
                        className="text-sm cursor-pointer"
                      >
                        {option.label}
                      </label>
                    </div>
                  ))}
                </div>
              </div>

              {/* Year Filter */}
              <div className="space-y-2">
                <Label>Год выпуска</Label>
                <div className="flex gap-2">
                  <Input
                    type="number"
                    placeholder="От"
                    value={yearFrom}
                    onChange={(e) => setYearFrom(e.target.value)}
                    className="w-24"
                  />
                  <Input
                    type="number"
                    placeholder="До"
                    value={yearTo}
                    onChange={(e) => setYearTo(e.target.value)}
                    className="w-24"
                  />
                </div>
              </div>

              {/* Sort */}
              <div className="space-y-2">
                <Label>Сортировка</Label>
                <Select value={sort} onValueChange={(v) => { setSort(v); setPage(0); }}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {sortOptions.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Tags */}
            {availableTags.length > 0 && (
              <div className="space-y-2">
                <Label>Теги</Label>
                <div className="flex flex-wrap gap-2">
                  {availableTags.map((tag) => (
                    <Badge
                      key={tag.id}
                      variant={selectedTags.includes(tag.slug) ? "default" : "outline"}
                      className="cursor-pointer"
                      onClick={() => toggleTag(tag.slug)}
                    >
                      {tag.name}
                    </Badge>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* Results */}
        <div className="flex items-center justify-between">
          <p className="text-sm text-muted-foreground">
            Найдено: {totalElements} тайтлов
          </p>
        </div>

        {/* Titles Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          {loading
            ? Array.from({ length: 20 }).map((_, i) => (
                <TitleCardSkeleton key={i} />
              ))
            : titles.map((title) => <TitleCard key={title.id} title={title} />)}
        </div>

        {!loading && titles.length === 0 && (
          <div className="text-center py-12">
            <p className="text-muted-foreground">Тайтлы не найдены</p>
          </div>
        )}

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
            <div className="flex items-center gap-2">
              <span className="text-sm">
                Страница {page + 1} из {totalPages}
              </span>
            </div>
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
    </div>
  );
}

function SearchFallback() {
  return (
    <div className="container py-6">
      <div className="flex flex-col gap-6">
        <div className="h-10 w-32 bg-muted rounded animate-pulse" />
        <div className="h-10 w-full bg-muted rounded animate-pulse" />
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          {Array.from({ length: 20 }).map((_, i) => (
            <TitleCardSkeleton key={i} />
          ))}
        </div>
      </div>
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={<SearchFallback />}>
      <SearchContent />
    </Suspense>
  );
}
