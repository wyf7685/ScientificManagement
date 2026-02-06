import dayjs from 'dayjs'

export function formatDateTime(value?: string, format = 'YYYY-MM-DD HH:mm'): string {
  if (!value) return '-'
  const parsed = dayjs(value)
  if (parsed.isValid()) {
    return parsed.format(format)
  }
  return String(value)
    .replace('T', ' ')
    .replace(/Z$/, '')
    .replace(/\.\d+$/, '')
}
