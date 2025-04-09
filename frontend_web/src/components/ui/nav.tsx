import { Button } from './button'
import spotLogo from '../../img/spot-logo.png'; 

export function Nav() {
  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

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
        <div className="ml-12">
          <Button 
            asChild 
            className="rounded-full bg-white hover:bg-gray-100 text-gray-900 px-6 shadow-lg"
            size="sm"
          >
            Join Spot
          </Button>
        </div>
      </div>
    </nav>
  )
}
