/**
 * List endpoints return a Spring Data `Page<T>` (`{ content, totalElements, ... }`),
 * but may also be a bare array or a `{ items, total }` envelope depending on
 * deployment. Normalize all three shapes so page code only ever deals with a
 * consistent Paginated<T> result.
 */
import type { Paginated } from '../types/domain'

export interface SpringPage<T> {
  content: T[]
  totalElements: number
}

function isSpringPage<T>(data: unknown): data is SpringPage<T> {
  return (
    typeof data === 'object' &&
    data !== null &&
    Array.isArray((data as SpringPage<T>).content) &&
    typeof (data as SpringPage<T>).totalElements === 'number'
  )
}

export function normalizeList<T>(data: T[] | Paginated<T> | SpringPage<T>): Paginated<T> {
  if (Array.isArray(data)) {
    return { items: data, total: data.length }
  }
  if (isSpringPage<T>(data)) {
    return { items: data.content, total: data.totalElements }
  }
  return data
}
