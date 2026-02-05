import Link from "next/link";
import { BookOpen } from "lucide-react";

export function Footer() {
  return (
    <footer className="border-t">
      <div className="container flex flex-col items-center justify-between gap-4 py-10 md:h-24 md:flex-row md:py-0">
        <div className="flex flex-col items-center gap-4 px-8 md:flex-row md:gap-2 md:px-0">
          <BookOpen className="h-6 w-6 text-primary" />
          <p className="text-center text-sm leading-loose md:text-left">
            MangaDex &copy; {new Date().getFullYear()}. Все права защищены.
          </p>
        </div>
        <div className="flex gap-4">
          <Link
            href="/search"
            className="text-sm text-muted-foreground hover:text-foreground"
          >
            Каталог
          </Link>
          <Link
            href="/library"
            className="text-sm text-muted-foreground hover:text-foreground"
          >
            Библиотека
          </Link>
          <Link
            href="/collections"
            className="text-sm text-muted-foreground hover:text-foreground"
          >
            Коллекции
          </Link>
        </div>
      </div>
    </footer>
  );
}
