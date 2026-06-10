<template>
  <header class="app-nav">
    <RouterLink class="nav-brand" to="/">
      <span class="brand-symbol">
        <OfficeBuilding />
      </span>
      <span>
        <strong>房产销售系统</strong>
        <small>Estate Atlas</small>
      </span>
    </RouterLink>

    <nav class="nav-menu">
      <RouterLink v-for="item in navItems" :key="item.path" :to="item.path" class="nav-item">
        <component :is="item.icon" />
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>

    <div class="nav-tools">
      <el-input
        v-model="keyword"
        class="nav-search"
        placeholder="搜索房源 / 区域 / 地铁"
        :prefix-icon="Search"
        @keyup.enter="search"
      />
      <button class="icon-pill" :class="{ 'is-active': isFavoritesRoute }" type="button" aria-label="收藏" @click="openFavorites">
        <Star />
        <span>收藏</span>
      </button>
      <RouterLink class="user-pill" :class="{ 'is-active': isUserRoute }" :to="session.user ? '/settings' : '/login'">
        <span class="user-orb"></span>
        <span>{{ userName }}</span>
      </RouterLink>
    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  List,
  Location,
  OfficeBuilding,
  Search,
  Star
} from '@element-plus/icons-vue'
import { session } from '../stores/session'

const router = useRouter()
const route = useRoute()
const keyword = ref('')

const navItems = [
  { path: '/', label: '房源地图', icon: Location },
  { path: '/properties', label: '房源列表', icon: List }
]

const userName = computed(() => session.user?.realName || session.user?.username || '登录')
const isFavoritesRoute = computed(() => route.path === '/favorites')
const isUserRoute = computed(() => ['/settings', '/login', '/register'].includes(route.path))

function search() {
  router.push({
    path: '/properties',
    query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {}
  })
}

async function openFavorites() {
  if (session.user) {
    router.push('/favorites')
    return
  }
  router.push('/login')
}
</script>
