/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_ENV: string;
  readonly VITE_API_URL: string;
  // Add more environment variables as needed
}

// Extends the existing ImportMeta interface
interface ImportMeta {
  readonly env: ImportMetaEnv;
}
