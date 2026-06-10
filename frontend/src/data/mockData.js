export const sampleProperties = [
  {
    id: 1,
    title: '保利 · 天汇',
    region: '建邺区',
    address: '河西中部',
    layout: '3室2厅2卫',
    area: 143,
    price: 860,
    imageUrl: '/property-images/interior-river-suite.png',
    mapX: 76,
    mapY: 43,
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
    imageUrl: '/property-images/interior-city-lounge.png',
    mapX: 39,
    mapY: 30,
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
    imageUrl: '/property-images/interior-cozy-flat.png',
    mapX: 61,
    mapY: 28,
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
    imageUrl: '/property-images/interior-luxury-flat.png',
    mapX: 35,
    mapY: 88,
    status: 'OFFLINE',
    contactName: '赵管理员',
    contactPhone: '135-0000-3219',
    description: '核心区大平层，已售下架，后台留档。'
  },
  {
    id: 5,
    title: '奥体滨河四居',
    region: '建邺区',
    address: '奥体东路',
    layout: '四室两厅',
    area: 156,
    price: 920,
    imageUrl: '/property-images/interior-river-suite.png',
    mapX: 54,
    mapY: 54,
    status: 'PUBLISHED',
    contactName: '林管理员',
    contactPhone: '138-0000-8912',
    description: '靠近奥体商圈，双阳台设计，适合改善型家庭。'
  },
  {
    id: 6,
    title: '湖畔简约两居',
    region: '西湖区',
    address: '文二西路',
    layout: '两室两厅',
    area: 88,
    price: 430,
    imageUrl: '/property-images/interior-cozy-flat.png',
    mapX: 47,
    mapY: 68,
    status: 'PUBLISHED',
    contactName: '陈管理员',
    contactPhone: '136-0000-7721',
    description: '简约装修，动线紧凑，临近湖畔生活圈。'
  },
  {
    id: 7,
    title: '江景日出跃层',
    region: '滨江区',
    address: '闻涛路',
    layout: '三室两厅',
    area: 132,
    price: 760,
    imageUrl: '/property-images/interior-city-lounge.png',
    mapX: 28,
    mapY: 47,
    status: 'PUBLISHED',
    contactName: '周管理员',
    contactPhone: '137-0000-4510',
    description: '高区视野开阔，客厅挑高，适合品质居住。'
  },
  {
    id: 8,
    title: '城东花园洋房',
    region: '上城区',
    address: '艮山西路',
    layout: '三室两厅',
    area: 126,
    price: 650,
    imageUrl: '/property-images/interior-luxury-flat.png',
    mapX: 68,
    mapY: 74,
    status: 'OFFLINE',
    contactName: '赵管理员',
    contactPhone: '135-0000-3219',
    description: '低密洋房，花园景观，下架后用于后台案例展示。'
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
