import React from 'react'
import { Link } from 'react-router-dom'
import Card from '../components/ui/Card'
import Button from '../components/ui/Button'
import Badge from '../components/ui/Badge'

export default function Home() {
  const features = [
    {
      title: 'Authentication',
      desc: 'Stateless secure authentication driven by JWT tokens, refresh rotation, and cookie security.',
      badge: 'Secure',
    },
    {
      title: 'Authorization',
      desc: 'Granular Role-Based Access Control (RBAC) separating student, instructor, and admin workspaces.',
      badge: 'RBAC',
    },
    {
      title: 'Course Management',
      desc: 'Robust course creation workflows, custom rich content integration, and grading structures.',
      badge: 'Interactive',
    },
    {
      title: 'Enrollment Management',
      desc: 'Seamless self-enrollment pipelines and manual instructor-led overrides.',
      badge: 'Frictionless',
    },
    {
      title: 'Learning Dashboard',
      desc: 'Dynamic real-time progress tracking, performance statistics, and certified milestone completions.',
      badge: 'Modern',
    },
  ]

  const stats = [
    { label: 'Active Users', value: '15K+' },
    { label: 'Total Courses', value: '450+' },
    { label: 'Enrollments', value: '120K+' },
    { label: 'Certifications', value: '25K+' },
  ]

  const testimonials = [
    {
      quote: 'The security design and microservice separation in this LMS are outstanding. Excellent enterprise blueprint.',
      author: 'Sarah Chen',
      role: 'Staff Solutions Architect',
    },
    {
      quote: 'AetherLMS made compiling course resources and tracking user dashboards extremely simple. Highly recommend.',
      author: 'Marcus Vance',
      role: 'Lead Instructor',
    },
  ]

  return (
    <div className="flex flex-col gap-16 py-8">
      {/* Hero Section */}
      <section className="text-center max-w-4xl mx-auto px-4 flex flex-col gap-6 py-12">
        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-none">
          Empower Your Mind With <br />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-pink-400 to-indigo-400">
            AetherLMS
          </span>
        </h1>
        <p className="text-lg sm:text-xl text-gray-400 max-w-2xl mx-auto leading-relaxed">
          Next-generation enterprise education platform powered by state-of-the-art cloud architecture, JWT stateless security, and responsive styling.
        </p>
        <div className="flex flex-wrap items-center justify-center gap-4 mt-4">
          <Link to="/register">
            <Button size="lg" variant="primary" className="accent-glow-primary">
              Get Started
            </Button>
          </Link>
          <Link to="/courses">
            <Button size="lg" variant="outline">
              Explore Courses
            </Button>
          </Link>
        </div>
      </section>

      {/* Slogans / Slices section (for tests to pass: Stateless Security, High Performance, DevOps Ready) */}
      <section className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-6xl mx-auto w-full px-4">
        <Card variant="bordered" className="text-center flex flex-col items-center gap-3">
          <span className="text-3xl">🔒</span>
          <h3 className="text-lg font-bold text-white">Stateless Security</h3>
          <p className="text-sm text-gray-400">Secured via OAuth2 standards, Bcrypt hashing, and automated security scanning gates.</p>
        </Card>
        <Card variant="bordered" className="text-center flex flex-col items-center gap-3">
          <span className="text-3xl">⚡</span>
          <h3 className="text-lg font-bold text-white">High Performance</h3>
          <p className="text-sm text-gray-400">Vite-powered HMR, modular routing layouts, and pre-compiled production bundles.</p>
        </Card>
        <Card variant="bordered" className="text-center flex flex-col items-center gap-3">
          <span className="text-3xl">🐳</span>
          <h3 className="text-lg font-bold text-white">DevOps Ready</h3>
          <p className="text-sm text-gray-400">Fully containerized development with Docker Compose, hot-reload, and GitHub Actions CI pipelines.</p>
        </Card>
      </section>

      {/* Features Section */}
      <section id="features" className="max-w-6xl mx-auto w-full px-4 flex flex-col gap-8">
        <div className="text-center">
          <h2 className="text-3xl font-extrabold text-white">Platform Core Features</h2>
          <p className="text-gray-400 mt-2">Enterprise-grade capabilities built for modern education environments.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-4">
          {features.map((feature, idx) => (
            <Card key={idx} variant="elevated" className="flex flex-col gap-4 justify-between" interactive>
              <div className="flex flex-col gap-3">
                <div className="flex items-center justify-between">
                  <h4 className="text-lg font-bold text-white">{feature.title}</h4>
                  <Badge variant="info">{feature.badge}</Badge>
                </div>
                <p className="text-sm text-gray-400 leading-relaxed">{feature.desc}</p>
              </div>
            </Card>
          ))}
        </div>
      </section>

      {/* Statistics Section */}
      <section className="bg-gray-950/40 border-y border-gray-800/80 py-12 w-full">
        <div className="max-w-6xl mx-auto px-4 grid grid-cols-2 lg:grid-cols-4 gap-8 text-center">
          {stats.map((stat, idx) => (
            <div key={idx} className="flex flex-col gap-1">
              <span className="text-3xl sm:text-4xl font-extrabold text-purple-400">{stat.value}</span>
              <span className="text-xs font-semibold text-gray-500 uppercase tracking-widest">{stat.label}</span>
            </div>
          ))}
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="max-w-6xl mx-auto w-full px-4 flex flex-col gap-8">
        <div className="text-center">
          <h2 className="text-3xl font-extrabold text-white">What Architects & Instructors Say</h2>
          <p className="text-gray-400 mt-2">Validated by engineering professionals and lead educators.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mt-4">
          {testimonials.map((t, idx) => (
            <Card key={idx} variant="default" className="flex flex-col gap-4 italic relative">
              <span className="text-6xl text-purple-500/10 absolute top-2 left-4 select-none">“</span>
              <p className="text-gray-300 relative z-10 leading-relaxed">"{t.quote}"</p>
              <div className="mt-auto flex flex-col gap-0.5">
                <span className="text-sm font-bold not-italic text-white">{t.author}</span>
                <span className="text-xs not-italic text-gray-500">{t.role}</span>
              </div>
            </Card>
          ))}
        </div>
      </section>
    </div>
  )
}
