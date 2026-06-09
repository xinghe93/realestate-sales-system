<template>
  <div class="app-shell">
    <AppNav />
    <RouterView v-slot="{ Component, route }">
      <Transition name="route-fade" mode="out-in" :duration="240">
        <component :is="Component" :key="route.fullPath" />
      </Transition>
    </RouterView>
  </div>
</template>

<script setup>
import AppNav from './components/AppNav.vue'
</script>
