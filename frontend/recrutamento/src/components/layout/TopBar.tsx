import { useNavigate } from "react-router-dom";
import { User, Building2, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { authService } from "@/services/auth.service";
import { getOrganizacaoNomeFromToken } from "@/lib/jwt";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface TopBarProps {
  title?: string;
  onMenuClick?: () => void;
}

export default function TopBar({ title, onMenuClick }: TopBarProps) {
  const navigate = useNavigate();
  const user = authService.getUser();
  const isRecrutador =
    user?.roles?.includes("ROLE_RECRUTADOR") ||
    user?.roles?.includes("ROLE_ADMIN");
  const isAdmin = user?.roles?.includes("ROLE_ADMIN");
  const organizacaoNome = getOrganizacaoNomeFromToken() || "Empresa";

  const handleLogout = () => {
    authService.logout();
  };

  return (
    <header className="h-16 border-b border-blue-100 bg-white/80 backdrop-blur-sm flex items-center justify-between px-4 lg:px-6 shadow-sm">
      {/* Left side - Menu button (mobile) and Title */}
      <div className="flex items-center gap-3 flex-1 min-w-0">
        {/* Hamburger Menu - Only visible on mobile */}
        <Button
          variant="ghost"
          size="icon"
          className="lg:hidden"
          onClick={onMenuClick}
        >
          <Menu className="w-5 h-5" />
        </Button>

        {/* Title */}
        {title && (
          <h1 className="text-lg sm:text-xl font-semibold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent truncate">
            {title}
          </h1>
        )}
      </div>

      {/* Right side - Organization/Role Info, Notifications and User */}
      <div className="flex items-center gap-2 sm:gap-4">
        {/* Organization Info for Recruiters */}
        {isRecrutador && (
          <div className="hidden md:flex items-center gap-2 px-3 py-1.5 bg-gradient-to-r from-blue-50 to-purple-50 border border-blue-200 rounded-lg">
            <Building2 className="w-4 h-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-900 truncate max-w-[150px]">{organizacaoNome}</span>
          </div>
        )}

        {/* User Menu */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="rounded-full">
              <User className="w-5 h-5" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuLabel>
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium truncate">{user?.nome || "Usuário"}</p>
                <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
                {isRecrutador && (
                  <Badge variant="secondary" className="w-fit mt-1">
                    {user?.roles?.includes("ROLE_ADMIN")
                      ? "Admin"
                      : "Recrutador"}
                  </Badge>
                )}
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            {isAdmin && (
              <DropdownMenuItem
                onClick={() => navigate("/dashboard/gerenciar-usuarios")}
              >
                Gerenciar Usuários
              </DropdownMenuItem>
            )}
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={handleLogout}
              className="text-destructive"
            >
              Sair
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
