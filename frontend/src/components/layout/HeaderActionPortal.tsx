import { type ReactNode, useEffect, useState } from "react";
import { createPortal } from "react-dom";

interface HeaderActionPortalProps {
  children: ReactNode;
}

export function HeaderActionPortal({ children }: HeaderActionPortalProps) {
  const [target, setTarget] = useState<HTMLElement | null>(null);

  useEffect(() => {
    setTarget(document.getElementById("header-action-root"));
  }, []);

  if (!target) {
    return null;
  }

  return createPortal(children, target);
}
