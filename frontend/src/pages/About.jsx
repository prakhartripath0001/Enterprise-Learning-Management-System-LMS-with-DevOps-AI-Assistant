import React from 'react'
import Card from '../components/ui/Card'
import Badge from '../components/ui/Badge'

export default function About() {
  return (
    <div className="flex flex-col gap-12 py-8 max-w-4xl mx-auto">
      <div className="text-center flex flex-col gap-3">
        <h1 className="text-3xl sm:text-4xl font-extrabold text-white">About AetherLMS</h1>
        <p className="text-gray-400 text-base max-w-xl mx-auto leading-relaxed">
          Discover the mission, technology stack, and architectural guidelines behind our next-generation enterprise LMS.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <Card variant="elevated" className="flex flex-col gap-4">
          <div className="flex items-center gap-2">
            <span className="text-2xl">🎯</span>
            <h3 className="text-lg font-bold text-white">Our Vision</h3>
          </div>
          <p className="text-sm text-gray-400 leading-relaxed">
            Provide a production-ready, highly extensible platform for enterprise education that utilizes the database-per-service pattern, clean architecture, and strict security compliance.
          </p>
        </Card>

        <Card variant="elevated" className="flex flex-col gap-4">
          <div className="flex items-center gap-2">
            <span className="text-2xl">⚡</span>
            <h3 className="text-lg font-bold text-white">Core Technology</h3>
          </div>
          <p className="text-sm text-gray-400 leading-relaxed">
            Built using Spring Boot 3 microservices and React Vite. Orchestrated using Docker containers and verified through automated GitHub Actions pipelines.
          </p>
        </Card>
      </div>

      <Card variant="bordered" className="flex flex-col gap-4">
        <h3 className="text-lg font-bold text-white">Compliance & Architecture</h3>
        <p className="text-sm text-gray-400 leading-relaxed">
          AetherLMS follows strict OWASP guidelines for web security, Bcrypt password encryption (work factor 12), and JWT session authorization. All schema updates are managed through Flyway migration files.
        </p>
        <div className="flex flex-wrap gap-2 mt-2">
          <Badge variant="info">Java 21</Badge>
          <Badge variant="info">Spring Security 6</Badge>
          <Badge variant="info">MySQL 8</Badge>
          <Badge variant="info">Docker</Badge>
          <Badge variant="info">Flyway</Badge>
        </div>
      </Card>
    </div>
  )
}
