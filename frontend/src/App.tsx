import { ToastProvider } from "./components/ui/ToastProvider";
import { AppRouter } from "./router/AppRouter";

export default function App() {
  return (
    <ToastProvider>
      <AppRouter />
    </ToastProvider>
  );
}
