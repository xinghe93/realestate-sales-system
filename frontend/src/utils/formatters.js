export function moneyWan(value) {
  const number = Number(value || 0)
  return `${number.toLocaleString()}万`
}

export function squareMeters(value) {
  return `${Number(value || 0).toLocaleString()}㎡`
}

export function unitPrice(property) {
  const price = Number(property?.price || 0)
  const area = Number(property?.area || 1)
  return `${Math.round((price * 10000) / area).toLocaleString()}元/㎡`
}

export function statusLabel(status) {
  return status === 'OFFLINE' ? '已下架' : '已发布'
}

export function roleLabel(role) {
  return role === 'ADMIN' ? '管理员' : '普通用户'
}
