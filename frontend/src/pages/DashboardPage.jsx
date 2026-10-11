import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

// Placeholder so sign-in has somewhere to land. Replace with the real dashboard (UC-SPEND-01).
export default function DashboardPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <>
      <nav className="navbar bg-white border-bottom">
        <div className="container">
          <span className="navbar-brand brand fw-semibold">Student Savings</span>
          <button className="btn btn-outline-secondary btn-sm" onClick={handleLogout}>Sign out</button>
        </div>
      </nav>
      <main className="container py-5">
        <h1 className="h3">Welcome, {user.firstName}!</h1>
        <p className="text-muted">You're signed in as {user.username}. The dashboard goes here.</p>
      </main>
    </>
  )
}