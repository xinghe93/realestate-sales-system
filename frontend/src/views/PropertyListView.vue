<template>
  <main class="workspace-page">
    <PageHeader
      kicker="Inventory Console"
      title="房源列表"
      description="管理员新建房源后默认发布，已售房源通过下架隐藏给普通购房用户。"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增房源</el-button>
      </template>
    </PageHeader>

    <section class="console-layout">
      <aside class="console-rail">
        <MetricTile label="房源总数" :value="properties.length" hint="后台完整房源池" />
        <MetricTile label="已发布" :value="published.length" hint="普通用户可见" tone="green" />
        <MetricTile label="已下架" :value="offline.length" hint="已售或隐藏" tone="red" />
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
          <article v-for="property in filteredProperties" :key="property.id" class="inventory-row">
            <div class="inventory-main">
              <span class="region-token">{{ property.region }}</span>
              <div>
                <h2>{{ property.title }}</h2>
                <p>{{ property.address }} · {{ property.layout }} · {{ squareMeters(property.area) }}</p>
              </div>
            </div>
            <div class="inventory-price">
              <strong>{{ moneyWan(property.price) }}</strong>
              <span>{{ unitPrice(property) }}</span>
            </div>
            <StatusBadge :property-status="property.status" />
            <div class="inventory-actions">
              <el-button link type="primary" @click="openEdit(property)">编辑</el-button>
              <el-button link :type="property.status === 'PUBLISHED' ? 'warning' : 'success'" @click="toggleStatus(property)">
                {{ property.status === 'PUBLISHED' ? '下架' : '发布' }}
              </el-button>
              <el-button link type="danger" @click="removeRow(property)">删除</el-button>
            </div>
          </article>
        </div>
      </section>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑房源' : '新增房源'" width="720px">
      <el-form class="editor-form" label-position="top" @submit.prevent>
        <el-form-item label="房源名称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="form.region" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="户型">
          <el-input v-model="form.layout" />
        </el-form-item>
        <el-form-item label="总价（万元）">
          <el-input-number v-model="form.price" :min="0" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="面积（㎡）">
          <el-input-number v-model="form.area" :min="0" :precision="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="描述" class="editor-wide">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProperty">保存并发布</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import PageHeader from '../components/PageHeader.vue'
import MetricTile from '../components/MetricTile.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { usePropertyCatalog } from '../composables/usePropertyCatalog'
import { createProperty, deleteProperty, updateProperty, updatePropertyStatus } from '../api/properties'
import { moneyWan, squareMeters, unitPrice } from '../utils/formatters'

const route = useRoute()
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
  description: ''
})

const regions = computed(() => [...new Set(properties.value.map((item) => item.region))])

const filteredProperties = computed(() => {
  const keyword = String(filters.keyword || '').trim().toLowerCase()
  return properties.value.filter((property) => {
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

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    title: '',
    region: '',
    address: '',
    layout: '',
    price: 0,
    area: 0,
    description: ''
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
    description: property.description
  })
  dialogVisible.value = true
}

async function saveProperty() {
  if (!form.title || !form.region || !form.price) {
    ElMessage.warning('请填写房源名称、区域和总价')
    return
  }
  saving.value = true
  const payload = { ...form, status: 'PUBLISHED' }
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
    ElMessage.success('房源已保存并发布')
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
  } catch {
    // 演示模式下直接更新本地状态。
  }
  property.status = nextStatus
  ElMessage.success(nextStatus === 'PUBLISHED' ? '已发布' : '已下架')
}

async function removeRow(property) {
  await ElMessageBox.confirm(`确定删除「${property.title}」吗？`, '删除房源', { type: 'warning' })
  try {
    await deleteProperty(property.id)
  } catch {
    // 演示模式下直接移除本地行。
  }
  properties.value = properties.value.filter((item) => item.id !== property.id)
  ElMessage.success('房源已删除')
}
</script>
