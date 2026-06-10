<template>
  <main class="workspace-page">
    <PageHeader
      kicker="Inventory Console"
      title="房源列表"
    >
      <template #actions>
        <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="openCreate">新增房源</el-button>
      </template>
    </PageHeader>

    <section class="console-layout">
      <aside class="console-rail">
        <MetricTile label="房源总数" :value="visibleProperties.length" hint="完整房源" />
        <MetricTile label="已发布" :value="published.length" hint="可售房源" tone="green" />
        <MetricTile label="已下架" :value="offline.length" hint="已售房源" tone="red" />
      </aside>

      <section class="console-main">
        <div class="control-strip">
          <el-input v-model="filters.keyword" placeholder="搜索房源、区域、地址" :prefix-icon="Search" clearable />
          <el-select v-model="filters.region" placeholder="区域" clearable>
            <el-option v-for="region in regions" :key="region" :label="region" :value="region" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态" clearable>
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </div>

        <div class="inventory-list" v-loading="loading">
          <div class="inventory-head" :class="{ 'is-user': !isAdmin }">
            <span>房源信息</span>
            <span>价格</span>
            <span>状态</span>
            <span v-if="isAdmin">操作</span>
          </div>
          <article
            v-for="property in filteredProperties"
            :key="property.id"
            class="inventory-row"
            :class="{ 'is-user': !isAdmin }"
            role="button"
            tabindex="0"
            @click="goToDetail(property)"
            @keydown.enter.prevent="goToDetail(property)"
          >
            <div class="inventory-main">
              <span class="region-token">{{ property.region }}</span>
              <div>
                <h2>{{ property.title }}</h2>
                <p>{{ property.address }} · {{ property.layout }} · {{ squareMeters(property.area) }}</p>
                <button class="row-detail-link" type="button" @click.stop="goToDetail(property)">查看详情</button>
              </div>
            </div>
            <div class="inventory-price">
              <strong>{{ moneyWan(property.price) }}</strong>
              <span>{{ unitPrice(property) }}</span>
            </div>
            <StatusBadge :property-status="property.status" />
            <div v-if="isAdmin" class="inventory-actions">
              <el-button link type="primary" @click.stop="openEdit(property)">编辑</el-button>
              <el-button link :type="property.status === 'PUBLISHED' ? 'warning' : 'success'" @click.stop="toggleStatus(property)">
                {{ property.status === 'PUBLISHED' ? '下架' : '发布' }}
              </el-button>
              <el-button link type="danger" @click.stop="removeRow(property)">删除</el-button>
            </div>
          </article>
        </div>
      </section>
    </section>

    <el-dialog
      v-model="dialogVisible"
      class="property-editor-dialog"
      :title="editingId ? '编辑房源' : '新增房源'"
      width="860px"
      transition="property-dialog-pop"
      modal-class="property-editor-overlay"
      :lock-scroll="false"
    >
      <el-form class="editor-form" label-position="top" @submit.prevent>
        <el-form-item label="房源名称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="区域">
          <el-select
            v-model="form.region"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入区域"
          >
            <el-option v-for="region in regions" :key="region" :label="region" :value="region" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-select
            v-model="form.address"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入地址"
          >
            <el-option v-for="address in addresses" :key="address" :label="address" :value="address" />
          </el-select>
        </el-form-item>
        <el-form-item label="户型">
          <el-select
            v-model="form.layout"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入户型"
          >
            <el-option v-for="layout in layouts" :key="layout" :label="layout" :value="layout" />
          </el-select>
        </el-form-item>
        <el-form-item label="总价（万元）">
          <el-input-number v-model="form.price" :min="0" :precision="0" :controls="false" />
        </el-form-item>
        <el-form-item label="面积（㎡）">
          <el-input-number v-model="form.area" :min="0" :precision="1" :controls="false" />
        </el-form-item>
        <el-form-item label="描述" class="editor-wide">
          <el-input v-model="form.description" type="textarea" :rows="3" resize="none" />
        </el-form-item>
        <el-form-item label="地图位置" class="editor-wide">
          <div class="map-picker" @click="selectMapPosition">
            <img :src="mapImage" alt="选择房源地图位置" draggable="false" />
            <span
              v-if="hasMapPosition"
              class="map-picker-pin"
              :style="{ left: `${form.mapX}%`, top: `${form.mapY}%` }"
            >
              {{ form.title || '房源' }}
            </span>
            <strong v-else>点击地图选择房源位置</strong>
          </div>
          <p class="map-picker-meta">
            坐标：{{ hasMapPosition ? `${Number(form.mapX).toFixed(2)}%, ${Number(form.mapY).toFixed(2)}%` : '未选择' }}
          </p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProperty">
          {{ editingId ? '保存' : ('保存并' + (form.status === 'PUBLISHED' ? '发布' : '下架')) }}
        </el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import PageHeader from '../components/PageHeader.vue'
