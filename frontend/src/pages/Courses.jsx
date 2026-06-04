import React, { useState, useEffect } from 'react'
import Table from '../components/ui/Table'
import Badge from '../components/ui/Badge'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'

export default function Courses() {
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Simulate API fetch delay
    const timer = setTimeout(() => {
      setCourses([
        {
          id: 'course-1',
          title: 'Introduction to Microservices Architecture',
          instructor: 'Dr. Evelyn Foster',
          difficulty: 'Beginner',
          duration: '6 Weeks',
          enrollmentStatus: 'Open',
        },
        {
          id: 'course-2',
          title: 'Advanced Spring Boot & Security Integrations',
          instructor: 'Staff Engineer John Doe',
          difficulty: 'Advanced',
          duration: '10 Weeks',
          enrollmentStatus: 'Open',
        },
        {
          id: 'course-3',
          title: 'React 19 & Modern State Management',
          instructor: 'Lead Architect Sarah Chen',
          difficulty: 'Intermediate',
          duration: '8 Weeks',
          enrollmentStatus: 'Closed',
        },
        {
          id: 'course-4',
          title: 'DevOps pipelines & Kubernetes Orchestration',
          instructor: 'DevOps Lead Marcus Vance',
          difficulty: 'Advanced',
          duration: '12 Weeks',
          enrollmentStatus: 'Open',
        },
      ])
      setLoading(false)
    }, 800)

    return () => clearTimeout(timer)
  }, [])

  const headers = ['Course Title', 'Instructor', 'Difficulty', 'Duration', 'Enrollment', 'Action']

  const renderRow = (course, idx) => {
    const difficultyVariants = {
      Beginner: 'success',
      Intermediate: 'info',
      Advanced: 'danger',
    }

    const enrollmentVariants = {
      Open: 'success',
      Closed: 'default',
    }

    return (
      <tr key={course.id} className="hover:bg-gray-800/30 transition-colors">
        <td className="px-6 py-4 font-semibold text-white max-w-xs sm:max-w-sm truncate">
          {course.title}
        </td>
        <td className="px-6 py-4 text-gray-300">
          {course.instructor}
        </td>
        <td className="px-6 py-4">
          <Badge variant={difficultyVariants[course.difficulty] || 'default'}>
            {course.difficulty}
          </Badge>
        </td>
        <td className="px-6 py-4 text-gray-400">
          {course.duration}
        </td>
        <td className="px-6 py-4">
          <Badge variant={enrollmentVariants[course.enrollmentStatus] || 'default'}>
            {course.enrollmentStatus}
          </Badge>
        </td>
        <td className="px-6 py-4">
          <Button
            size="sm"
            variant={course.enrollmentStatus === 'Open' ? 'primary' : 'outline'}
            disabled={course.enrollmentStatus !== 'Open'}
          >
            {course.enrollmentStatus === 'Open' ? 'Enroll' : 'Unavailable'}
          </Button>
        </td>
      </tr>
    )
  }

  return (
    <div className="flex flex-col gap-8 py-8 w-full max-w-6xl mx-auto">
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex flex-col gap-1.5">
          <h1 className="text-3xl font-extrabold text-white">Course Catalog</h1>
          <p className="text-gray-400 text-sm">Explore our list of courses and enroll to upgrade your skills.</p>
        </div>
        <Button variant="secondary" onClick={() => alert('Search and filter utilities coming soon.')}>
          🔍 Filter Courses
        </Button>
      </div>

      <Card variant="elevated" className="p-0 border border-gray-800 overflow-hidden">
        <Table
          headers={headers}
          data={courses}
          loading={loading}
          renderRow={renderRow}
          emptyMessage="No courses are currently offered."
        />
      </Card>
    </div>
  )
}
