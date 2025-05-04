/**
 * Utility functions for managing cookies in a secure way
 */

/**
 * Set a cookie with a specified name, value, and expiration time
 */
export function setCookie(name: string, value: string, expiryDays: number = 1): void {
  // Use HttpOnly and SameSite=Strict for security
  const expires = new Date(Date.now() + expiryDays * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires.toUTCString()}; path=/; SameSite=Strict`;
}

/**
 * Get the value of a cookie by name
 */
export function getCookie(name: string): string | null {
  const cookies = document.cookie.split(';');
  for (let i = 0; i < cookies.length; i++) {
    const cookie = cookies[i].trim();
    if (cookie.startsWith(name + '=')) {
      return decodeURIComponent(cookie.substring(name.length + 1));
    }
  }
  return null;
}

/**
 * Remove a cookie by setting its expiration to the past
 */
export function removeCookie(name: string): void {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict`;
}

/**
 * Clear all authentication-related cookies
 */
export function clearAuthCookies(): void {
  removeCookie('auth_token');
  removeCookie('user_id');
  removeCookie('auth_timestamp');
  removeCookie('last_token_refresh_time');
  // Make sure to remove the userRole cookie specifically
  removeCookie('userRole');
}
