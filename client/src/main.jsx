import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import "./index.css";
import { appConfig, requireConfigValue } from "./config/appConfig.js";

const root = createRoot(document.getElementById("root"));
const publishableKey = requireConfigValue("VITE_CLERK_PUBLISHABLE_KEY", appConfig.clerkPublishableKey);

async function bootstrap() {
  await import("./browserCompat.js");

  const [{ ClerkProvider }, { default: App }] = await Promise.all([
    import("@clerk/clerk-react"),
    import("./App.jsx"),
  ]);

  root.render(
    <BrowserRouter>
      <ClerkProvider
        publishableKey={publishableKey}
        clerkJSVersion={appConfig.clerkJsVersion}
      >
        <App />
      </ClerkProvider>
    </BrowserRouter>,
  );
}

bootstrap().catch((error) => {
  console.error("Failed to bootstrap the application.", error);
  throw error;
});
