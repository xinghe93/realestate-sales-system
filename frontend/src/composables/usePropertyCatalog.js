import { computed, ref } from 'vue'
import { getProperties } from '../api/properties'
import { sampleProperties } from '../data/mockData'

const markerPositions = [
  { x: 76, y: 43 },
  { x: 39, y: 30 },
  { x: 61, y: 28 },
  { x: 35, y: 88 },
  { x: 64, y: 72 },
  { x: 52, y: 58 }
]

export function normalizeProperty(property, index = 0) {
  const point = markerPositions[index % markerPositions.length]
  return {
    id: property.id ?? index + 1,
    title: property.title || '未命名房源',
    region: property.region || '未知区域',
    address: property.address || '未填写地址',
    layout: property.layout || '未填写户型',
    price: Number(property.price || 0),
    area: Number(property.area || 0),
    imageUrl: property.imageUrl || '',
    description: property.description || '暂无房源描述',
    status: property.status || 'PUBLISHED',
    contactName: property.contactName || '发布管理员',
    contactPhone: property.contactPhone || '未填写电话',
    createdBy: property.createdBy,
    x: property.x || point.x,
    y: property.y || point.y
  }
}

export function usePropertyCatalog(options = {}) {
  const properties = ref([])
  const loading = ref(false)
  const error = ref(null)

  const published = computed(() => properties.value.filter((item) => item.status === 'PUBLISHED'))
  const offline = computed(() => properties.value.filter((item) => item.status === 'OFFLINE'))

  async function load(params = {}) {
    loading.value = true
    error.value = null
    try {
      const response = await getProperties({
        page: 1,
        pageSize: 100,
        ...options.params,
        ...params
      })
      const data = response.data
      const items = Array.isArray(data?.items) ? data.items : Array.isArray(data) ? data : sampleProperties
      properties.value = items.map(normalizeProperty)
    } catch (currentError) {
      error.value = currentError
      properties.value = sampleProperties.map(normalizeProperty)
    } finally {
      loading.value = false
    }
  }

  return {
    properties,
    published,
    offline,
    loading,
    error,
    load
  }
}
