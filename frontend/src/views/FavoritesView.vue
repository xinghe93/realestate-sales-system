<template>
  <main class="workspace-page">
    <PageHeader
      kicker="Buyer Collection"
      title="我的收藏"
      description="保留心仪的已发布房源，快速查看发布管理员电话。"
    >
      <template #actions>
        <el-button :icon="Refresh" @click="loadFavorites">刷新收藏</el-button>
      </template>
    </PageHeader>

    <section class="collection-layout">
      <aside class="collection-summary">
        <MetricTile label="收藏房源" :value="cards.length" hint="已下架房源不展示" />
        <div class="collection-note">
          <span>Contact Path</span>
          <p>用户看中心仪房源后，可以收藏，也可以直接拨打发布管理员电话。</p>
        </div>
      </aside>

      <section v-loading="loading" class="collection-grid">
        <PropertyCard v-for="item in cards" :key="item.id" :property="item.property">
          <template #actions="{ property }">
            <el-button type="primary" :icon="Phone">联系管理员</el-button>
            <el-button :icon="StarFilled" @click="remove(item)">取消收藏</el-button>
            <RouterLink to="/">
              <el-button text>回到地图</el-button>
            </RouterLink>
          </template>
        </PropertyCard>
        <el-empty v-if="!loading && cards.length === 0" description="暂无收藏房源">
          <RouterLink to="/">
            <el-button type="primary">去地图浏览</el-button>
          </RouterLink>
        </el-empty>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Phone, Refresh, StarFilled } from '@element-plus/icons-vue'
import PageHeader from '../components/PageHeader.vue'
import MetricTile from '../components/MetricTile.vue'
import PropertyCard from '../components/PropertyCard.vue'
import { getFavorites, removeFavorite } from '../api/favorites'
import { normalizeProperty } from '../composables/usePropertyCatalog'
import { sampleFavorites } from '../data/mockData'

const loading = ref(false)
const favorites = ref([])

const cards = computed(() => favorites.value
  .map((item, index) => ({
    ...item,
    property: normalizeProperty(item.property || item, index)
  }))
  .filter((item) => item.property.status === 'PUBLISHED'))

onMounted(loadFavorites)

async function loadFavorites() {
  loading.value = true
  try {
    const response = await getFavorites()
    favorites.value = Array.isArray(response.data) ? response.data : [...sampleFavorites]
  } catch {
    favorites.value = [...sampleFavorites]
  } finally {
    loading.value = false
  }
}

async function remove(item) {
  try {
    await removeFavorite(item.propertyId || item.property.id)
  } catch {
    // 演示模式下直接移除本地收藏。
  }
  favorites.value = favorites.value.filter((favorite) => favorite.id !== item.id)
  ElMessage.success('已取消收藏')
}
</script>
