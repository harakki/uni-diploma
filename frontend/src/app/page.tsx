import Link from "next/link";
import { ArrowRight, BookOpen, Search, TrendingUp } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function Home() {
  return (
    <div className="container py-6">
      {/* Hero Section */}
      <section className="py-12 md:py-24 lg:py-32 text-center">
        <div className="flex flex-col items-center space-y-4">
          <div className="flex items-center justify-center w-16 h-16 rounded-full bg-primary/10 mb-4">
            <BookOpen className="h-8 w-8 text-primary" />
          </div>
          <h1 className="text-3xl font-bold tracking-tighter sm:text-5xl xl:text-6xl/none">
            Добро пожаловать в MangaDex
          </h1>
          <p className="max-w-[600px] text-muted-foreground md:text-xl">
            Читайте мангу, манхву и комиксы онлайн. Персональные рекомендации,
            коллекции и история чтения.
          </p>
          <div className="flex flex-col gap-2 min-[400px]:flex-row mt-4">
            <Button asChild size="lg">
              <Link href="/search">
                <Search className="mr-2 h-4 w-4" />
                Искать тайтлы
              </Link>
            </Button>
            <Button asChild variant="outline" size="lg">
              <Link href="/library">
                Моя библиотека
              </Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-12">
        <h2 className="text-2xl font-bold text-center mb-8">Возможности</h2>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader>
              <TrendingUp className="h-8 w-8 text-primary mb-2" />
              <CardTitle>Персональные рекомендации</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                Получайте рекомендации на основе ваших предпочтений и истории
                чтения.
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <BookOpen className="h-8 w-8 text-primary mb-2" />
              <CardTitle>Удобное чтение</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                Продолжайте чтение с места остановки. Все ваши закладки
                синхронизированы.
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <Search className="h-8 w-8 text-primary mb-2" />
              <CardTitle>Расширенный поиск</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                Фильтрация по жанрам, тегам, статусу и многому другому.
              </p>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Quick Links */}
      <section className="py-12">
        <div className="flex flex-col md:flex-row gap-4 justify-center">
          <Button asChild variant="outline" size="lg">
            <Link href="/search?type=MANGA">
              Манга
              <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link href="/search?type=MANHWA">
              Манхва
              <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link href="/search?type=MANHUA">
              Маньхуа
              <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link href="/search?type=COMIC">
              Комиксы
              <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
        </div>
      </section>
    </div>
  );
}
