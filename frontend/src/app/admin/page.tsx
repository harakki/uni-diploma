import Link from "next/link";
import {
  Book,
  Users,
  Building2,
  Tags,
  FileText,
  Image,
  LayoutDashboard,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const adminLinks = [
  {
    title: "Тайтлы",
    description: "Управление каталогом тайтлов",
    href: "/admin/titles",
    icon: Book,
  },
  {
    title: "Авторы",
    description: "Управление авторами и художниками",
    href: "/admin/authors",
    icon: Users,
  },
  {
    title: "Издатели",
    description: "Управление издателями",
    href: "/admin/publishers",
    icon: Building2,
  },
  {
    title: "Теги",
    description: "Управление тегами и жанрами",
    href: "/admin/tags",
    icon: Tags,
  },
  {
    title: "Главы",
    description: "Управление главами тайтлов",
    href: "/admin/chapters",
    icon: FileText,
  },
  {
    title: "Медиа",
    description: "Управление изображениями и файлами",
    href: "/admin/media",
    icon: Image,
  },
];

export default function AdminPage() {
  return (
    <div className="container py-6">
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <LayoutDashboard className="h-8 w-8 text-primary" />
            Панель администратора
          </h1>
          <p className="text-muted-foreground mt-2">
            Управляйте контентом сервиса MangaDex
          </p>
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {adminLinks.map((link) => (
            <Link key={link.href} href={link.href}>
              <Card className="hover:border-primary transition-colors h-full">
                <CardHeader>
                  <link.icon className="h-8 w-8 text-primary mb-2" />
                  <CardTitle>{link.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">{link.description}</p>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
