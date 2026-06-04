import React, { useState } from 'react'
import Input from '../components/ui/Input'
import TextArea from '../components/ui/TextArea'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import Alert from '../components/ui/Alert'

export default function Contact() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [submitted, setSubmitted] = useState(false)

  const handleSubmit = (e) => {
    e.preventDefault()
    setSubmitted(true)
    setName('')
    setEmail('')
    setMessage('')
  }

  return (
    <div className="flex items-center justify-center min-h-[70vh] px-4 py-8">
      <Card variant="elevated" className="w-full max-w-lg flex flex-col gap-6 p-8 bg-gray-900/60 border border-gray-800">
        <div className="text-center flex flex-col gap-2">
          <h2 className="text-2xl font-bold text-white tracking-wide">Contact Us</h2>
          <p className="text-sm text-gray-400">Get in touch with AetherLMS support or sales</p>
        </div>

        {submitted && (
          <Alert variant="success" onClose={() => setSubmitted(false)}>
            Thank you for reaching out! We will respond as soon as possible.
          </Alert>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Input
            label="Full Name"
            type="text"
            id="name"
            placeholder="Jane Doe"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />

          <Input
            label="Email Address"
            type="email"
            id="email"
            placeholder="jane.doe@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <TextArea
            label="Message"
            id="message"
            placeholder="Enter your query here..."
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            required
          />

          <Button type="submit" variant="primary" className="w-full mt-2">
            Send Message
          </Button>
        </form>
      </Card>
    </div>
  )
}
