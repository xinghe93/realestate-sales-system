<template>
  <main class="auth-page">
    <section class="auth-stage">
      <div class="auth-poster">
        <span class="section-kicker">Estate Gate</span>
        <h1>登录房产销售系统</h1>
        <p>进入地图浏览、收藏房源、后台管理和安全配置。</p>
        <div class="auth-tags">
          <span>BCrypt 加盐</span>
          <span>验证码触发</span>
          <span>Session 权限</span>
        </div>
      </div>

      <el-form class="gate-form" label-position="top" @submit.prevent>
        <div class="gate-head">
          <h2>账号登录</h2>
          <RouterLink to="/register">注册购房用户</RouterLink>
        </div>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            size="large"
            @keyup.enter="submitLogin"
          />
        </el-form-item>
        <el-form-item v-if="captcha.question" label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captcha" :placeholder="captcha.question" size="large" />
            <el-button size="large" @click="loadCaptcha">换一题</el-button>
          </div>
        </el-form-item>
        <button class="quiet-action" type="button" @click="loadCaptcha">登录失败多次时需要验证码</button>
        <el-button class="wide-action" type="primary" size="large" :loading="submitting" @click="submitLogin">
          登录系统
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, login } from '../api/auth'
import { setSessionUser } from '../stores/session'

const router = useRouter()
const submitting = ref(false)
const form = reactive({ username: '', password: '', captcha: '' })
const captcha = reactive({ question: '' })

async function loadCaptcha() {
  try {
    const response = await getCaptcha()
    captcha.question = response.data?.question || ''
    form.captcha = ''
  } catch (error) {
    if (import.meta.env.DEV && isBackendUnavailable(error)) {
      ElMessage.warning('未连接后端，无法加载验证码')
      return
    }
    ElMessage.error('验证码加载失败')
  }
}

async function submitLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  submitting.value = true
  try {
    const response = await login({
      username: form.username,
      password: form.password,
      captcha: form.captcha
    })
    setSessionUser(response.data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    const message = error.response?.data?.message || ''
    if (message.includes('验证码')) {
      await loadCaptcha()
      return
    }
    if (import.meta.env.DEV && isBackendUnavailable(error)) {
      ElMessage.warning('未连接后端，无法登录')
      return
    }
    ElMessage.error(message || '登录失败，请检查账号或后端服务')
  } finally {
    submitting.value = false
  }
}

function isBackendUnavailable(error) {
  const status = error.response?.status
  const message = error.message || ''
  return !status || status === 404 || status >= 500 || message.includes('Network Error') || message.includes('timeout')
}
</script>