import MetricTile from '../components/MetricTile.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { usePropertyCatalog } from '../composables/usePropertyCatalog'
import { createProperty, deleteProperty, updateProperty, updatePropertyStatus } from '../api/properties'
import { moneyWan, squareMeters, unitPrice } from '../utils/formatters'
import { session } from '../stores/session'
import mapImage from '../assets/city-map-home.png'

const route = useRoute()
const router = useRouter()
const { properties, published, offline, loading, load } = usePropertyCatalog()
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const filters = reactive({
  keyword: route.query.keyword || '',
  region: '',
  status: ''
})

const form = reactive({
  title: '',
  region: '',
  address: '',
  layout: '',
  price: 0,
  area: 0,
  mapX: null,
  mapY: null,
  description: '',
  imageUrl: '',
  status: 'PUBLISHED'
})

const isAdmin = computed(() => session.user?.role === 'ADMIN')
const visibleProperties = computed(() => properties.value)
const regions = computed(() => [...new Set(visibleProperties.value.map((item) => item.region))])
const addresses = computed(() => [...new Set(visibleProperties.value.map((item) => item.address).filter(Boolean))])
const layouts = computed(() => [...new Set(visibleProperties.value.map((item) => item.layout).filter(Boolean))])
const hasMapPosition = computed(() => form.mapX !== null && form.mapY !== null)

const filteredProperties = computed(() => {
  const keyword = String(filters.keyword || '').trim().toLowerCase()
  return visibleProperties.value.filter((property) => {
    const hitKeyword = !keyword || [property.title, property.region, property.address, property.layout]
      .some((value) => String(value || '').toLowerCase().includes(keyword))
    const hitRegion = !filters.region || property.region === filters.region
    const hitStatus = !filters.status || property.status === filters.status
    return hitKeyword && hitRegion && hitStatus
  })
})

onMounted(load)

function resetFilters() {
  filters.keyword = ''
  filters.region = ''
  filters.status = ''
}

function goToDetail(property) {
  router.push({
    name: 'property-detail',
    params: { id: property.id },
    query: { from: 'list' }
  })
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    title: '',
    region: '',
    address: '',
    layout: '',
    price: 0,
    area: 0,
    mapX: null,
    mapY: null,
    description: '',
    imageUrl: '',
    status: 'PUBLISHED'
  })
  dialogVisible.value = true
}

function openEdit(property) {
  editingId.value = property.id
  Object.assign(form, {
    title: property.title,
    region: property.region,
    address: property.address,
    layout: property.layout,
    price: property.price,
    area: property.area,
    mapX: property.mapX,
    mapY: property.mapY,
    description: property.description,
    imageUrl: property.imageUrl || '',
    status: property.status || 'PUBLISHED'
  })
  dialogVisible.value = true
}

function selectMapPosition(event) {
  const rect = event.currentTarget.getBoundingClientRect()
  form.mapX = clampPercent(((event.clientX - rect.left) / rect.width) * 100)
  form.mapY = clampPercent(((event.clientY - rect.top) / rect.height) * 100)
}

async function saveProperty() {
  if (!form.title || !form.region || !form.price) {
    ElMessage.warning('请填写房源名称、区域和总价')
    return
  }
  if (!hasMapPosition.value) {
    ElMessage.warning('请在地图上选择房源位置')
    return
  }
  saving.value = true
  const payload = {
    title: form.title,
    region: form.region,
    address: form.address,
    layout: form.layout,
    price: form.price,
    area: form.area,
    mapX: form.mapX,
    mapY: form.mapY,
    description: form.description,
    imageUrl: form.imageUrl || null,
    status: form.status
  }
  const statusLabel = form.status === 'PUBLISHED' ? '已发布' : '已下架'
  try {
    if (editingId.value) {
      await updateProperty(editingId.value, payload)
      const target = properties.value.find((item) => item.id === editingId.value)
      Object.assign(target, payload)
    } else {
      const response = await createProperty(payload)
      properties.value.unshift({
        ...payload,
        id: response.data?.id || Date.now(),
        contactName: '当前管理员',
        contactPhone: '待补充'
      })
    }
    dialogVisible.value = false
    ElMessage.success(`房源已保存（${statusLabel}）`)
  } catch {
    ElMessage.info('后端未连接，页面已保留当前演示状态')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(property) {
  const nextStatus = property.status === 'PUBLISHED' ? 'OFFLINE' : 'PUBLISHED'
  try {
    await updatePropertyStatus(property.id, nextStatus)
    property.status = nextStatus
    ElMessage.success(nextStatus === 'PUBLISHED' ? '已发布' : '已下架')
  } catch {
    ElMessage.error('状态更新失败')
  }
}

async function removeRow(property) {
  await ElMessageBox.confirm(`确定删除「${property.title}」吗？`, '删除房源', { type: 'warning' })
  try {
    await deleteProperty(property.id)
    properties.value = properties.value.filter((item) => item.id !== property.id)
    ElMessage.success('房源已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}

function clampPercent(value) {
  return Math.max(0, Math.min(100, Number(value.toFixed(2))))
}
</script>
