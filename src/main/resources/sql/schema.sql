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

-- admin / admin123
insert into users(username, password_hash, real_name, phone, email, role, status)
values
  ('admin1', '$2a$12$R5eIDTRiUXY42kCqM8Nnhe5gD/nAWBv4Ihl3LpqNeTaXdlwIdroHi', '管理员1', '13800000000', 'admin@example.com', 'ADMIN', 'ACTIVE');

-- buyer / user123
insert into users(username, password_hash, real_name, phone, email, role, status)
values
  ('user1', '$2a$12$jWBEBJLCaFhPOh8bjy9PAe2P57QZzogmHqOizDg4130rBac/lkdIO', '普通用户1', '13900000000', 'user@example.com', 'USER', 'ACTIVE');

insert into properties(title, region, address, layout, price, area, image_url, description, status, created_by)
values
  ('滨江观景三居', '滨江区', '滨江区江南大道 188 号', '三室两厅', 5200000.00, 118.50, '', '近地铁，南北通透，适合改善型居住。', 'PUBLISHED', 1),
  ('城西精装两居', '西湖区', '西湖区文三路 66 号', '两室一厅', 3100000.00, 78.00, '', '精装修，周边学校和商业配套成熟。', 'PUBLISHED', 1),
  ('钱江新城大平层', '上城区', '上城区钱江路 88 号', '四室两厅', 8600000.00, 168.00, '', '该房源已成交，由管理员下架。', 'OFFLINE', 1);
