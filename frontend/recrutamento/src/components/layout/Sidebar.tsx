import { Link, useLocation } from "react-router-dom";
import { cn } from "@/lib/utils";
import {
  Home,
  Search,
  Briefcase,
  Users,
  FileText,
  UserSearch,
  Gift,
  GraduationCap,
  Bookmark,
  User,
} from "lucide-react";
import { authService } from "@/services/auth.service";
import { Sheet, SheetContent } from "@/components/ui/sheet";

const candidatoMenuItems = [
  { path: "/dashboard", label: "Página Inicial", icon: Home },
  { path: "/dashboard/perfil", label: "Meu Perfil", icon: User },
  { path: "/dashboard/buscar-vagas", label: "Buscar Vagas", icon: Search },
  { path: "/dashboard/vagas-salvas", label: "Vagas Salvas", icon: Bookmark },
  {
    path: "/dashboard/vagas-externas",
    label: "Vagas Externas",
    icon: FileText,
  },
  {
    path: "/dashboard/minhas-candidaturas",
    label: "Minhas Candidaturas",
    icon: Users,
  }
];

const recrutadorMenuItems = [
  { path: "/dashboard", label: "Página Inicial", icon: Home },
  {
    path: "/dashboard/buscar-candidatos",
    label: "Buscar Candidatos",
    icon: UserSearch,
  },
  {
    path: "/dashboard/minhas-vagas",
    label: "Minhas Vagas",
    icon: Briefcase,
  }
];

interface SidebarProps {
  isOpen?: boolean;
  onClose?: () => void;
}

function SidebarContent({ onItemClick }: { onItemClick?: () => void }) {
  const location = useLocation();
  const user = authService.getUser();
  const isRecrutador =
    user?.roles?.includes("ROLE_RECRUTADOR") ||
    user?.roles?.includes("ROLE_ADMIN");
  const isAdmin = user?.roles?.includes("ROLE_ADMIN");

  const menuItems = isRecrutador ? recrutadorMenuItems : candidatoMenuItems;

  return (
    <>
      {/* Logo and Organization */}
      <div className="p-4 sm:p-6 border-b border-gray-200 bg-white space-y-3">
        <div className="flex items-center gap-2">
          <div className="w-10 h-10 sm:w-12 sm:h-12 bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg flex items-center justify-center shadow-md">
            <Briefcase className="w-6 h-6 text-white" />
          </div>
          <div className="text-lg sm:text-xl font-bold bg-gradient-to-r from-blue-500 to-blue-600 bg-clip-text text-transparent">
            RECRUTAMENTO
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-3 sm:p-4 overflow-y-auto">
        <ul className="space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;

            return (
              <li key={item.path}>
                <Link
                  to={item.path}
                  onClick={onItemClick}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2.5 sm:px-4 sm:py-3 rounded-lg text-sm font-medium transition-all",
                    isActive
                      ? "bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md shadow-blue-200"
                      : "text-gray-700 hover:bg-blue-50 hover:text-blue-700 hover:shadow-sm"
                  )}
                >
                  <Icon className="w-5 h-5 flex-shrink-0" />
                  <span className="break-words">{item.label}</span>
                </Link>
              </li>
            );
          })}
          {/* Admin-only menu items */}
          {isAdmin && (
            <>
              <li>
                <Link
                  to="/dashboard/gerenciar-beneficios"
                  onClick={onItemClick}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2.5 sm:px-4 sm:py-3 rounded-lg text-sm font-medium transition-all",
                    location.pathname === "/dashboard/gerenciar-beneficios"
                      ? "bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md shadow-blue-200"
                      : "text-gray-700 hover:bg-blue-50 hover:text-blue-700 hover:shadow-sm"
                  )}
                >
                  <Gift className="w-5 h-5 flex-shrink-0" />
                  <span className="break-words">Gerenciar Benefícios</span>
                </Link>
              </li>
              <li>
                <Link
                  to="/dashboard/gerenciar-niveis-experiencia"
                  onClick={onItemClick}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2.5 sm:px-4 sm:py-3 rounded-lg text-sm font-medium transition-all",
                    location.pathname ===
                      "/dashboard/gerenciar-niveis-experiencia"
                      ? "bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md shadow-blue-200"
                      : "text-gray-700 hover:bg-blue-50 hover:text-blue-700 hover:shadow-sm"
                  )}
                >
                  <GraduationCap className="w-5 h-5 flex-shrink-0" />
                  <span className="break-words">Níveis de Experiência</span>
                </Link>
              </li>
            </>
          )}
        </ul>
      </nav>
    </>
  );
}

export default function Sidebar({ isOpen, onClose }: SidebarProps) {
  return (
    <>
      {/* Desktop Sidebar - Fixed, always visible on large screens */}
      <aside className="hidden lg:flex w-64 bg-white border-r border-gray-200 min-h-screen flex-col shadow-sm">
        <SidebarContent />
      </aside>

      {/* Mobile Sidebar - Drawer */}
      <Sheet open={isOpen} onOpenChange={onClose}>
        <SheetContent side="left" className="w-72 p-0 bg-white">
          <div className="flex flex-col h-full">
            <SidebarContent onItemClick={onClose} />
          </div>
        </SheetContent>
      </Sheet>
    </>
  );
}
