import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'

// Sends signed-out users to /login, then back to the page they wanted after signing in.
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <div className="text-center mt-5">Loading…</div>
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />
  return children
}
