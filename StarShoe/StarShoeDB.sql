CREATE DATABASE StarShoeDB;
GO
USE StarShoeDB;
GO

-- Roles
CREATE TABLE Roles (
    RoleID INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL
);
GO

INSERT INTO Roles (RoleName) VALUES 
('Admin'), ('Employee'), ('Customer');
GO

-- Users
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    FullName NVARCHAR(100),
    Username NVARCHAR(50) UNIQUE NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    Email NVARCHAR(100),
    RoleID INT FOREIGN KEY REFERENCES Roles(RoleID),
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO


INSERT INTO Users (FullName, Username, PasswordHash, Email, RoleID) VALUES
(N'Tran Trong D', 'admin', '123456', 'admin@starshoe.com', 1),
(N'Le Van C', 'employee1', '123456', 'emp1@starshoe.com', 2),
(N'Nguyen Van A', 'customer1', '123456', 'cust1@gmail.com', 3),
(N'Tran Thi B', 'customer2', '123456', 'cust2@gmail.com', 3);
GO

-- Customers
CREATE TABLE Customers (
    CustomerID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT FOREIGN KEY REFERENCES Users(UserID),
    FullName NVARCHAR(100),
    Phone NVARCHAR(15),
    Address NVARCHAR(255)
);
GO

INSERT INTO Customers (UserID, FullName, Phone, Address) VALUES
(3, N'Nguyen Van A', '0901234567', N'Ha Noi'),
(4, N'Tran Thi B', '0912345678', N'Ho Chi Minh');
GO

-- Employees
CREATE TABLE Employees (
    EmployeeID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT FOREIGN KEY REFERENCES Users(UserID),
    FullName NVARCHAR(100),
    Position NVARCHAR(50),
    Phone NVARCHAR(15)
);
GO

INSERT INTO Employees (UserID, FullName, Position, Phone) VALUES
(2, N'Le Van C', N'Warehouse management', '0987654321');
GO

-- Brands
CREATE TABLE Brands (
    BrandID INT IDENTITY(1,1) PRIMARY KEY,
    BrandName NVARCHAR(100) NOT NULL
);
GO

INSERT INTO Brands (BrandName) VALUES
('Nike'), ('Adidas'), ('Puma'), ('Converse'), ('Jordan');
GO

-- Categories
CREATE TABLE Categories (
    CategoryID INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName NVARCHAR(100) NOT NULL
);
GO

INSERT INTO Categories (CategoryName) VALUES
(N'Sneaker'), (N'Running'), (N'Sport'), (N'Training'), (N'Casual');
GO

-- Products
CREATE TABLE Products (
    ProductID INT IDENTITY(1,1) PRIMARY KEY,
    BrandID INT FOREIGN KEY REFERENCES Brands(BrandID),
    CategoryID INT FOREIGN KEY REFERENCES Categories(CategoryID),
    ProductName NVARCHAR(150),
    Description NVARCHAR(MAX),
    Price DECIMAL(10,2),
    Quantity INT,
    Image NVARCHAR(255),
    CreatedAt DATETIME DEFAULT GETDATE(),
    IsSale BIT DEFAULT 0 NULL,
    SalePercentage INT NULL
);
GO

