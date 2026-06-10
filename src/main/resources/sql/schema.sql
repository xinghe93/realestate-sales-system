create database if not exists realestate_sales_system
  default character set utf8mb4
  collate utf8mb4_0900_ai_ci;

use realestate_sales_system;

drop table if exists favorites;
drop table if exists properties;
drop table if exists users;

create table users (
  id bigint primary key auto_increment,
  username varchar(50) not null unique,
  password_hash varchar(100) not null,
  real_name varchar(50),
  phone varchar(30),
  email varchar(100),
  role enum('USER', 'ADMIN') not null default 'USER',
  status enum('ACTIVE', 'DISABLED') not null default 'ACTIVE',
  failed_login_count int not null default 0,
  locked_until datetime null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp
) engine=InnoDB default charset=utf8mb4;

create table properties (
  id bigint primary key auto_increment,
  title varchar(120) not null,
  region varchar(50) not null,
  address varchar(200),
  layout varchar(50) not null,
  price decimal(14, 2) not null,
  area decimal(10, 2) not null,
  map_x decimal(5, 2) not null default 50.00,
  map_y decimal(5, 2) not null default 50.00,
  image_url varchar(500),
  description text,
  status enum('OFFLINE', 'PUBLISHED') not null default 'PUBLISHED',
  created_by bigint,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint fk_properties_created_by foreign key (created_by) references users(id)
    on update cascade on delete set null,
  index idx_properties_filter (status, region, layout, price)
) engine=InnoDB default charset=utf8mb4;

create table favorites (
  id bigint primary key auto_increment,
  user_id bigint not null,
  property_id bigint not null,
  created_at datetime not null default current_timestamp,
  unique key uk_favorites_user_property (user_id, property_id),
  constraint fk_favorites_user foreign key (user_id) references users(id)
    on update cascade on delete cascade,
  constraint fk_favorites_property foreign key (property_id) references properties(id)
    on update cascade on delete cascade
) engine=InnoDB default charset=utf8mb4;

insert into properties(title, region, address, layout, price, area, map_x, map_y, image_url, description, status, created_by)
values
  ('保利 · 天汇', '建邺区', '建邺区河西中部', '3室2厅2卫', 860.00, 143.00, 60.00, 50.00, '/property-images/interior-river-suite.png', '南北通透，河西成熟商圈，近地铁与学校配套。', 'PUBLISHED', 1),
  ('滨江观景三居', '滨江区', '滨江区江南大道 188 号', '三室两厅', 520.00, 118.50, 64.00, 62.00, '/property-images/interior-city-lounge.png', '近地铁，南北通透，适合改善型居住。', 'PUBLISHED', 1),
  ('城西精装两居', '西湖区', '西湖区文三路 66 号', '两室一厅', 310.00, 78.00, 25.00, 65.00, '/property-images/interior-cozy-flat.png', '精装修，周边学校和商业配套成熟。', 'PUBLISHED', 1),
  ('钱江新城大平层', '上城区', '上城区钱江路 88 号', '四室两厅', 1180.00, 168.00, 75.00, 58.00, '/property-images/interior-luxury-flat.png', '该房源已成交，由管理员下架。', 'OFFLINE', 1),
  ('奥体滨河四居', '建邺区', '建邺区奥体东路 9 号', '四室两厅', 920.00, 156.00, 35.00, 33.00, '/property-images/interior-river-suite.png', '靠近奥体商圈，双阳台设计，适合改善型家庭。', 'PUBLISHED', 1),
  ('湖畔简约两居', '西湖区', '西湖区文二西路 118 号', '两室两厅', 430.00, 88.00, 51.00, 80.00, '/property-images/interior-cozy-flat.png', '简约装修，动线紧凑，临近湖畔生活圈。', 'PUBLISHED', 1),
  ('江景日出跃层', '滨江区', '滨江区闻涛路 299 号', '三室两厅', 760.00, 132.00, 71.00, 24.00, '/property-images/interior-city-lounge.png', '高区视野开阔，客厅挑高，适合品质居住。', 'PUBLISHED', 1),
  ('城东花园洋房', '上城区', '上城区艮山西路 36 号', '三室两厅', 650.00, 126.00, 80.00, 43.00, '/property-images/interior-luxury-flat.png', '低密洋房，花园景观，下架后用于后台案例展示。', 'OFFLINE', 1);
