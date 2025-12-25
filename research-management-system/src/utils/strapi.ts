export interface PaginationFallback {
  page?: number
  pageSize?: number
}

function isStrapiResponse(res: any) {
  return res && res.code === undefined && Object.prototype.hasOwnProperty.call(res, 'data')
}

export function normalizeStrapiItem<T = any>(
  item: any,
  mapper?: (entity: any, raw?: any) => T
): T {
  if (!item) return item as T
  const entity = item.attributes ? { id: item.id ?? item.attributes?.id, ...item.attributes } : { ...item }
  return mapper ? mapper(entity, item) : (entity as T)
}

export function normalizeStrapiList<T = any>(
  res: any,
  mapper?: (entity: any, raw?: any) => T,
  fallback: PaginationFallback = {}
) {
  if (!isStrapiResponse(res) || !Array.isArray(res.data)) return res
  const list = res.data.map((item: any) => normalizeStrapiItem<T>(item, mapper))
  const pagination = res.meta?.pagination || {}
  return {
    data: {
      list,
      total: pagination.total ?? list.length,
      page: pagination.page ?? fallback.page ?? 1,
      pageSize: pagination.pageSize ?? fallback.pageSize ?? list.length
    },
    meta: res.meta
  }
}

export function normalizeStrapiCollection<T = any>(
  res: any,
  mapper?: (entity: any, raw?: any) => T
) {
  if (!isStrapiResponse(res) || !Array.isArray(res.data)) return res
  const list = res.data.map((item: any) => normalizeStrapiItem<T>(item, mapper))
  return {
    data: list,
    meta: res.meta
  }
}

export function normalizeStrapiSingle<T = any>(
  res: any,
  mapper?: (entity: any, raw?: any) => T
) {
  if (!isStrapiResponse(res) || !res.data || Array.isArray(res.data)) return res
  return {
    data: normalizeStrapiItem<T>(res.data, mapper),
    meta: res.meta
  }
}

export function normalizeStrapiMedia(raw: any) {
  const files = Array.isArray(raw) ? raw : raw?.data
  if (!Array.isArray(files)) return []
  return files.map((file: any) => {
    const attrs = file?.attributes ? { id: file.id ?? file.attributes?.id, ...file.attributes } : { ...file }
    return {
      id: attrs.id ?? file?.id,
      name: attrs.name || attrs.filename || attrs.alternativeText || '附件',
      url: attrs.url,
      size: attrs.size,
      ...attrs
    }
  })
}
