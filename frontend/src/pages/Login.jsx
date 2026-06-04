import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import Alert from '../components/ui/Alert'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const handleLogin = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api/v1'

    try {
      const response = await fetch(`${baseUrl}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'Login failed. Please check credentials.')
      }

      setSuccess('Login successful! Welcome back.')
      localStorage.setItem('accessToken', data.data.accessToken)
      localStorage.setItem('user', JSON.stringify(data.data.user))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[70vh] px-4 py-8">
      <Card variant="elevated" className="w-full max-w-md flex flex-col gap-6 p-8 bg-gray-900/60 border border-gray-800">
        <div className="text-center flex flex-col gap-2">
          <h2 className="text-2xl font-bold text-white tracking-wide">Welcome Back</h2>
          <p className="text-sm text-gray-400">Login to your AetherLMS account</p>
        </div>

        {error && (
          <Alert variant="error" className="alert-error">
            {error}
          </Alert>
        )}
        {success && (
          <Alert variant="success" className="alert-success">
            {success}
          </Alert>
        )}

        <form onSubmit={handleLogin} className="flex flex-col gap-4" noValidate>
          <Input
            label="Email Address"
            type="email"
            id="email"
            placeholder="name@organization.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <Input
            label="Password"
            type="password"
            id="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <Button type="submit" variant="primary" className="w-full mt-2" disabled={loading}>
            {loading ? 'Logging in...' : 'Sign In'}
          </Button>
        </form>

        <p className="text-center text-sm text-gray-400">
          Don't have an account?{' '}
          <Link to="/register" className="text-purple-400 hover:text-purple-300 font-medium transition-colors">
            Register here
          </Link>
        </p>
      </Card>
    </div>
  )
}
