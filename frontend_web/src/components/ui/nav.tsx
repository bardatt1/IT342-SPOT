import { Button } from './button'
import spotLogo from '../../img/spot-logo.png';
import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';

export function Nav() {
  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  // Check if user is authenticated
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  
  useEffect(() => {
    const token = localStorage.getItem('token');
    setIsAuthenticated(!!token);
  }, []);

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 flex justify-center pt-4">
      <div className="flex items-center justify-between px-6 py-2 bg-[#333333]/90 rounded-full backdrop-blur-sm w-fit">
        <div className="flex items-center space-x-12">
          <button onClick={() => scrollToSection('hero')} className="flex items-center">
            <img src={spotLogo} alt="SPOT" className="h-8" />
          </button>
          <div className="flex space-x-10">
            <button 
              onClick={() => scrollToSection('problem')}
              className="text-[15px] text-white/90 hover:text-white transition-colors font-medium"
            >
              The Problem
            </button>
            <button 
              onClick={() => scrollToSection('solution')}
              className="text-[15px] text-white/90 hover:text-white transition-colors font-medium"
            >
              Our Solution
            </button>
            <button 
              onClick={() => scrollToSection('team')}
              className="text-[15px] text-white/90 hover:text-white transition-colors font-medium"
            >
              Team
            </button>
          </div>
        </div>
        <div className="ml-12 flex items-center space-x-4">
          {isAuthenticated ? (
            <>
              <Link to="/home" className="text-[15px] text-white/90 hover:text-white transition-colors font-medium">
                Dashboard
              </Link>
              <Button 
                asChild 
                className="rounded-full bg-red-500 hover:bg-red-600 text-white px-6 shadow-lg"
                size="sm"
              >
                <Link to="/logout">Logout</Link>
              </Button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-[15px] text-white/90 hover:text-white transition-colors font-medium">
                Login
              </Link>
              <Button 
                asChild 
                className="rounded-full bg-white hover:bg-gray-100 text-gray-900 px-6 shadow-lg"
                size="sm"
              >
                <Link to="/signup">Join SPOT</Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}
