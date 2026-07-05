/**
 * NOTE: The backend does not (yet) expose a metrics-history endpoint, only a
 * point-in-time `/metrics/dashboard-summary` snapshot. For this portfolio demo
 * we synthesize a plausible rolling time series client-side, anchored to the
 * real summary numbers, so the charts aren't just empty/static placeholders.
 * Swap this out for a real `/metrics/history` call if one is ever added.
 */

export interface TimeSeriesPoint {
  timestamp: string
  label: string
  value: number
}

const WINDOW_MINUTES = 20
const STEP_MINUTES = 1

function seededJitter(seed: number, index: number, amplitude: number): number {
  // Deterministic-ish pseudo-random jitter so re-renders don't wildly reshuffle the chart.
  const x = Math.sin(seed * 12.9898 + index * 78.233) * 43758.5453
  const fraction = x - Math.floor(x)
  return (fraction - 0.5) * 2 * amplitude
}

function buildSeries(
  baseValue: number,
  amplitude: number,
  seed: number,
  options?: { min?: number; trendPerStep?: number },
): TimeSeriesPoint[] {
  const now = Date.now()
  const points: TimeSeriesPoint[] = []
  const min = options?.min ?? 0
  const trendPerStep = options?.trendPerStep ?? 0

  for (let i = WINDOW_MINUTES; i >= 0; i -= STEP_MINUTES) {
    const timestamp = new Date(now - i * 60_000)
    const stepsFromStart = WINDOW_MINUTES - i
    const trend = trendPerStep * stepsFromStart
    const jitter = seededJitter(seed, stepsFromStart, amplitude)
    const value = Math.max(min, baseValue + trend + jitter)
    points.push({
      timestamp: timestamp.toISOString(),
      label: timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      value: Math.round(value * 100) / 100,
    })
  }

  return points
}

export function simulateApiLatencySeries(avgLatencyMs: number): TimeSeriesPoint[] {
  return buildSeries(avgLatencyMs, Math.max(4, avgLatencyMs * 0.12), 1.1, { min: 1 })
}

export function simulateKafkaThroughputSeries(kafkaMessagesProcessed: number): TimeSeriesPoint[] {
  // Derive a plausible "messages per minute" baseline from the cumulative counter.
  const perMinuteBaseline = Math.max(50, Math.round(kafkaMessagesProcessed / 480))
  return buildSeries(perMinuteBaseline, perMinuteBaseline * 0.18, 2.3, { min: 0 })
}

export function simulateErrorRateSeries(degradedServices: number, totalServices: number): TimeSeriesPoint[] {
  const ratio = totalServices > 0 ? degradedServices / totalServices : 0
  const baselinePercent = Math.max(0.1, ratio * 8)
  return buildSeries(baselinePercent, baselinePercent * 0.35, 3.7, { min: 0 })
}
