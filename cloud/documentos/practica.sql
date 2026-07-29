-- Crear base de datos
CREATE DATABASE EmpresaVentas;
GO

USE EmpresaVentas;
GO

-- Tabla Clientes
CREATE TABLE Clientes (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100),
    telefono VARCHAR(20),
    correo VARCHAR(100)
);
GO

-- Tabla Productos
CREATE TABLE Productos (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100),
    precio DECIMAL(10,2),
    stock INT
);
GO

-- Tabla Empleados
CREATE TABLE Empleados (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100),
    puesto VARCHAR(50)
);
GO

-- Tabla Ventas
CREATE TABLE Ventas (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cliente_id INT,
    producto_id INT,
    empleado_id INT,
    cantidad INT,
    fecha DATETIME DEFAULT GETDATE(),

    FOREIGN KEY(cliente_id) REFERENCES Clientes(id),
    FOREIGN KEY(producto_id) REFERENCES Productos(id),
    FOREIGN KEY(empleado_id) REFERENCES Empleados(id)
);
GO

-- Datos de prueba

INSERT INTO Clientes(nombre, telefono, correo)
VALUES
('Juan Perez','3411234567','juan@mail.com'),
('Maria Lopez','3417654321','maria@mail.com');
GO

INSERT INTO Productos(nombre, precio, stock)
VALUES
('Laptop',15000,10),
('Mouse',250,50),
('Teclado',500,20);
GO

INSERT INTO Empleados(nombre, puesto)
VALUES
('Carlos Ruiz','Vendedor'),
('Ana Torres','Gerente');
GO

INSERT INTO Ventas(cliente_id, producto_id, empleado_id, cantidad)
VALUES
(1,1,1,1),
(2,2,1,3);
GO
