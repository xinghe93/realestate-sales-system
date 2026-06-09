export const sampleProperties = [
  {
    id: 1,
    title: '保利 · 天汇',
    region: '建邺区',
    address: '河西中部',
    layout: '3室2厅2卫',
    area: 143,
    price: 860,
    status: 'PUBLISHED',
    contactName: '林管理员',
    contactPhone: '138-0000-8912',
    description: '南北通透，河西成熟商圈，近地铁与学校配套。'
  },
  {
    id: 2,
    title: '滨江观景三居',
    region: '滨江区',
    address: '江南大道',
    layout: '三室两厅',
    area: 118,
    price: 520,
    status: 'PUBLISHED',
    contactName: '周管理员',
    contactPhone: '137-0000-4510',
    description: '高层江景，精装交付，适合改善型家庭。'
  },
  {
    id: 3,
    title: '城西精装两居',
    region: '西湖区',
    address: '文三路',
    layout: '两室一厅',
    area: 78,
    price: 310,
    status: 'PUBLISHED',
    contactName: '陈管理员',
    contactPhone: '136-0000-7721',
    description: '低总价精装两居，周边生活氛围成熟。'
  },
  {
    id: 4,
    title: '钱江新城大平层',
    region: '上城区',
    address: '市民中心旁',
    layout: '四室两厅',
    area: 168,
    price: 1180,
    status: 'OFFLINE',
    contactName: '赵管理员',
    contactPhone: '135-0000-3219',
    description: '核心区大平层，已售下架，后台留档。'
  }
]

export const sampleFavorites = sampleProperties
  .filter((item) => item.status === 'PUBLISHED')
  .slice(0, 2)
  .map((property, index) => ({
    id: index + 1,
    propertyId: property.id,
    property,
    createdAt: '2026-06-09T10:00:00'
  }))

export const sampleUsers = [
  {
    id: 1,
    username: 'admin',
    realName: '系统管理员',
    phone: '138-0000-0001',
    email: 'admin@example.com',
    role: 'ADMIN',
    status: 'ACTIVE',
    failedLoginCount: 0,
    lockedUntil: null
  },
  {
    id: 2,
    username: 'buyer',
    realName: '普通购房用户',
    phone: '139-0000-0002',
    email: 'buyer@example.com',
    role: 'BUYER',
    status: 'ACTIVE',
    failedLoginCount: 1,
    lockedUntil: null
  }
]
