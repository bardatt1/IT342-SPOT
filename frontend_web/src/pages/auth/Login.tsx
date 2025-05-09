"use client"

import type React from "react"

import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "../../contexts/AuthContext"
import { Button } from "../../components/ui/button"
import { Input } from "../../components/ui/input"
import { Label } from "../../components/ui/label"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "../../components/ui/card"
import { AlertCircle, Eye, EyeOff, LogIn } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "../../components/ui/alert"
import { Badge } from "../../components/ui/badge"
import axios from "axios"

const Login = () => {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setIsLoading(true)

    try {
      console.log("Attempting login with:", email)

      // Make direct API call to login
      const apiUrl = import.meta.env.VITE_API_URL || "https://backend.spot-edu.me/api"
      const response = await axios.post(`${apiUrl}/auth/login`, { email, password })
      console.log("Login API response:", response.data)

      // Extract data directly from response
      const responseData = response.data.data || response.data
      const userType = responseData.userType

      // Update auth context - this will parse the response correctly
      await login(email, password)
      console.log("Login successful through AuthContext")

      // Determine redirect based on user type from API response
      let targetRoute = "/"

      if (userType === "SYSTEMADMIN") {
        console.log("User is SYSTEMADMIN, redirecting to system admin dashboard")
        targetRoute = "/system-admin/dashboard"
      } else if (userType === "ADMIN") {
        console.log("User is ADMIN, redirecting to admin dashboard")
        targetRoute = "/admin/dashboard"
      } else if (userType === "TEACHER") {
        console.log("User is TEACHER, redirecting to teacher dashboard")
        targetRoute = "/teacher/dashboard"
      }

      // Use navigate for spa navigation
      console.log(`Redirecting to ${targetRoute}`)
      navigate(targetRoute)
    } catch (error) {
      console.error("Login error:", error)
      setError("Invalid email or password. Please try again.")
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-[#215f47]/5 via-white to-[#215f47]/10 p-4">
      <div className="w-full max-w-md">
        <Card className="border-[#215f47]/20 shadow-xl overflow-hidden">
          <div className="h-2 bg-[#215f47]"></div>
          <CardHeader className="space-y-2 pt-8">
            <div className="flex justify-center mb-2">
              <Badge variant="outline" className="bg-[#215f47]/5 text-[#215f47] px-3 py-1 text-sm">
                Secure Login
              </Badge>
            </div>
            <CardTitle className="text-2xl text-center font-bold text-[#215f47]">SPOT</CardTitle>
            <CardDescription className="text-center text-gray-600">
              Student Presence and Oversight Tracker
            </CardDescription>
          </CardHeader>

          <CardContent className="space-y-4 pt-4">
            {error && (
              <Alert variant="destructive" className="border-red-300 bg-red-50">
                <AlertCircle className="h-4 w-4 text-red-600" />
                <AlertTitle className="text-red-700">Authentication Error</AlertTitle>
                <AlertDescription className="text-red-600">{error}</AlertDescription>
              </Alert>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email" className="text-gray-700">SPOT Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="name@spot-edu.me"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password" className="text-gray-700">Password</Label>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="border-[#215f47]/20 focus:border-[#215f47] focus:ring-2 focus:ring-[#215f47]/20"
                  />
                  <button
                    type="button"
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-[#215f47]"
                    onClick={() => setShowPassword(!showPassword)}
                    tabIndex={-1}
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4" aria-hidden="true" />
                    ) : (
                      <Eye className="h-4 w-4" aria-hidden="true" />
                    )}
                  </button>
                </div>
              </div>

              <Button 
                type="submit" 
                className="w-full mt-6 bg-[#215f47] hover:bg-[#215f47]/90 text-white gap-2 py-2"
                disabled={isLoading}
              >
                {isLoading ? "Signing in..." : "Sign in"}
                {!isLoading && <LogIn className="h-4 w-4" />}
              </Button>
            </form>
          </CardContent>

          <CardFooter className="flex justify-center text-sm text-gray-500 pb-6">
            <p>Protected access for authorized personnel only</p>
          </CardFooter>
        </Card>
      </div>
    </div>
  )
}

export default Login;
