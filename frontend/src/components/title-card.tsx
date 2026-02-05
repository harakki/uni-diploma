import Image from "next/image";
import Link from "next/link";
import { TitleResponse, TitleStatus, TitleType, ContentRating } from "@/types";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { getMediaUrl } from "@/lib/api";

const typeLabels: Record<TitleType, string> = {
  [TitleType.MANGA]: "Манга",
  [TitleType.MANHWA]: "Манхва",
  [TitleType.MANHUA]: "Маньхуа",
  [TitleType.COMIC]: "Комикс",
  [TitleType.ARTBOOK]: "Артбук",
  [TitleType.NOVEL]: "Новелла",
};

const statusLabels: Record<TitleStatus, string> = {
  [TitleStatus.ONGOING]: "Выходит",
  [TitleStatus.COMPLETED]: "Завершен",
  [TitleStatus.ANNOUNCED]: "Анонс",
  [TitleStatus.SUSPENDED]: "Приостановлен",
  [TitleStatus.DISCONTINUED]: "Прекращен",
};

const ratingLabels: Record<ContentRating, string> = {
  [ContentRating.SIX_PLUS]: "6+",
  [ContentRating.TWELVE_PLUS]: "12+",
  [ContentRating.SIXTEEN_PLUS]: "16+",
  [ContentRating.EIGHTEEN_PLUS]: "18+",
};

interface TitleCardProps {
  title: TitleResponse;
}

export function TitleCard({ title }: TitleCardProps) {
  const coverUrl = getMediaUrl(title.mainCoverMediaId);

  return (
    <Link href={`/titles/${title.slug}`}>
      <Card className="group overflow-hidden hover:shadow-lg transition-shadow">
        <div className="relative aspect-[3/4] overflow-hidden">
          {coverUrl ? (
            <Image
              src={coverUrl}
              alt={title.name}
              fill
              className="object-cover transition-transform group-hover:scale-105"
              sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 20vw"
            />
          ) : (
            <div className="w-full h-full bg-muted flex items-center justify-center">
              <span className="text-muted-foreground text-sm">Нет обложки</span>
            </div>
          )}
          <div className="absolute top-2 left-2 flex flex-col gap-1">
            <Badge variant="secondary" className="text-xs">
              {typeLabels[title.type]}
            </Badge>
            {title.contentRating === ContentRating.EIGHTEEN_PLUS && (
              <Badge variant="destructive" className="text-xs">
                18+
              </Badge>
            )}
          </div>
        </div>
        <CardContent className="p-3">
          <h3 className="font-medium text-sm line-clamp-2 group-hover:text-primary transition-colors">
            {title.name}
          </h3>
          <div className="mt-1 flex items-center gap-2 text-xs text-muted-foreground">
            <span>{statusLabels[title.titleStatus]}</span>
            {title.releaseYear && <span>• {title.releaseYear}</span>}
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}

export function TitleCardSkeleton() {
  return (
    <Card className="overflow-hidden">
      <div className="relative aspect-[3/4] bg-muted animate-pulse" />
      <CardContent className="p-3">
        <div className="h-4 bg-muted rounded animate-pulse" />
        <div className="mt-2 h-3 w-2/3 bg-muted rounded animate-pulse" />
      </CardContent>
    </Card>
  );
}
