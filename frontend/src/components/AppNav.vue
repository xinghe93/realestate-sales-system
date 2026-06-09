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
      <RouterLink class="icon-pill" to="/favorites" aria-label="收藏">
        <Star />
        <span>收藏</span>
      </RouterLink>
      <RouterLink class="user-pill" :to="session.user ? '/settings' : '/login'">
        <span class="user-orb"></span>
        <span>{{ userName }}</span>
      </RouterLink>
    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  List,
  Location,
  OfficeBuilding,
  Search,
  Star
} from '@element-plus/icons-vue'
import { session } from '../stores/session'

const router = useRouter()
const keyword = ref('')

const navItems = [
  { path: '/', label: '房源地图', icon: Location },
  { path: '/properties', label: '房源列表', icon: List }
]

const userName = computed(() => session.user?.realName || session.user?.username || '登录')

function search() {
  router.push({
    path: '/properties',
    query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {}
  })
}
</script>
