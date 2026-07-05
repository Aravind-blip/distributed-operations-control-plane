import { useCallback, useEffect, useRef, useState } from 'react'

interface AsyncState<T> {
  data: T | null
  isLoading: boolean
  error: string | null
}

/**
 * Fetches data via `fetcher` whenever `deps` changes, exposing a stable
 * loading -> error -> data lifecycle plus a manual `refetch` for retry buttons.
 */
export function useAsync<T>(fetcher: () => Promise<T>, deps: unknown[]): AsyncState<T> & { refetch: () => void } {
  const [data, setData] = useState<T | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const fetcherRef = useRef(fetcher)
  fetcherRef.current = fetcher

  const [reloadToken, setReloadToken] = useState(0)

  const run = useCallback(() => {
    let cancelled = false
    setIsLoading(true)
    setError(null)

    fetcherRef
      .current()
      .then((result) => {
        if (!cancelled) setData(result)
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Unexpected error')
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, reloadToken])

  useEffect(() => run(), [run])

  const refetch = useCallback(() => setReloadToken((token) => token + 1), [])

  return { data, isLoading, error, refetch }
}
