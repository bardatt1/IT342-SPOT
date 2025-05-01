import React, { createContext, useContext, useState } from "react";
import { cn } from "../../lib/utils";
import { Button } from "./button";
import { Menu } from "lucide-react";

// Create context for sidebar state
interface SidebarContextProps {
  isOpen: boolean;
  setIsOpen: React.Dispatch<React.SetStateAction<boolean>>;
  isMobile: boolean;
}

const SidebarContext = createContext<SidebarContextProps | undefined>(undefined);

export function useSidebar() {
  const context = useContext(SidebarContext);
  if (!context) {
    throw new Error("useSidebar must be used within a SidebarProvider");
  }
  return context;
}

interface SidebarProviderProps {
  children: React.ReactNode;
  defaultOpen?: boolean;
}

export function SidebarProvider({
  children,
  defaultOpen = true,
}: SidebarProviderProps) {
  const [isOpen, setIsOpen] = useState(defaultOpen);
  const [isMobile, setIsMobile] = useState(false);

  // Check if we're on mobile viewport
  React.useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 1024);
      if (window.innerWidth >= 1024) {
        setIsOpen(true);
      } else {
        setIsOpen(false);
      }
    };

    // Set initial state
    handleResize();

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return (
    <SidebarContext.Provider value={{ isOpen, setIsOpen, isMobile }}>
      {children}
    </SidebarContext.Provider>
  );
}

interface SidebarProps {
  children?: React.ReactNode;
  className?: string;
}

export function Sidebar({ children, className }: SidebarProps) {
  const { isOpen, isMobile } = useSidebar();

  return (
    <>
      {/* Mobile overlay */}
      {isMobile && isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50"
          onClick={() => useSidebar().setIsOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 flex h-full w-64 flex-col border-r bg-background transition-transform lg:static lg:z-auto",
          isOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0",
          className
        )}
      >
        {children}
      </aside>
    </>
  );
}

interface SidebarHeaderProps {
  children?: React.ReactNode;
  className?: string;
}

export function SidebarHeader({ children, className }: SidebarHeaderProps) {
  return (
    <div
      className={cn(
        "flex h-14 items-center border-b px-4",
        className
      )}
    >
      {children}
    </div>
  );
}

interface SidebarContentProps {
  children?: React.ReactNode;
  className?: string;
}

export function SidebarContent({ children, className }: SidebarContentProps) {
  return (
    <div className={cn("flex-1 overflow-auto p-4", className)}>
      {children}
    </div>
  );
}

interface SidebarFooterProps {
  children?: React.ReactNode;
  className?: string;
}

export function SidebarFooter({ children, className }: SidebarFooterProps) {
  return (
    <div className={cn("mt-auto", className)}>
      {children}
    </div>
  );
}

interface SidebarMenuProps {
  children?: React.ReactNode;
  className?: string;
}

export function SidebarMenu({ children, className }: SidebarMenuProps) {
  return (
    <nav className={cn("space-y-1", className)}>
      {children}
    </nav>
  );
}

interface SidebarMenuItemProps {
  children?: React.ReactNode;
  className?: string;
}

export function SidebarMenuItem({ children, className }: SidebarMenuItemProps) {
  return (
    <div className={cn("", className)}>
      {children}
    </div>
  );
}

interface SidebarMenuButtonProps {
  children?: React.ReactNode;
  className?: string;
  isActive?: boolean;
  tooltip?: string;
  asChild?: boolean;
}

export function SidebarMenuButton({
  children,
  className,
  isActive,
  tooltip,
  asChild = false,
}: SidebarMenuButtonProps) {
  const Comp = asChild ? React.Fragment : "button";
  const props = asChild ? {} : { type: "button" as "button" };

  return (
    <Comp {...props}>
      <div
        className={cn(
          "group flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
          isActive
            ? "bg-accent text-accent-foreground"
            : "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
          className
        )}
        title={tooltip}
      >
        {children}
      </div>
    </Comp>
  );
}

interface SidebarTriggerProps {
  className?: string;
}

export function SidebarTrigger({ className }: SidebarTriggerProps) {
  const { setIsOpen } = useSidebar();

  return (
    <Button
      variant="ghost"
      size="icon"
      className={cn("lg:hidden", className)}
      onClick={() => setIsOpen((open) => !open)}
    >
      <Menu className="h-5 w-5" />
      <span className="sr-only">Toggle sidebar</span>
    </Button>
  );
}

interface SidebarInsetProps {
  children?: React.ReactNode;
  className?: string;
}

export function SidebarInset({ children, className }: SidebarInsetProps) {
  return (
    <div className={cn("flex flex-1 flex-col overflow-hidden", className)}>
      {children}
    </div>
  );
}
