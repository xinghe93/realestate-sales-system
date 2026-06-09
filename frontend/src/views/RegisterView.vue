<template>
  <main class="auth-page">
    <section class="auth-stage register-stage">
      <div class="auth-poster">
        <span class="section-kicker">Buyer Gate</span>
        <h1>注册购房用户</h1>
        <p>注册后可浏览已发布房源、收藏心仪房源，并联系发布管理员。</p>
        <div class="auth-tags">
          <span>浏览房源</span>
          <span>收藏房源</span>
          <span>维护资料</span>
        </div>
      </div>

      <el-form class="gate-form register-gate" label-position="top" @submit.prevent>
        <div class="gate-head">
          <h2>创建账号</h2>
          <RouterLink to="/login">返回登录</RouterLink>
        </div>
        <div class="gate-grid">
          <el-form-item label="用户名">
            <el-input v-model="form.username" size="large" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password size="large" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="form.realName" size="large" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" size="large" />
          </el-form-item>
          <el-form-item label="邮箱" class="editor-wide">
            <el-input v-model="form.email" size="large" />
          </el-form-item>
        </div>
        <el-button class="wide-action" type="primary" size="large" :loading="submitting" @click="submitRegister">
          注册普通用户
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: ''
})

async function submitRegister() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  submitting.value = true
  try {
    await register({ ...form })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    submitting.value = false
  }
}
</script>
