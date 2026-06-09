<template>
  <main class="atlas-page">
    <section
      ref="mapCanvasRef"
      class="atlas-map"
      :class="{ dragging: dragState.dragging }"
      @pointerdown="startDrag"
      @pointermove="dragMap"
      @pointerup="endDrag"
      @pointerleave="endDrag"
      @pointercancel="endDrag"
    >
      <div ref="mapLayerRef" class="map-layer" :style="mapLayerStyle">
        <img class="map-image" :src="mapImage" alt="城市地图底图" draggable="false" />
        <button
          v-for="property in visibleProperties"
          :key="property.id"
          class="map-pin"
          :class="{ active: property.id === activeProperty?.id }"
          :style="{ left: `${property.x}%`, top: `${property.y}%` }"
          @click.stop="selectProperty(property)"
        >
          <span>{{ moneyWan(property.price) }}</span>
        </button>
      </div>
    </section>

    <aside class="atlas-filter">
      <div class="panel-heading">
        <span>Map Filter</span>
        <button type="button" @click="resetFilters">
          <Refresh />
          重置
        </button>
      </div>

      <el-form label-position="top">
        <el-form-item label="区域">
          <el-select v-model="filters.region" placeholder="全部区域" clearable>
            <el-option v-for="region in regions" :key="region" :label="region" :value="region" />
          </el-select>
        </el-form-item>
        <el-form-item label="户型">
          <el-select v-model="filters.layout" placeholder="全部户型" clearable>
            <el-option label="两室" value="两" />
            <el-option label="三室" value="三" />
            <el-option label="四室" value="四" />
          </el-select>
        </el-form-item>
        <el-form-item label="总价区间（万元）">
          <div class="range-inputs">
            <el-input-number v-model="filters.minPrice" :min="0" :controls="false" />
            <span>至</span>
            <el-input-number v-model="filters.maxPrice" :min="0" :controls="false" />
          </div>
        </el-form-item>
      </el-form>

      <div class="filter-summary">
        <span>已发布房源</span>
        <strong>{{ visibleProperties.length }}</strong>
      </div>
    </aside>

    <aside v-if="activeProperty" class="atlas-detail">
      <div class="detail-topline">
        <span>{{ activeProperty.region }}</span>
        <StatusBadge :property-status="activeProperty.status" />
      </div>
      <h1>{{ activeProperty.title }}</h1>
      <p>{{ activeProperty.address }} · {{ activeProperty.layout }} · {{ squareMeters(activeProperty.area) }}</p>
      <div class="detail-price">
        <strong>{{ moneyWan(activeProperty.price) }}</strong>
        <span>{{ unitPrice(activeProperty) }}</span>
      </div>
      <div class="detail-contact">
        <span>发布管理员</span>
        <strong>{{ activeProperty.contactName }}</strong>
        <a :href="`tel:${activeProperty.contactPhone}`">
          <Phone />
          {{ activeProperty.contactPhone }}
        </a>
      </div>
      <div class="detail-actions">
        <el-button type="primary" :icon="Phone">联系管理员</el-button>
        <el-button :icon="Star">收藏</el-button>
      </div>
    </aside>

    <div class="map-tools">
      <button type="button" @click="resetMap"><Aim />定位</button>
      <button type="button" @click="resetMap"><Refresh /></button>
    </div>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { Aim, Phone, Refresh, Star } from '@element-plus/icons-vue'
import StatusBadge from '../components/StatusBadge.vue'
import { usePropertyCatalog } from '../composables/usePropertyCatalog'
import { moneyWan, squareMeters, unitPrice } from '../utils/formatters'
import mapImage from '../assets/city-map-home.png'

const { properties, published, load } = usePropertyCatalog()

const mapCanvasRef = ref(null)
const mapLayerRef = ref(null)
const activeProperty = ref(null)

const filters = reactive({
  region: '',
  layout: '',
  minPrice: 0,
  maxPrice: 2000
})

const mapOffset = reactive({ x: 0, y: 0 })
const dragBounds = reactive({ minX: 0, maxX: 0, minY: 0, maxY: 0 })
const dragState = reactive({
  dragging: false,
  startX: 0,
  startY: 0,
  originX: 0,
  originY: 0
})

const regions = computed(() => [...new Set(properties.value.map((item) => item.region))])

const visibleProperties = computed(() => published.value.filter((property) => {
  const hitRegion = !filters.region || property.region === filters.region
  const hitLayout = !filters.layout || property.layout.includes(filters.layout)
  const hitPrice = property.price >= filters.minPrice && property.price <= filters.maxPrice
  return hitRegion && hitLayout && hitPrice
}))

const mapLayerStyle = computed(() => ({
  transform: `translate(${mapOffset.x}px, ${mapOffset.y}px)`
}))

onMounted(async () => {
  await load({ status: 'PUBLISHED' })
  activeProperty.value = visibleProperties.value[0]
  nextTick(updateDragBounds)
  window.addEventListener('resize', updateDragBounds)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateDragBounds)
})

function selectProperty(property) {
  activeProperty.value = property
}

function resetFilters() {
  filters.region = ''
  filters.layout = ''
  filters.minPrice = 0
  filters.maxPrice = 2000
  activeProperty.value = visibleProperties.value[0]
}

function startDrag(event) {
  if (event.target.closest('button, .atlas-detail, .atlas-filter')) {
    return
  }
  updateDragBounds()
  dragState.dragging = true
  dragState.startX = event.clientX
  dragState.startY = event.clientY
  dragState.originX = mapOffset.x
  dragState.originY = mapOffset.y
  event.currentTarget.setPointerCapture?.(event.pointerId)
}

function dragMap(event) {
  if (!dragState.dragging) {
    return
  }
  mapOffset.x = clamp(dragState.originX + event.clientX - dragState.startX, dragBounds.minX, dragBounds.maxX)
  mapOffset.y = clamp(dragState.originY + event.clientY - dragState.startY, dragBounds.minY, dragBounds.maxY)
}

function endDrag() {
  dragState.dragging = false
}

function resetMap() {
  mapOffset.x = 0
  mapOffset.y = 0
}

function updateDragBounds() {
  const canvas = mapCanvasRef.value
  const layer = mapLayerRef.value
  if (!canvas || !layer) {
    return
  }
  dragBounds.minX = Math.min(0, canvas.clientWidth - layer.offsetWidth)
  dragBounds.maxX = 0
  dragBounds.minY = Math.min(0, canvas.clientHeight - layer.offsetHeight)
  dragBounds.maxY = 0
  mapOffset.x = clamp(mapOffset.x, dragBounds.minX, dragBounds.maxX)
  mapOffset.y = clamp(mapOffset.y, dragBounds.minY, dragBounds.maxY)
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}
</script>