INSERT INTO Products (BrandID, CategoryID, ProductName, Description, Price, Quantity, Image) VALUES
(1, 1, N'Nike Air Force 1', N'Classic all-white Nike sneaker with leather upper, perforated toe, and cushioned midsole for all-day comfort.', 126, 10, 'product1.png'),
(1, 3, N'Nike LeBron Witness 8', N'High-performance mid-cut basketball shoes featuring responsive cushioning, breathable mesh, and durable traction for indoor/outdoor courts.', 152, 8, 'product2.png'),
(1, 2, N'Nike Pegasus 41', N'Lightweight and durable running shoes designed for daily training, with foam midsole and engineered mesh for breathability.', 139, 12, 'product3.png'),
(1, 4, N'Nike Metcon 9', N'Stability-focused training shoes with firm heel, flexible forefoot, and durable outsole ideal for weightlifting and HIIT workouts.', 143, 7, 'product4.png'),
(1, 1, N'Nike Dunk Low Retro', N'Casual low-top sneakers with vintage color blocking, soft leather upper, and comfortable foam midsole.', 135, 11, 'product5.png'),
(2, 1, N'Adidas Stan Smith', N'Iconic minimalist leather sneakers with perforated three stripes, cushioned insole, and durable rubber outsole.', 117, 15, 'product6.png'),
(2, 2, N'Adidas Ultraboost 23', N'Premium running shoes with ultra-soft Boost cushioning, breathable Primeknit upper, and responsive energy return for long distances.', 165, 6, 'product7.png'),
(2, 4, N'Adidas Powerlift 5', N'Professional weightlifting shoes with stable heel, reinforced straps, and grippy rubber outsole for maximum lifting performance.', 135, 5, 'product8.png'),
(2, 2, N'Adidas Terrex Free Hiker 2', N'Waterproof hiking shoes with GORE-TEX upper, supportive midsole, and durable traction for rugged outdoor terrain.', 178, 4, 'product9.png'),
(2, 3, N'Adidas Harden Vol.8', N'Basketball shoes designed for explosive movements, featuring lightweight cushioning, responsive midsole, and secure fit.', 161, 6, 'product10.png'),
(3, 1, N'Puma Cali Dream', N'Retro-inspired women’s sneaker with leather upper, thick sole, and stylish casual look for everyday wear.', 109, 9, 'product11.png'),
(3, 2, N'Puma Velocity Nitro 3', N'Advanced running shoes with Nitro Foam for responsive cushioning, breathable mesh, and durable outsole.', 144, 10, 'product12.png'),
(3, 3, N'Puma Court Rider 3', N'Indoor basketball shoes with lightweight support, cushioned midsole, and excellent grip for court performance.', 148, 7, 'product13.png'),
(3, 1, N'Puma RS-X Efekt', N'Chunky lifestyle sneakers with bold colors, mixed materials, and comfortable cushioning for daily wear.', 117, 9, 'product14.png'),
(3, 4, N'Puma Fuse 3.0', N'Gym and training shoes with firm grip, flexible design, breathable upper, and ideal for cross-training workouts.', 122, 8, 'product15.png'),
(4, 5, N'Converse Chuck Taylor 70s', N'Legendary high-top canvas sneakers with vintage detailing, durable rubber sole, and timeless casual style.', 78, 20, 'product16.png'),
(4, 1, N'Converse Run Star Hike', N'Chunky platform sneakers with bold design, mixed-material upper, and comfortable EVA midsole for streetwear fashion.', 104, 12, 'product17.png'),
(4, 1, N'Converse One Star Pro', N'Skate-ready low-top suede sneakers with cushioned insole, durable sole, and classic star branding.', 91, 10, 'product18.png'),
(5, 3, N'Air Jordan 1 Retro High OG', N'Iconic high-top basketball sneakers in black/red colorway with premium leather and classic Jordan branding.', 183, 5, 'product19.png'),
(5, 3, N'Air Jordan 11 Concord', N'Classic basketball sneakers with patent leather upper, responsive cushioning, and legendary design.', 196, 3, 'product20.png'),
(5, 1, N'Jordan Luka 2', N'Signature sneakers of Luka Dončić with lightweight support, responsive cushioning, and stylish basketball design.', 169, 6, 'product21.png'),
(5, 2, N'Jordan Zoom Separate', N'Performance running shoes featuring Zoom Air cushioning, breathable mesh upper, and responsive midsole.', 157, 8, 'product22.png'),
(1, 3, N'Nike Zoom Freak 4', N'Basketball shoes inspired by Giannis Antetokounmpo with lightweight cushioning and enhanced court traction.', 148, 7, 'product23.png'),
(1, 2, N'Nike React Infinity Run', N'Running shoes designed to reduce injury risk, featuring React foam and breathable upper.', 140, 10, 'product24.png'),
(2, 1, N'Adidas Superstar', N'Classic low-top sneakers with shell toe design, leather upper, and comfortable EVA midsole.', 115, 14, 'product25.png'),
(2, 3, N'Adidas Dame 8', N'Basketball shoes with responsive cushioning, breathable mesh, and supportive lockdown fit.', 162, 6, 'product26.png'),
(3, 2, N'Puma Deviate Nitro', N'Advanced running shoes with Nitro Foam for lightweight, responsive energy return.', 145, 9, 'product27.png'),
(3, 1, N'Puma Future Rider', N'Retro-inspired casual sneakers with soft cushioning and stylish colorways.', 110, 12, 'product28.png'),
(4, 5, N'Converse All Star Pro BB', N'High-performance basketball sneakers with responsive cushioning and durable traction.', 102, 5, 'product29.png'),
(4, 1, N'Converse Chuck 70 Low', N'Low-top casual sneakers with vintage canvas upper, cushioned insole, and classic styling.', 77, 11, 'product30.png'),
(5, 3, N'Air Jordan 5 Retro', N'Basketball shoes with premium leather, cushioned midsole, and iconic Jordan silhouette.', 182, 4, 'product31.png'),
(5, 1, N'Jordan Why Not Zer0.4', N'Basketball sneakers designed for explosive performance with responsive cushioning and supportive fit.', 172, 6, 'product32.png'),
(1, 4, N'Nike Free Metcon', N'Training shoes with flexible forefoot, firm heel, and breathable mesh upper.', 138, 8, 'product33.png'),
(2, 2, N'Adidas Adizero Adios Pro', N'Lightweight running shoes with energy-returning midsole and breathable upper.', 170, 5, 'product34.png'),
(3, 3, N'Puma Clyde All Pro', N'Basketball shoes with supportive upper, responsive cushioning, and excellent grip on court.', 150, 7, 'product35.png');
GO

