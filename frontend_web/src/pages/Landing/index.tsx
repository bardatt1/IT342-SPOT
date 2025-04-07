import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Nav } from "@/components/ui/nav";
import backgroundBg from "@/img/classroom-bg.png";

// Import the sections
import Problem from "@/pages/Landing/Problem";
import Solution from "@/pages/Landing/Solution";
import Team from "@/pages/Landing/Team";

export default function LandingPage() {
  return (
    <div className="min-h-screen relative">
      {/* Background Image - Hero Section */}
      <div id="hero" className="min-h-screen relative overflow-hidden">
        <div
          className="absolute inset-0 z-0 grayscale"
          style={{
            backgroundImage: `url("${backgroundBg}")`,
            backgroundSize: "cover",
            backgroundPosition: "center",
            filter: "brightness(0.7)",
          }}
        />
        
        <Nav />
        
        <div className="relative z-10 flex flex-col items-center justify-center min-h-screen text-center px-4">
          <h1 className="text-5xl md:text-6xl font-bold text-white mb-6">
            Welcome to SPOT
          </h1>
          <p className="text-xl text-white/90 max-w-2xl mb-8">
            Smart Presence Observation Technology - Revolutionizing attendance tracking in educational institutions
          </p>
          <Button 
            asChild 
            size="lg" 
            className="bg-white text-gray-900 hover:bg-gray-100"
          >
            <Link to="/login" className="flex items-center">
              Get Started
            </Link>
          </Button>
        </div>
      </div>

      {/* Problem Section */}
      <Problem />

      {/* Solution Section */}
      <Solution />

      {/* Team Section */}
      <Team />
    </div>
  );
}
