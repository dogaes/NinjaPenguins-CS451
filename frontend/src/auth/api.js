// Small helper for calling the Spring Boot API. Sends the session cookie and parses
// errors shaped like {"errors": {"field": "message"}}.
export async function api(path, { method = 'GET', body } = {}) {
    const res = await fetch(`/api${path}`, {
      method,
      credentials: 'include',
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
    const data = res.status === 204 ? null : await res.json().catch(() => null)
    if (!res.ok) {
      const err = new Error('Request failed')
      err.status = res.status
      err.errors = data?.errors ?? { form: 'Something went wrong. Please try again.' }
      throw err
    }
    return data
  }
  