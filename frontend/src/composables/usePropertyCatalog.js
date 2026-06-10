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

const sampleImages = [
  '/property-images/interior-river-suite.png',
  '/property-images/interior-city-lounge.png',
  '/property-images/interior-cozy-flat.png',
  '/property-images/interior-luxury-flat.png'
]

export function normalizeProperty(property, index = 0) {
  const point = markerPositions[index % markerPositions.length]
  const mapX = Number(property.mapX ?? property.x ?? point.x)
  const mapY = Number(property.mapY ?? property.y ?? point.y)
  const price = normalizePriceWan(property.price)
  return {
    id: property.id ?? index + 1,
    title: property.title || '未命名房源',
    region: property.region || '未知区域',
    address: property.address || '未填写地址',
    layout: property.layout || '未填写户型',
    price,
    area: Number(property.area || 0),
    imageUrl: property.imageUrl ?? null,
    description: property.description || '暂无房源描述',
    status: property.status || 'PUBLISHED',
    contactName: property.contactName || '发布管理员',
    contactPhone: property.contactPhone || '未填写电话',
    createdBy: property.createdBy,
    mapX,
    mapY,
    x: mapX,
    y: mapY
  }
}

function normalizePriceWan(value) {
  const price = Number(value || 0)
  return price > 10000 ? Number((price / 10000).toFixed(2)) : price
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
