/**
 * Environment configuration
 * Centralizes access to environment variables across the application
 */

interface EnvConfig {
  env: string;
  apiUrl: string;
  isProduction: boolean;
  isDevelopment: boolean;
  isTesting: boolean;
}

// Get environment variables with appropriate defaults
export const env: EnvConfig = {
  env: import.meta.env.VITE_APP_ENV || 'development',
  apiUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  get isProduction() {
    return this.env === 'production';
  },
  get isDevelopment() {
    return this.env === 'development';
  },
  get isTesting() {
    return this.env === 'testing';
  }
};

export default env;
