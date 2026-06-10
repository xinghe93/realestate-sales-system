import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/base.css'
import App from './App.vue'
import router from './router'
import { loadSession } from './stores/session'

loadSession()

createApp(App)
  .use(router)
  .use(ElementPlus)
  .mount('#app')
