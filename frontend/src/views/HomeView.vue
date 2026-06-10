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
          :class="{ active: property.id === hoveredProperty?.id, offline: property.status === 'OFFLINE' }"
          :style="{ left: `${property.mapX}%`, top: `${property.mapY}%` }"
          @mouseenter="showPropertyCard(property)"
          @mouseleave="scheduleHidePropertyCard"
          @click.stop="goToDetail(property)"
        >
          <span><b>{{ property.title }}</b></span>
        </button>
      </div>
      <article
        v-if="hoveredProperty"
        :key="hoveredProperty.id"
        class="map-hover-card"
        :style="hoverCardStyle"
        @mouseenter="cancelHidePropertyCard"
        @mouseleave="scheduleHidePropertyCard"
        @click.stop="goToDetail(hoveredProperty)"
      >
        <div class="map-card-art">
          <img
            v-if="hoveredProperty.imageUrl"
            :key="`${hoveredProperty.id}-${hoveredProperty.imageUrl}`"
            :src="hoveredProperty.imageUrl"
            :alt="hoveredProperty.title"
          />
        </div>
        <div class="map-card-body">
          <div class="detail-topline">
            <h2>{{ hoveredProperty.title }}</h2>
            <StatusBadge :property-status="hoveredProperty.status" />
          </div>
          <p class="map-card-location">{{ hoveredProperty.region }} · {{ hoveredProperty.address }}</p>
          <p>{{ hoveredProperty.layout }} 丨 {{ squareMeters(hoveredProperty.area) }} 丨 南北通透</p>
          <div class="detail-price">
            <strong>{{ moneyWan(hoveredProperty.price) }}</strong>
            <span>{{ unitPrice(hoveredProperty) }}</span>
          </div>
          <div class="map-card-actions">
            <span>☆ 收藏</span>
            <i></i>
            <span>☎ 联系管理员</span>
          </div>
        </div>
      </article>
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
            <el-option label="两室" :value="2" />
            <el-option label="三室" :value="3" />
            <el-option label="四室" :value="4" />
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
        <span>地图房源</span>
        <strong>{{ visibleProperties.length }}</strong>
      </div>
    </aside>

    <div class="map-tools">
      <button type="button" @click="resetMap"><Aim />定位</button>
    </div>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Aim, Refresh } from '@element-plus/icons-vue'
import StatusBadge from '../components/StatusBadge.vue'
import { usePropertyCatalog } from '../composables/usePropertyCatalog'
import { moneyWan, squareMeters, unitPrice } from '../utils/formatters'
import mapImage from '../assets/city-map-home.png'

const { properties, load } = usePropertyCatalog()
const router = useRouter()

const mapCanvasRef = ref(null)
const mapLayerRef = ref(null)
const hoveredProperty = ref(null)
const hideTimer = ref(null)

const filters = reactive({
  region: '',
  layout: '',
  minPrice: 0,
  maxPrice: 2000
})

const mapOffset = reactive({ x: 0, y: 0 })
const mapSize = reactive({ width: 0, height: 0 })
const dragBounds = reactive({ minX: 0, maxX: 0, minY: 0, maxY: 0 })
const dragState = reactive({
  dragging: false,
  startX: 0,
  startY: 0,
  originX: 0,
  originY: 0
})

const regions = computed(() => [...new Set(properties.value.map((item) => item.region))])

const visibleProperties = computed(() => properties.value.filter((property) => {
  const hitRegion = !filters.region || property.region === filters.region
  const hitLayout = !filters.layout || getRoomCount(property.layout) === filters.layout
  const hitPrice = property.price >= filters.minPrice && property.price <= filters.maxPrice
  return hitRegion && hitLayout && hitPrice
}))

const mapLayerStyle = computed(() => ({
  transform: `translate(${mapOffset.x}px, ${mapOffset.y}px)`
}))

const hoverCardStyle = computed(() => {
  if (!hoveredProperty.value) {
    return {}
  }
  return {
    left: `${mapOffset.x + mapSize.width * Number(hoveredProperty.value.mapX) / 100}px`,
    top: `${mapOffset.y + mapSize.height * Number(hoveredProperty.value.mapY) / 100}px`
  }
})

onMounted(async () => {
  await load()
  preloadPropertyImages()
  await nextTick()
  updateDragBounds()
  centerMap()
  window.addEventListener('resize', updateDragBounds)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateDragBounds)
  cancelHidePropertyCard()
})

function resetFilters() {
  filters.region = ''
  filters.layout = ''
  filters.minPrice = 0
  filters.maxPrice = 2000
  hoveredProperty.value = null
}

function showPropertyCard(property) {
  cancelHidePropertyCard()
  if (hoveredProperty.value?.id === property.id) {
    return
  }
  hoveredProperty.value = property
}

function preloadPropertyImages() {
  properties.value.forEach((property) => {
    if (property.imageUrl) {
      const image = new Image()
      image.src = property.imageUrl
    }
  })
}

function scheduleHidePropertyCard() {
  cancelHidePropertyCard()
  hideTimer.value = window.setTimeout(() => {
    hoveredProperty.value = null
  }, 160)
}

function cancelHidePropertyCard() {
  if (hideTimer.value) {
    window.clearTimeout(hideTimer.value)
    hideTimer.value = null
  }
}

function goToDetail(property) {
  router.push(`/properties/${property.id}`)
}

function getRoomCount(layout) {
  const text = String(layout || '')
  const digitMatch = text.match(/(\d+)\s*室/)
  if (digitMatch) {
    return Number(digitMatch[1])
  }
  const chineseMatch = text.match(/([一二两三四五六七八九])\s*室/)
  const roomMap = {
    一: 1,
    二: 2,
    两: 2,
    三: 3,
    四: 4,
    五: 5,
    六: 6,
    七: 7,
    八: 8,
    九: 9
  }
  return chineseMatch ? roomMap[chineseMatch[1]] : null
}

function startDrag(event) {
  if (event.target.closest('button, .map-hover-card, .atlas-filter')) {
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
  updateDragBounds()
  centerMap()
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
  mapSize.width = layer.offsetWidth
  mapSize.height = layer.offsetHeight
  mapOffset.x = clamp(mapOffset.x, dragBounds.minX, dragBounds.maxX)
  mapOffset.y = clamp(mapOffset.y, dragBounds.minY, dragBounds.maxY)
}

function centerMap() {
  const canvas = mapCanvasRef.value
  const layer = mapLayerRef.value
  if (!canvas || !layer) {
    return
  }
  const centeredX = (canvas.clientWidth - layer.offsetWidth) / 2
  const centeredY = (canvas.clientHeight - layer.offsetHeight) / 2
  mapOffset.x = clamp(centeredX, dragBounds.minX, dragBounds.maxX)
  mapOffset.y = clamp(centeredY, dragBounds.minY, dragBounds.maxY)
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}
</script>
