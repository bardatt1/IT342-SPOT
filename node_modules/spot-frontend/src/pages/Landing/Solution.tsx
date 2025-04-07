export default function SolutionPage() {
  return (
    <section id="solution" className="min-h-screen relative py-20">
      <div className="absolute inset-0 bg-gray-900/95 z-0" />
      <div className="container mx-auto px-4 relative z-10">
        <h1 className="text-4xl font-bold text-center mb-12 text-white">Our Solution</h1>
        <div className="max-w-4xl mx-auto space-y-8">
          <div className="bg-white/10 backdrop-blur-sm p-8 rounded-lg border border-white/10 hover:bg-white/20 transition-colors">
            <h2 className="text-2xl font-semibold mb-4 text-white">Smart Attendance System</h2>
            <p className="text-white/80">
              SPOT uses advanced facial recognition technology to automatically track student attendance,
              eliminating the need for manual roll calls and reducing administrative overhead.
            </p>
          </div>
          
          <div className="bg-white/10 backdrop-blur-sm p-8 rounded-lg border border-white/10 hover:bg-white/20 transition-colors">
            <h2 className="text-2xl font-semibold mb-4 text-white">Real-time Analytics</h2>
            <p className="text-white/80">
              Get instant insights into attendance patterns with our powerful analytics dashboard.
              Track trends, identify issues, and make data-driven decisions to improve student engagement.
            </p>
          </div>
          
          <div className="bg-white/10 backdrop-blur-sm p-8 rounded-lg border border-white/10 hover:bg-white/20 transition-colors">
            <h2 className="text-2xl font-semibold mb-4 text-white">Seamless Integration</h2>
            <p className="text-white/80">
              SPOT integrates smoothly with existing school management systems and provides
              automated notifications to keep all stakeholders informed in real-time.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
