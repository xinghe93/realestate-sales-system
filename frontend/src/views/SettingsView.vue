<template>
  <main class="workspace-page">
    <PageHeader
      kicker="User Center"
      title="用户中心"
      description="维护个人联系方式，管理员可查看和管理系统用户。"
    >
      <template #actions>
        <el-button type="danger" plain :icon="SwitchButton" @click="signOut">退出登录</el-button>
      </template>
    </PageHeader>

    <section class="settings-console">
      <article class="settings-block profile-block">
        <div class="profile-summary-card">
          <span class="profile-avatar">{{ profileInitial }}</span>
          <div>
            <span class="profile-kicker">{{ isAdmin ? 'Administrator' : 'Buyer Account' }}</span>
            <h2>{{ profile.realName || profile.username }}</h2>
            <p>{{ roleLabel(profile.role || session.user?.role) }}</p>
          </div>
        </div>

        <div class="profile-editor">
          <div class="block-title">
            <span>Profile</span>
            <h2>个人信息</h2>
          </div>
          <el-form label-position="top" class="profile-form" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="profile.username" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="profile.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profile.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profile.email" />
            </el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
          </el-form>
        </div>
      </article>
    </section>

    <section v-if="isAdmin" class="user-ledger">
      <div class="block-title">
        <span>User Ledger</span>
        <h2>用户账号管理</h2>
      </div>
      <div v-loading="loadingUsers" class="ledger-list">
        <article v-for="user in users" :key="user.id" class="ledger-row">
          <div>
            <h3>{{ user.realName || user.username }}</h3>
            <p>{{ user.username }} · {{ user.phone || '未填写电话' }} · {{ user.email || '未填写邮箱' }}</p>
          </div>
          <span class="role-tag">{{ roleLabel(user.role) }}</span>
          <span class="status-badge" :class="user.status === 'ACTIVE' ? 'is-published' : 'is-offline'">
            {{ user.status === 'ACTIVE' ? '启用' : '禁用' }}
          </span>
          <span class="failed-count">失败 {{ user.failedLoginCount || 0 }} 次</span>
          <el-button
            link
            type="danger"
            :disabled="user.role === 'ADMIN' || user.status !== 'ACTIVE'"
            @click="disable(user)"
          >
            {{ user.status === 'ACTIVE' ? '禁用' : '已禁用' }}
          </el-button>
        </article>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SwitchButton } from '@element-plus/icons-vue'
import PageHeader from '../components/PageHeader.vue'
import { logout } from '../api/auth'
import { disableUser, getProfile, getUsers, updateProfile } from '../api/users'
import { clearSession, session } from '../stores/session'
import { sampleUsers } from '../data/mockData'
import { roleLabel } from '../utils/formatters'

const router = useRouter()
const savingProfile = ref(false)
const loadingUsers = ref(false)
const users = ref([])

const profile = reactive({
  username: session.user?.username || 'buyer',
  realName: session.user?.realName || '普通购房用户',
  phone: session.user?.phone || '139-0000-0002',
  email: session.user?.email || 'buyer@example.com',
  role: session.user?.role || 'USER'
})

const isAdmin = computed(() => (profile.role || session.user?.role) === 'ADMIN')
const profileInitial = computed(() => (profile.realName || profile.username || '用').slice(0, 1))

onMounted(() => {
  loadProfile()
})

async function loadProfile() {
  try {
    const response = await getProfile()
    if (response.data && typeof response.data === 'object') {
      Object.assign(profile, response.data)
    }
  } catch {
    // 演示模式使用默认资料。
  } finally {
    if (isAdmin.value) {
      loadUsers()
    }
  }
}

async function loadUsers() {
  loadingUsers.value = true
  try {
    const response = await getUsers()
    users.value = Array.isArray(response.data) ? response.data : [...sampleUsers]
  } catch {
    users.value = [...sampleUsers]
  } finally {
    loadingUsers.value = false
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await updateProfile({
      realName: profile.realName,
      phone: profile.phone,
      email: profile.email
    })
    ElMessage.success('个人资料已保存')
  } catch {
    ElMessage.info('后端未连接，当前为页面演示状态')
  } finally {
    savingProfile.value = false
  }
}

async function disable(user) {
  try {
    await disableUser(user.id)
  } catch {
    // 演示模式下直接更新本地状态。
  }
  user.status = 'DISABLED'
  ElMessage.success('用户已禁用')
}

async function signOut() {
  try {
    await logout()
  } catch {
    // 未连接后端时仍清理前端状态。
  }
  clearSession()
  router.push('/login')
}
</script>
