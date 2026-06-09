<template>
  <article class="property-card">
    <div class="property-art">
      <span>{{ property.region }}</span>
    </div>
    <div class="property-card-content">
      <div class="property-card-head">
        <div>
          <h2>{{ property.title }}</h2>
          <p>{{ property.address }}</p>
        </div>
        <StatusBadge :property-status="property.status" />
      </div>

      <div class="property-specs">
        <span>{{ property.layout }}</span>
        <span>{{ squareMeters(property.area) }}</span>
        <span>{{ unitPrice(property) }}</span>
      </div>

      <div class="property-card-price">
        <strong>{{ moneyWan(property.price) }}</strong>
        <small>{{ property.description }}</small>
      </div>

      <div class="property-contact">
        <div>
          <span>发布管理员</span>
          <strong>{{ property.contactName }}</strong>
        </div>
        <a :href="`tel:${property.contactPhone}`">
          <Phone />
          {{ property.contactPhone }}
        </a>
      </div>

      <div class="property-card-actions">
        <slot name="actions" :property="property">
          <el-button type="primary" :icon="Phone">联系管理员</el-button>
          <el-button :icon="Star">收藏</el-button>
        </slot>
      </div>
    </div>
  </article>
</template>

<script setup>
import { Phone, Star } from '@element-plus/icons-vue'
import StatusBadge from './StatusBadge.vue'
import { moneyWan, squareMeters, unitPrice } from '../utils/formatters'

defineProps({
  property: {
    type: Object,
    required: true
  }
})
</script>