-- Orders
CREATE TABLE Orders (
    OrderID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerID INT FOREIGN KEY REFERENCES Customers(CustomerID),
    OrderDate DATETIME DEFAULT GETDATE(),
    Status NVARCHAR(50) DEFAULT N'Pending',
    TotalAmount DECIMAL(10,2)
);
GO

INSERT INTO Orders (CustomerID, TotalAmount, Status)
VALUES
(1, 6700000, N'Confirmed'),
(2, 1800000, N'Pending');
GO

-- OrderDetails
CREATE TABLE OrderDetails (
    OrderDetailID INT IDENTITY(1,1) PRIMARY KEY,
    OrderID INT FOREIGN KEY REFERENCES Orders(OrderID),
    ProductID INT FOREIGN KEY REFERENCES Products(ProductID),
    Quantity INT,
    UnitPrice DECIMAL(10,2)
);
GO

INSERT INTO OrderDetails (OrderID, ProductID, Quantity, UnitPrice)
VALUES
(1,1,1,3200000),
(1,2,1,3500000),
(2,4,1,1800000);
GO

-- Payments
CREATE TABLE Payments (
    PaymentID INT IDENTITY(1,1) PRIMARY KEY,
    OrderID INT FOREIGN KEY REFERENCES Orders(OrderID),
    PaymentMethod NVARCHAR(50),
    PaymentDate DATETIME DEFAULT GETDATE(),
    Amount DECIMAL(10,2)
);
GO

INSERT INTO Payments (OrderID, PaymentMethod, Amount)
VALUES
(1, N'Credit Card', 6700000),
(2, N'COD', 1800000);
GO

-- Reviews
CREATE TABLE Reviews (
    ReviewID INT IDENTITY(1,1) PRIMARY KEY,
    ProductID INT FOREIGN KEY REFERENCES Products(ProductID),
    CustomerID INT FOREIGN KEY REFERENCES Customers(CustomerID),
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    Comment NVARCHAR(500),
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

INSERT INTO Reviews (ProductID, CustomerID, Rating, Comment)
VALUES
(1, 1, 5, N'It is a nice shoe, very like it'),
(2, 2, 4, N'Feel great, the delivery guy is very friendly');
GO
