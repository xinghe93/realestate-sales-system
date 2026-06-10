import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import PropertyListView from '../views/PropertyListView.vue'
import PropertyDetailView from '../views/PropertyDetailView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import SettingsView from '../views/SettingsView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/properties', name: 'properties', component: PropertyListView },
  { path: '/properties/:id', name: 'property-detail', component: PropertyDetailView },
  { path: '/favorites', name: 'favorites', component: FavoritesView },
  { path: '/settings', name: 'settings', component: SettingsView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
