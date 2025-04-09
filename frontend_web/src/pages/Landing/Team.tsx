import gabe from '@/img/gabe-spot.png'
import brett from '@/img/Brett.jpg'
import marga from '@/img/Marga.jpg'

const team = [
  {
    name: 'Gabe San Diego',
    role: 'Full Stack Developer',
    image: gabe
  },
  {
    name: 'Brett Arda',
    role: 'Backend Developer',
    image: brett
  },
  {
    name: 'Marga Matunog',
    role: 'Mobile Developer',
    image: marga
  }
]

export default function TeamPage() {
  return (
    <section id="team" className="min-h-screen relative py-20">
      <div className="absolute inset-0 bg-gray-900/95 z-0" />
      <div className="container mx-auto px-4 relative z-10">
        <h1 className="text-4xl font-bold text-center mb-12 text-white">Our Team</h1>
        <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {team.map((member) => (
            <div key={member.name} className="bg-white/10 backdrop-blur-sm p-8 rounded-lg border border-white/10 hover:bg-white/20 transition-colors text-center">
              <img
                src={member.image}
                alt={member.name}
                className="w-32 h-32 mx-auto mb-4 rounded-full object-cover"
              />
              <h2 className="text-xl font-semibold mb-2 text-white">{member.name}</h2>
              <p className="text-white/80">{member.role}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
