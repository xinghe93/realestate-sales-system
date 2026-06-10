<template>
  <main class="workspace-page">
    <section v-loading="loading" class="property-detail-layout">
      <article v-if="property" class="property-detail-main">
        <RouterLink class="back-link" :to="backTarget">
          <ArrowLeft />
          {{ backText }}
        </RouterLink>
        <div class="detail-topline">
          <span>{{ property.region }}</span>
          <StatusBadge :property-status="property.status" />
        </div>
        <h1>{{ property.title }}</h1>
        <p class="detail-address">{{ property.address }} · {{ property.layout }} · {{ squareMeters(property.area) }}</p>
        <div class="detail-price">
          <strong>{{ moneyWan(property.price) }}</strong>
          <span>{{ unitPrice(property) }}</span>
        </div>
        <figure v-if="property.imageUrl" class="detail-hero-image">
          <img :src="property.imageUrl" :alt="property.title" />
        </figure>
        <p class="detail-description">{{ property.description }}</p>
        <div class="detail-contact">
          <span>发布管理员</span>
          <strong>{{ property.contactName }}</strong>
          <a :href="`tel:${property.contactPhone}`">
            <Phone />
            {{ property.contactPhone }}
          </a>
        </div>
        <div class="detail-actions">
          <el-button type="primary" :icon="Phone">联系管理员</el-button>
          <el-button :icon="Star" @click="favoriteProperty">收藏</el-button>
        </div>
      </article>

      <aside v-if="property" class="property-detail-map">
        <div ref="detailMapFrameRef" class="detail-map-layer" aria-label="房源地图位置">
          <div class="detail-map-content" :style="detailMapContentStyle">
            <img :src="mapImage" alt="" draggable="false" />
            <span
              class="detail-map-pin"
              :style="{ left: `${property.mapX}%`, top: `${property.mapY}%` }"
              :aria-label="`${property.title}地图位置`"
            />
          </div>
        </div>
      </aside>

      <el-empty v-if="!loading && !property" description="房源不存在">
        <RouterLink :to="backTarget">
          <el-button type="primary">{{ backText }}</el-button>
        </RouterLink>
      </el-empty>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Phone, Star } from '@element-plus/icons-vue'
import StatusBadge from '../components/StatusBadge.vue'
import { getProperty } from '../api/properties'
import { addFavorite } from '../api/favorites'
import { normalizeProperty } from '../composables/usePropertyCatalog'
import { sampleProperties } from '../data/mockData'
import { session } from '../stores/session'
import { moneyWan, squareMeters, unitPrice } from '../utils/formatters'
import mapImage from '../assets/city-map-home.png'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const property = ref(null)
const detailMapFrameRef = ref(null)
const detailMapSize = ref({ width: 0, height: 0 })
let detailMapObserver = null
const backTarget = computed(() => route.query.from === 'list' ? '/properties' : '/')
const backText = computed(() => route.query.from === 'list' ? '返回列表' : '返回地图')
const detailMapContentStyle = computed(() => {
  if (!property.value || !detailMapSize.value.width || !detailMapSize.value.height) {
    return {}
  }
  const mapRatio = 16 / 9
  const zoom = 1.65
  const frameWidth = detailMapSize.value.width
  const frameHeight = detailMapSize.value.height
  const frameRatio = frameWidth / frameHeight
  const baseWidth = frameRatio > mapRatio ? frameWidth : frameHeight * mapRatio
  const baseHeight = frameRatio > mapRatio ? frameWidth / mapRatio : frameHeight
  const mapWidth = baseWidth * zoom
  const mapHeight = baseHeight * zoom
  const left = clamp(frameWidth / 2 - (mapWidth * Number(property.value.mapX || 50)) / 100, frameWidth - mapWidth, 0)
  const top = clamp(frameHeight / 2 - (mapHeight * Number(property.value.mapY || 50)) / 100, frameHeight - mapHeight, 0)
  return {
    width: `${mapWidth}px`,
    height: `${mapHeight}px`,
    transform: `translate(${left}px, ${top}px)`
  }
})

onMounted(async () => {
  await loadProperty()
  await nextTick()
  bindDetailMapObserver()
})

onBeforeUnmount(() => {
  detailMapObserver?.disconnect()
})

async function loadProperty() {
  loading.value = true
  try {
    const response = await getProperty(route.params.id)
    property.value = normalizeProperty(response.data)
  } catch {
    const fallback = sampleProperties.find((item) => String(item.id) === String(route.params.id))
    property.value = fallback ? normalizeProperty(fallback) : null
  } finally {
    loading.value = false
  }
}

async function favoriteProperty() {
  if (!session.user) {
    router.push('/login')
    return
  }
  if (!property.value?.id) {
    ElMessage.warning('房源信息未加载')
    return
  }
  try {
    await addFavorite(property.value.id)
    ElMessage.success('已加入收藏')
  } catch {
    ElMessage.error('收藏失败，请稍后重试')
  }
}

function bindDetailMapObserver() {
  updateDetailMapSize()
  if (!detailMapFrameRef.value || typeof ResizeObserver === 'undefined') {
    return
  }
  detailMapObserver = new ResizeObserver(updateDetailMapSize)
  detailMapObserver.observe(detailMapFrameRef.value)
}

function updateDetailMapSize() {
  const rect = detailMapFrameRef.value?.getBoundingClientRect()
  if (!rect) {
    return
  }
  detailMapSize.value = {
    width: rect.width,
    height: rect.height
  }
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value))
}
</script>
