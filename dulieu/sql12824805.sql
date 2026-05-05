-- phpMyAdmin SQL Dump
-- version 4.7.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: sql12.freesqldatabase.com:3306
-- Thời gian đã tạo: Th5 03, 2026 lúc 07:02 PM
-- Phiên bản máy phục vụ: 5.5.62-0ubuntu0.14.04.1
-- Phiên bản PHP: 7.0.33-0ubuntu0.16.04.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `sql12824805`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `invoices`
--

CREATE TABLE `invoices` (
  `id` int(11) NOT NULL,
  `code` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `created_by` int(11) NOT NULL,
  `customer_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `status` enum('PAID','CANCELLED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PAID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `invoices`
--

INSERT INTO `invoices` (`id`, `code`, `created_at`, `created_by`, `customer_name`, `note`, `total_amount`, `status`) VALUES
(1, 'HD596691', '2026-04-29 16:21:00', 2, '', 'PAY=QR', '25000.00', 'PAID'),
(2, 'HD1F4298', '2026-04-29 16:21:08', 2, '', 'PAY=QR', '25000.00', 'PAID'),
(3, 'HD931BB6', '2026-04-29 16:23:35', 2, '', 'PAY=QR', '33000.00', 'PAID'),
(4, 'HD89CAB3', '2026-04-29 16:26:24', 2, '', 'PAY=QR', '20000.00', 'PAID'),
(5, 'HD01E86E', '2026-04-29 16:27:13', 2, '', 'PAY=QR', '30000.00', 'PAID'),
(8, 'HDD86B1C', '2026-04-29 21:40:38', 2, '', 'PAY=CASH', '45000.00', 'PAID'),
(11, 'HD8999AB', '2026-04-29 22:09:45', 2, '', 'PAY=QR', '75000.00', 'PAID'),
(12, 'HDBAF5DD', '2026-04-29 22:52:57', 2, '', 'PAY=QR', '63000.00', 'PAID'),
(13, 'HD6BE6A8', '2026-05-01 06:18:38', 2, '', 'PAY=QR', '40000.00', 'PAID'),
(15, 'HDF4A156', '2026-05-01 07:07:16', 2, '', 'PAY=QR', '95000.00', 'PAID'),
(16, 'HD1CC8C9', '2026-05-01 07:07:48', 2, '', 'PAY=CASH', '115000.00', 'PAID'),
(17, 'HDDB986C', '2026-05-01 07:17:04', 2, '', 'PAY=QR', '45000.00', 'PAID'),
(18, 'HD3A76EC', '2026-05-01 07:17:14', 2, '', 'PAY=CASH', '45000.00', 'PAID'),
(19, 'HDD9A73C', '2026-05-01 07:20:42', 2, '', 'PAY=CASH', '20000.00', 'PAID'),
(20, 'HDAAE7B2', '2026-05-01 07:20:51', 2, '', 'PAY=QR', '20000.00', 'PAID'),
(21, 'HD510D18', '2026-05-01 07:21:59', 2, '', 'PAY=CASH', '15000.00', 'PAID'),
(22, 'HD4844F3', '2026-05-01 09:31:51', 2, '', 'PAY=QR', '54000.00', 'PAID'),
(23, 'HDD28EBD', '2026-05-01 10:01:36', 2, '', 'PAY=QR', '90000.00', 'PAID'),
(24, 'HD251CDE', '2026-05-01 10:04:03', 2, '', 'PAY=QR', '20000.00', 'PAID'),
(25, 'HD2735B5', '2026-05-01 10:32:33', 2, '', 'PAY=QR', '60000.00', 'PAID'),
(26, 'HDFEFD54', '2026-05-01 10:49:12', 2, 'MA1', 'PAY=QR PROMO=', '19600.00', 'PAID'),
(27, 'HDB6C916', '2026-05-01 10:57:34', 2, 'MA1', 'PAY=CASH PROMO=', '19600.00', 'PAID'),
(28, 'HDD7FB83', '2026-05-01 11:12:15', 2, 'TVF0D162', 'PAY=CASH PROMO=', '19600.00', 'PAID'),
(29, 'HD0D0658', '2026-05-01 11:17:11', 2, 'TAI01', 'PAY=CASH PROMO=', '30000.00', 'PAID'),
(30, 'HD010EF6', '2026-05-01 11:20:35', 2, 'TAI01', 'PAY=QR PROMO=', '19600.00', 'PAID'),
(31, 'HD001', '2026-05-01 11:50:14', 1, 'Trần Cao Ti', '', '0.00', 'PAID'),
(35, 'HD88C6F8', '2026-05-02 18:43:01', 2, '', 'PAY=CASH PROMO=GIFT', '20000.00', 'PAID'),
(36, 'HD029A84', '2026-05-02 23:43:00', 2, 'MEM003', 'PAY=QR PROMO=', '49500.00', 'PAID'),
(37, 'HD738A14', '2026-05-02 23:53:27', 2, '', 'PAY=CASH PROMO=', '40000.00', 'PAID'),
(38, 'HD038886', '2026-05-03 13:50:11', 2, '', 'PAY=CASH PROMO=GIFT', '20000.00', 'PAID'),
(39, 'HD9C4068', '2026-05-03 13:58:51', 2, '', 'PAY=CASH PROMO=GIFT', '14000.00', 'PAID'),
(40, 'HDAB034C', '2026-05-03 16:13:05', 2, '', 'PAY=CASH PROMO=GIFT', '52500.00', 'PAID'),
(41, 'HDEBAA69', '2026-05-04 01:29:23', 2, 'MEM003', 'PAY=QR PROMO=', '83700.00', 'PAID'),
(42, 'HD9F1E99', '2026-05-04 01:46:46', 2, 'TV001026', 'PAY=QR PROMO=', '36000.00', 'PAID'),
(43, 'HD0B0001', '2026-05-04 01:48:11', 2, '', 'PAY=CASH PROMO=CHAYNHA', '3500.00', 'PAID'),
(44, 'HDEC3425', '2026-05-04 01:50:38', 2, '', 'PAY=CASH PROMO=', '20000.00', 'PAID'),
(45, 'HDD5955E', '2026-05-04 01:51:51', 3, '', 'PAY=CASH PROMO=', '15000.00', 'PAID'),
(46, 'HDEAF155', '2026-05-04 02:00:41', 2, '', 'PAY=CASH PROMO=', '20000.00', 'PAID');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `invoice_items`
--

CREATE TABLE `invoice_items` (
  `id` int(11) NOT NULL,
  `invoice_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `qty` int(11) NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `line_total` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `invoice_items`
--

INSERT INTO `invoice_items` (`id`, `invoice_id`, `product_id`, `qty`, `unit_price`, `line_total`) VALUES
(1, 1, 7, 1, '10000.00', '10000.00'),
(2, 1, 1, 1, '15000.00', '15000.00'),
(5, 3, 5, 1, '18000.00', '18000.00'),
(6, 3, 3, 1, '15000.00', '15000.00'),
(8, 5, 1, 1, '15000.00', '15000.00'),
(9, 5, 3, 1, '15000.00', '15000.00'),
(10, 2, 7, 1, '10000.00', '10000.00'),
(11, 2, 1, 1, '15000.00', '15000.00'),
(12, 8, 2, 1, '15000.00', '15000.00'),
(13, 8, 3, 1, '15000.00', '15000.00'),
(14, 8, 3, 1, '15000.00', '15000.00'),
(15, 4, 6, 1, '20000.00', '20000.00'),
(16, 11, 2, 3, '15000.00', '45000.00'),
(17, 11, 1, 1, '15000.00', '15000.00'),
(18, 11, 3, 1, '15000.00', '15000.00'),
(19, 12, 4, 1, '15000.00', '15000.00'),
(20, 12, 5, 1, '18000.00', '18000.00'),
(21, 12, 1, 2, '15000.00', '30000.00'),
(22, 13, 6, 1, '20000.00', '20000.00'),
(23, 13, 8, 1, '20000.00', '20000.00'),
(24, 15, 10, 1, '20000.00', '20000.00'),
(25, 15, 4, 1, '15000.00', '15000.00'),
(26, 15, 8, 1, '20000.00', '20000.00'),
(27, 15, 8, 1, '20000.00', '20000.00'),
(28, 15, 8, 1, '20000.00', '20000.00'),
(29, 16, 10, 1, '20000.00', '20000.00'),
(30, 16, 4, 1, '15000.00', '15000.00'),
(31, 16, 8, 1, '20000.00', '20000.00'),
(32, 16, 8, 3, '20000.00', '60000.00'),
(33, 17, 2, 1, '15000.00', '15000.00'),
(34, 17, 7, 1, '10000.00', '10000.00'),
(35, 17, 8, 1, '20000.00', '20000.00'),
(36, 18, 2, 1, '15000.00', '15000.00'),
(37, 18, 7, 1, '10000.00', '10000.00'),
(38, 18, 8, 1, '20000.00', '20000.00'),
(39, 19, 10, 1, '20000.00', '20000.00'),
(40, 20, 10, 1, '20000.00', '20000.00'),
(42, 22, 5, 3, '18000.00', '54000.00'),
(43, 23, 4, 1, '15000.00', '15000.00'),
(44, 23, 2, 1, '15000.00', '15000.00'),
(45, 23, 10, 1, '20000.00', '20000.00'),
(46, 23, 10, 1, '20000.00', '20000.00'),
(47, 23, 10, 1, '20000.00', '20000.00'),
(48, 24, 10, 1, '20000.00', '20000.00'),
(49, 25, 4, 1, '15000.00', '15000.00'),
(50, 25, 2, 1, '15000.00', '15000.00'),
(51, 25, 4, 1, '15000.00', '15000.00'),
(52, 25, 4, 1, '15000.00', '15000.00'),
(53, 26, 10, 1, '20000.00', '20000.00'),
(54, 27, 10, 1, '20000.00', '20000.00'),
(55, 21, 2, 1, '15000.00', '15000.00'),
(56, 28, 10, 1, '20000.00', '20000.00'),
(59, 30, 10, 1, '20000.00', '20000.00'),
(60, 29, 2, 1, '15000.00', '15000.00'),
(61, 29, 4, 1, '15000.00', '15000.00'),
(63, 35, 10, 1, '20000.00', '20000.00'),
(64, 36, 2, 1, '15000.00', '15000.00'),
(65, 36, 4, 1, '15000.00', '15000.00'),
(66, 36, 7, 1, '10000.00', '10000.00'),
(67, 36, 1, 1, '15000.00', '15000.00'),
(68, 37, 10, 1, '20000.00', '20000.00'),
(69, 37, 10, 1, '20000.00', '20000.00'),
(70, 38, 10, 1, '20000.00', '20000.00'),
(71, 39, 10, 1, '20000.00', '20000.00'),
(72, 40, 10, 3, '20000.00', '60000.00'),
(73, 40, 2, 1, '15000.00', '15000.00'),
(74, 41, 9, 1, '20000.00', '20000.00'),
(75, 41, 5, 1, '18000.00', '18000.00'),
(76, 41, 3, 1, '15000.00', '15000.00'),
(77, 41, 8, 1, '20000.00', '20000.00'),
(78, 41, 10, 1, '20000.00', '20000.00'),
(79, 42, 2, 1, '15000.00', '15000.00'),
(80, 42, 4, 1, '15000.00', '15000.00'),
(81, 42, 7, 1, '10000.00', '10000.00'),
(82, 43, 10, 1, '20000.00', '20000.00'),
(83, 43, 2, 1, '15000.00', '15000.00'),
(84, 44, 10, 1, '20000.00', '20000.00'),
(85, 45, 2, 1, '15000.00', '15000.00'),
(86, 46, 10, 1, '20000.00', '20000.00');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `members`
--

CREATE TABLE `members` (
  `id` int(11) NOT NULL,
  `member_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `member_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `member_type` enum('BAC','VANG','BACH_KIM') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BAC',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `members`
--

INSERT INTO `members` (`id`, `member_code`, `member_name`, `member_type`, `active`, `created_at`) VALUES
(1, 'MEM001', 'Nguyễn Văn A', 'BAC', 1, '2026-05-01 03:17:50'),
(2, 'MEM002', 'Trần Thị B', 'VANG', 1, '2026-05-01 03:17:50'),
(3, 'MEM003', 'Lê Văn C', 'BACH_KIM', 1, '2026-05-01 03:17:50'),
(4, 'TAI01', 'Nguyễn Thành Tài', 'BAC', 1, '2026-05-01 03:29:54'),
(6, 'TVF0D162', 'Taiii', 'BAC', 1, '2026-05-01 04:13:59'),
(7, 'TVD1025D', 'Tran Ngoc Tan', 'BACH_KIM', 1, '2026-05-02 16:46:22'),
(8, 'TV001026', 'Huỳnh Thái Trọng', 'BACH_KIM', 1, '2026-05-03 18:48:53'),
(9, 'TVE7BE79', 'Huỳnh Thầy Trọng', 'BAC', 1, '2026-05-03 18:49:19');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `memberVip`
--

CREATE TABLE `memberVip` (
  `id` int(11) NOT NULL,
  `member_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `points` int(11) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `memberVip`
--

INSERT INTO `memberVip` (`id`, `member_code`, `full_name`, `phone`, `points`) VALUES
(1, 'TV01', 'Đỗ Thanh Tiến', '09281467', 50),
(2, 'TV02', 'Tiến', '032589723', 100);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `products`
--

CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(12,2) NOT NULL DEFAULT '0.00',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `image` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `products`
--

INSERT INTO `products` (`id`, `code`, `name`, `category`, `price`, `active`, `created_at`, `image`) VALUES
(1, 'PEP', 'Pepsi', 'Soft Drink', '15000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/K8rktQ44/pepsi.jpg'),
(2, 'COC', 'Coca', 'Soft Drink', '15000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/jSZPJK8P/cocacola.jpg'),
(3, 'SPR', 'Sprite', 'Soft Drink', '15000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/KjSMyYzb/sprite.jpg'),
(4, 'FAN', 'Fanta', 'Soft Drink', '15000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/qMr3bgSr/fanta.jpg'),
(5, 'STI', 'Sting', 'Energy', '18000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/4xLHxPd2/Sting.jpg'),
(6, 'RED', 'Red Bull', 'Energy', '20000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/W1Htv4Wh/Red-Bull.jpg'),
(7, 'WAT', 'Nước suối', 'Water', '10000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/mZmsq56Q/water.jpg'),
(8, 'TRA', 'Trà chanh', 'Tea', '20000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/HWhNbDhQ/Tea.jpg'),
(9, 'MLK', 'Sữa tươi', 'Milk', '20000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/9fL6S4Lf/Milk.jpg'),
(10, 'CFD', 'Cà phê đen', 'Coffee', '20000.00', 1, '2026-04-29 09:21:35', 'https://i.postimg.cc/T1DBptGz/coffee.jpg');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `promotions`
--

CREATE TABLE `promotions` (
  `id` int(11) NOT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `discount_percent` int(11) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Đang đổ dữ liệu cho bảng `promotions`
--

INSERT INTO `promotions` (`id`, `code`, `name`, `discount_percent`, `start_date`, `end_date`) VALUES
(1, '123', 'gift', 10, '2026-05-02 17:32:25', '2026-05-04 17:32:25'),
(2, 'GIFT', 'GIAMGIA', 30, '2026-05-02 17:33:28', '2026-05-03 17:33:28'),
(3, 'DAOTRONLONG', 'Trộn Lòng Tới Chơi - Giảm 50%', 50, '2026-05-04 01:29:55', '2026-05-08 01:29:55'),
(4, 'CHAYNHA', 'CHÁY NHÀ', 90, '2026-05-01 01:47:08', '2026-05-28 01:47:08');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('MANAGER','EMPLOYEE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `username`, `password_hash`, `full_name`, `role`, `active`, `created_at`) VALUES
(1, 'admin', '123', 'Quản Lý', 'MANAGER', 1, '2026-04-29 09:21:32'),
(2, 'nv01', '123', 'Nhân Viên 01', 'EMPLOYEE', 1, '2026-04-29 09:21:32'),
(3, 'trancaoti', '123', 'Trần Cao Ti', 'EMPLOYEE', 1, '2026-05-03 18:54:23');

-- --------------------------------------------------------

--
-- Cấu trúc đóng vai cho view `v_invoice_detail`
-- (See below for the actual view)
--
CREATE TABLE `v_invoice_detail` (
`invoice_id` int(11)
,`invoice_code` varchar(40)
,`created_at` datetime
,`created_by_name` varchar(120)
,`customer_name` varchar(120)
,`note` varchar(255)
,`total_amount` decimal(12,2)
,`status` enum('PAID','CANCELLED')
,`item_id` int(11)
,`product_code` varchar(30)
,`product_name` varchar(120)
,`qty` int(11)
,`unit_price` decimal(12,2)
,`line_total` decimal(12,2)
);

-- --------------------------------------------------------

--
-- Cấu trúc cho view `v_invoice_detail`
--
DROP TABLE IF EXISTS `v_invoice_detail`;

CREATE ALGORITHM=UNDEFINED DEFINER=`sql12824805`@`%` SQL SECURITY DEFINER VIEW `v_invoice_detail`  AS  select `i`.`id` AS `invoice_id`,`i`.`code` AS `invoice_code`,`i`.`created_at` AS `created_at`,`u`.`full_name` AS `created_by_name`,`i`.`customer_name` AS `customer_name`,`i`.`note` AS `note`,`i`.`total_amount` AS `total_amount`,`i`.`status` AS `status`,`it`.`id` AS `item_id`,`p`.`code` AS `product_code`,`p`.`name` AS `product_name`,`it`.`qty` AS `qty`,`it`.`unit_price` AS `unit_price`,`it`.`line_total` AS `line_total` from (((`invoices` `i` join `users` `u` on((`u`.`id` = `i`.`created_by`))) join `invoice_items` `it` on((`it`.`invoice_id` = `i`.`id`))) join `products` `p` on((`p`.`id` = `it`.`product_id`))) ;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`),
  ADD KEY `idx_invoices_created_at` (`created_at`),
  ADD KEY `idx_invoices_created_by` (`created_by`);

--
-- Chỉ mục cho bảng `invoice_items`
--
ALTER TABLE `invoice_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_items_products` (`product_id`),
  ADD KEY `idx_items_invoice_id` (`invoice_id`);

--
-- Chỉ mục cho bảng `members`
--
ALTER TABLE `members`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `member_code` (`member_code`),
  ADD KEY `idx_members_active` (`active`),
  ADD KEY `idx_members_type` (`member_type`);

--
-- Chỉ mục cho bảng `memberVip`
--
ALTER TABLE `memberVip`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `member_code` (`member_code`);

--
-- Chỉ mục cho bảng `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`),
  ADD KEY `idx_products_active` (`active`);

--
-- Chỉ mục cho bảng `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `invoices`
--
ALTER TABLE `invoices`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;
--
-- AUTO_INCREMENT cho bảng `invoice_items`
--
ALTER TABLE `invoice_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=87;
--
-- AUTO_INCREMENT cho bảng `members`
--
ALTER TABLE `members`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;
--
-- AUTO_INCREMENT cho bảng `memberVip`
--
ALTER TABLE `memberVip`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT cho bảng `products`
--
ALTER TABLE `products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;
--
-- AUTO_INCREMENT cho bảng `promotions`
--
ALTER TABLE `promotions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `fk_invoices_users` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `invoice_items`
--
ALTER TABLE `invoice_items`
  ADD CONSTRAINT `fk_items_invoices` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_items_products` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
