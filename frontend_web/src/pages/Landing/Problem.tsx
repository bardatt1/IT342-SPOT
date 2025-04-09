export default function ProblemPage() {
  return (
    <section id="problem" className="min-h-screen relative py-20">
      <div className="absolute inset-0 bg-gray-900/95 z-0" />
      <div className="container mx-auto px-4 relative z-10">
        <h1 className="text-4xl font-bold text-center mb-12 text-white">The Problem</h1>
        <div className="max-w-4xl mx-auto space-y-8">
          <div className="bg-white/10 backdrop-blur-sm p-8 rounded-lg border border-white/10 hover:bg-white/20 transition-colors">
            <h2 className="text-2xl font-semibold mb-4 text-white">Manual Attendance Tracking</h2>
            <p className="text-white/80">
              Traditional attendance tracking methods are time-consuming and prone to errors.
              Teachers spend valuable class time taking attendance manually, which could be
              better spent on actual teaching.
            </p>
          </div>
          
          <div className="bg-white/10 backdrop-blur-sm p-8 rounded-lg border border-white/10 hover:bg-white/20 transition-colors">
            <h2 className="text-2xl font-semibold mb-4 text-white">Inefficient Record Keeping</h2>
            <p className="text-white/80">
              Paper-based attendance records are difficult to maintain, analyze, and share
              with stakeholders. This makes it challenging to track attendance patterns
              and identify potential issues early.
            </p>
          </div>
          
          <div className="bg-white/10 backdrop-blur-sm p-8 rounded-lg border border-white/10 hover:bg-white/20 transition-colors">
            <h2 className="text-2xl font-semibold mb-4 text-white">Limited Communication</h2>
            <p className="text-white/80">
              Current systems often lack real-time communication capabilities between
              teachers, administrators, and parents, leading to delayed responses to
              attendance issues.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
