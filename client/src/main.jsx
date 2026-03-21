import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {BrowserRouter} from "react-router-dom";
import {ClerkProvider} from "@clerk/clerk-react";
import {appConfig, requireConfigValue} from "./config/appConfig.js";

const publishableKey = requireConfigValue("VITE_CLERK_PUBLISHABLE_KEY", appConfig.clerkPublishableKey);

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <ClerkProvider publishableKey={publishableKey}>
        <App />
    </ClerkProvider>
  </BrowserRouter>,
)
