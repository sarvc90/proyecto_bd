/*
    Script de creación de base de datos para el proyecto de electrodomésticos.
    Motor: Microsoft SQL Server
    Ejecución recomendada en SQL Server Management Studio (SSMS) o Azure Data Studio.
x

IF DB_ID(N'ElectrodomesticosDB') IS NULL
BEGIN
    CREATE DATABASE ElectrodomesticosDB
    COLLATE Latin1_General_CI_AS;
END

GO

USE ElectrodomesticosDB;
GO

/* Eliminar tablas si ya existen (respeta dependencias) 
IF OBJECT_ID(N'dbo.Cuotas', N'U') IS NOT NULL DROP TABLE dbo.Cuotas;
IF OBJECT_ID(N'dbo.Creditos', N'U') IS NOT NULL DROP TABLE dbo.Creditos;
IF OBJECT_ID(N'dbo.DetalleVentas', N'U') IS NOT NULL DROP TABLE dbo.DetalleVentas;
IF OBJECT_ID(N'dbo.Ventas', N'U') IS NOT NULL DROP TABLE dbo.Ventas;
IF OBJECT_ID(N'dbo.Inventarios', N'U') IS NOT NULL DROP TABLE dbo.Inventarios;
IF OBJECT_ID(N'dbo.Productos', N'U') IS NOT NULL DROP TABLE dbo.Productos;
IF OBJECT_ID(N'dbo.Categorias', N'U') IS NOT NULL DROP TABLE dbo.Categorias;
IF OBJECT_ID(N'dbo.Clientes', N'U') IS NOT NULL DROP TABLE dbo.Clientes;
IF OBJECT_ID(N'dbo.Auditorias', N'U') IS NOT NULL DROP TABLE dbo.Auditorias;
IF OBJECT_ID(N'dbo.Usuarios', N'U') IS NOT NULL DROP TABLE dbo.Usuarios;
GO

/* ==================== TABLA: Usuarios ==================== 
CREATE TABLE dbo.Usuarios (
    idUsuario               INT             IDENTITY(1,1) PRIMARY KEY,
    nombreCompleto          VARCHAR(150)    NOT NULL,
    username                VARCHAR(50)     NOT NULL,
    passwordHash            CHAR(64)        NOT NULL,
    rol                     VARCHAR(30)     NOT NULL,
    email                   VARCHAR(150)    NULL,
    telefono                VARCHAR(25)     NULL,
    activo                  BIT             NOT NULL DEFAULT (1),
    fechaRegistro           DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    ultimoAcceso            DATETIME2       NULL,
    CONSTRAINT UQ_Usuarios_Username UNIQUE (username),
    CONSTRAINT CK_Usuarios_Rol CHECK (LEN(LTRIM(RTRIM(rol))) > 0)
);
GO

CREATE UNIQUE INDEX UX_Usuarios_Email
    ON dbo.Usuarios(email)
    WHERE email IS NOT NULL;
GO

/* ==================== TABLA: Auditorias ==================== 
CREATE TABLE dbo.Auditorias (
    idAuditoria             INT             IDENTITY(1,1) PRIMARY KEY,
    idUsuario               INT             NOT NULL,
    accion                  VARCHAR(50)     NOT NULL,
    tablaAfectada           VARCHAR(50)     NULL,
    descripcion             VARCHAR(500)    NULL,
    ip                      VARCHAR(45)     NULL,
    fechaAccion             DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_Auditorias_Usuarios
        FOREIGN KEY (idUsuario) REFERENCES dbo.Usuarios(idUsuario)
);
GO

CREATE INDEX IX_Auditorias_Usuario ON dbo.Auditorias(idUsuario);
CREATE INDEX IX_Auditorias_Fecha ON dbo.Auditorias(fechaAccion);
GO

/* ==================== TABLA: Clientes ==================== 
CREATE TABLE dbo.Clientes (
    idCliente               INT             IDENTITY(1,1) PRIMARY KEY,
    cedula                  VARCHAR(20)     NOT NULL,
    nombre                  VARCHAR(100)    NOT NULL,
    apellido                VARCHAR(100)    NOT NULL,
    direccion               VARCHAR(200)    NULL,
    telefono                VARCHAR(25)     NULL,
    email                   VARCHAR(150)    NULL,
    fechaRegistro           DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    activo                  BIT             NOT NULL DEFAULT (1),
    limiteCredito           DECIMAL(12,2)   NOT NULL DEFAULT (0),
    saldoPendiente          DECIMAL(12,2)   NOT NULL DEFAULT (0),
    CONSTRAINT UQ_Clientes_Cedula UNIQUE (cedula),
    CONSTRAINT CK_Clientes_Montos CHECK (limiteCredito >= 0 AND saldoPendiente >= 0)
);
GO

CREATE UNIQUE INDEX UX_Clientes_Email
    ON dbo.Clientes(email)
    WHERE email IS NOT NULL;
GO

/* ==================== TABLA: Categorias ==================== 
CREATE TABLE dbo.Categorias (
    idCategoria             INT             IDENTITY(1,1) PRIMARY KEY,
    codigo                  VARCHAR(20)     NOT NULL,
    nombre                  VARCHAR(120)    NOT NULL,
    descripcion             VARCHAR(300)    NULL,
    activo                  BIT             NOT NULL DEFAULT (1),
    fechaRegistro           DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    fechaUltimaActualizacion DATETIME2      NOT NULL DEFAULT (SYSDATETIME()),
    nivel                   TINYINT         NOT NULL DEFAULT (1),
    idCategoriaPadre        INT             NULL,
    rutaCompleta            VARCHAR(500)    NULL,
    cantidadProductos       INT             NOT NULL DEFAULT (0),
    CONSTRAINT UQ_Categorias_Codigo UNIQUE (codigo),
    CONSTRAINT CK_Categorias_Nivel CHECK (nivel BETWEEN 1 AND 5),
    CONSTRAINT CK_Categorias_Cantidad CHECK (cantidadProductos >= 0),
    CONSTRAINT FK_Categorias_Padre
        FOREIGN KEY (idCategoriaPadre) REFERENCES dbo.Categorias(idCategoria)
);
GO

CREATE INDEX IX_Categorias_IdPadre ON dbo.Categorias(idCategoriaPadre);
GO

/* ==================== TABLA: Productos ==================== 
CREATE TABLE dbo.Productos (
    idProducto              INT             IDENTITY(1,1) PRIMARY KEY,
    codigo                  VARCHAR(50)     NOT NULL,
    nombre                  VARCHAR(150)    NOT NULL,
    descripcion             VARCHAR(500)    NULL,
    marca                   VARCHAR(80)     NULL,
    modelo                  VARCHAR(80)     NULL,
    idCategoria             INT             NOT NULL,
    precioCompra            DECIMAL(12,2)   NOT NULL DEFAULT (0),
    precioVenta             DECIMAL(12,2)   NOT NULL DEFAULT (0),
    stockActual             INT             NOT NULL DEFAULT (0),
    stockMinimo             INT             NOT NULL DEFAULT (0),
    stockMaximo             INT             NOT NULL DEFAULT (0),
    unidadMedida            VARCHAR(20)     NOT NULL DEFAULT ('UNIDAD'),
    activo                  BIT             NOT NULL DEFAULT (1),
    fechaRegistro           DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    fechaUltimaActualizacion DATETIME2      NOT NULL DEFAULT (SYSDATETIME()),
    garantiaMeses           INT             NOT NULL DEFAULT (12),
    ubicacionAlmacen        VARCHAR(120)    NULL,
    CONSTRAINT UQ_Productos_Codigo UNIQUE (codigo),
    CONSTRAINT FK_Productos_Categoria
        FOREIGN KEY (idCategoria) REFERENCES dbo.Categorias(idCategoria),
    CONSTRAINT CK_Productos_Precios CHECK (precioCompra >= 0 AND precioVenta >= 0 AND precioVenta >= precioCompra),
    CONSTRAINT CK_Productos_Stock CHECK (stockActual >= 0 AND stockMinimo >= 0 AND stockMaximo >= 0 AND stockMaximo >= stockMinimo)
);
GO

CREATE INDEX IX_Productos_Categoria ON dbo.Productos(idCategoria);
CREATE INDEX IX_Productos_Busqueda ON dbo.Productos(nombre, marca);
GO

/* ==================== TABLA: Inventarios ==================== 
CREATE TABLE dbo.Inventarios (
    idInventario            INT             IDENTITY(1,1) PRIMARY KEY,
    idProducto              INT             NOT NULL,
    cantidadActual          INT             NOT NULL DEFAULT (0),
    stockMinimo             INT             NOT NULL DEFAULT (0),
    stockMaximo             INT             NOT NULL DEFAULT (0),
    ultimaActualizacion     DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    CONSTRAINT UQ_Inventarios_Producto UNIQUE (idProducto),
    CONSTRAINT FK_Inventarios_Producto
        FOREIGN KEY (idProducto) REFERENCES dbo.Productos(idProducto)
        ON DELETE CASCADE,
    CONSTRAINT CK_Inventarios_Stock CHECK (cantidadActual >= 0 AND stockMinimo >= 0 AND stockMaximo >= 0 AND stockMaximo >= stockMinimo)
);
GO

/* ==================== TABLA: Ventas ==================== 
CREATE TABLE dbo.Ventas (
    idVenta                 INT             IDENTITY(1,1) PRIMARY KEY,
    codigo                  VARCHAR(30)     NOT NULL,
    idCliente               INT             NOT NULL,
    idUsuario               INT             NOT NULL,
    fechaVenta              DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    esCredito               BIT             NOT NULL DEFAULT (0),
    subtotal                DECIMAL(12,2)   NOT NULL DEFAULT (0),
    ivaTotal                DECIMAL(12,2)   NOT NULL DEFAULT (0),
    total                   DECIMAL(12,2)   NOT NULL DEFAULT (0),
    cuotaInicial            DECIMAL(12,2)   NOT NULL DEFAULT (0),
    plazoMeses              INT             NOT NULL DEFAULT (0),
    estado                  VARCHAR(20)     NOT NULL DEFAULT ('REGISTRADA'),
    CONSTRAINT UQ_Ventas_Codigo UNIQUE (codigo),
    CONSTRAINT FK_Ventas_Clientes
        FOREIGN KEY (idCliente) REFERENCES dbo.Clientes(idCliente),
    CONSTRAINT FK_Ventas_Usuarios
        FOREIGN KEY (idUsuario) REFERENCES dbo.Usuarios(idUsuario),
    CONSTRAINT CK_Ventas_Totales CHECK (subtotal >= 0 AND ivaTotal >= 0 AND total >= 0),
    CONSTRAINT CK_Ventas_Plazo CHECK ((esCredito = 1 AND plazoMeses > 0) OR (esCredito = 0)),
    CONSTRAINT CK_Ventas_Cuota CHECK (cuotaInicial >= 0 AND cuotaInicial <= total),
    CONSTRAINT CK_Ventas_Estado CHECK (estado IN ('REGISTRADA','PAGADA','ANULADA'))
);
GO

CREATE INDEX IX_Ventas_Cliente ON dbo.Ventas(idCliente);
CREATE INDEX IX_Ventas_Usuario ON dbo.Ventas(idUsuario);
CREATE INDEX IX_Ventas_Fecha ON dbo.Ventas(fechaVenta);
GO

/* ==================== TABLA: DetalleVentas ==================== 
CREATE TABLE dbo.DetalleVentas (
    idDetalle               INT             IDENTITY(1,1) PRIMARY KEY,
    idVenta                 INT             NOT NULL,
    idProducto              INT             NOT NULL,
    cantidad                INT             NOT NULL DEFAULT (1),
    precioUnitario          DECIMAL(12,2)   NOT NULL DEFAULT (0),
    subtotal                DECIMAL(12,2)   NOT NULL DEFAULT (0),
    montoIVA                DECIMAL(12,2)   NOT NULL DEFAULT (0),
    total                   DECIMAL(12,2)   NOT NULL DEFAULT (0),
    fechaRegistro           DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_DetalleVentas_Ventas
        FOREIGN KEY (idVenta) REFERENCES dbo.Ventas(idVenta)
        ON DELETE CASCADE,
    CONSTRAINT FK_DetalleVentas_Productos
        FOREIGN KEY (idProducto) REFERENCES dbo.Productos(idProducto),
    CONSTRAINT CK_DetalleVentas_Cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_DetalleVentas_Valores CHECK (precioUnitario >= 0 AND subtotal >= 0 AND montoIVA >= 0 AND total >= 0)
);
GO

CREATE INDEX IX_DetalleVentas_Venta ON dbo.DetalleVentas(idVenta);
CREATE INDEX IX_DetalleVentas_Producto ON dbo.DetalleVentas(idProducto);
GO

/* ==================== TABLA: Creditos ==================== 
CREATE TABLE dbo.Creditos (
    idCredito               INT             IDENTITY(1,1) PRIMARY KEY,
    idVenta                 INT             NOT NULL,
    idCliente               INT             NOT NULL,
    montoTotal              DECIMAL(12,2)   NOT NULL DEFAULT (0),
    interes                 DECIMAL(6,4)    NOT NULL DEFAULT (0),
    plazoMeses              INT             NOT NULL DEFAULT (0),
    cuotaInicial            DECIMAL(12,2)   NOT NULL DEFAULT (0),
    saldoPendiente          DECIMAL(12,2)   NOT NULL DEFAULT (0),
    estado                  VARCHAR(20)     NOT NULL DEFAULT ('ACTIVO'),
    fechaRegistro           DATETIME2       NOT NULL DEFAULT (SYSDATETIME()),
    CONSTRAINT UQ_Creditos_Venta UNIQUE (idVenta),
    CONSTRAINT FK_Creditos_Ventas
        FOREIGN KEY (idVenta) REFERENCES dbo.Ventas(idVenta),
    CONSTRAINT FK_Creditos_Clientes
        FOREIGN KEY (idCliente) REFERENCES dbo.Clientes(idCliente),
    CONSTRAINT CK_Creditos_Montos CHECK (montoTotal >= 0 AND cuotaInicial >= 0 AND saldoPendiente >= 0),
    CONSTRAINT CK_Creditos_Interes CHECK (interes >= 0 AND interes <= 5),
    CONSTRAINT CK_Creditos_Plazo CHECK (plazoMeses > 0),
    CONSTRAINT CK_Creditos_Estado CHECK (estado IN ('ACTIVO','CANCELADO','MOROSO'))
);
GO

CREATE INDEX IX_Creditos_Cliente ON dbo.Creditos(idCliente);
CREATE INDEX IX_Creditos_Estado ON dbo.Creditos(estado);
GO

/* ==================== TABLA: Cuotas ==================== 
CREATE TABLE dbo.Cuotas (
    idCuota                 INT             IDENTITY(1,1) PRIMARY KEY,
    numeroCuota             INT             NOT NULL,
    idCredito               INT             NOT NULL,
    valor                   DECIMAL(12,2)   NOT NULL DEFAULT (0),
    fechaVencimiento        DATE            NOT NULL,
    fechaPago               DATETIME2       NULL,
    pagada                  BIT             NOT NULL DEFAULT (0),
    CONSTRAINT UQ_Cuotas_Numero UNIQUE (idCredito, numeroCuota),
    CONSTRAINT FK_Cuotas_Creditos
        FOREIGN KEY (idCredito) REFERENCES dbo.Creditos(idCredito)
        ON DELETE CASCADE,
    CONSTRAINT CK_Cuotas_Numero CHECK (numeroCuota > 0),
    CONSTRAINT CK_Cuotas_Valor CHECK (valor >= 0)
);
GO

CREATE INDEX IX_Cuotas_CreditoPagada ON dbo.Cuotas(idCredito, pagada);
CREATE INDEX IX_Cuotas_Vencimiento ON dbo.Cuotas(fechaVencimiento);
GO

/* ==================== DATOS DE REFERENCIA (Opcionales) ==================== 
-- Descomenta este bloque si deseas cargar datos de prueba básicos.
/*
INSERT INTO dbo.Usuarios (nombreCompleto, username, passwordHash, rol, email, telefono)
VALUES
    ('Admin General', 'admin', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '1234'), 2), 'ADMIN', 'admin@sistema.com', '3001112233'),
    ('Carlos Pérez', 'cperez', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'ventas123'), 2), 'VENDEDOR', 'carlos@sistema.com', '3014445566'),
    ('Laura Gómez', 'lgomez', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'clave789'), 2), 'GERENTE', 'laura@sistema.com', '3029998877');

INSERT INTO dbo.Clientes (cedula, nombre, apellido, direccion, telefono, email, limiteCredito, saldoPendiente)
VALUES
    ('1234567890', 'Carlos', 'Pérez', 'Calle 123', '3101112233', 'carlos@mail.com', 1000, 200),
    ('1098765432', 'Ana', 'Gómez', 'Carrera 45', '3204445566', 'ana@mail.com', 2000, 0);

INSERT INTO dbo.Categorias (codigo, nombre, descripcion, nivel)
VALUES
    ('CAT001', 'Electrodomésticos', 'Línea blanca y cocina', 1),
    ('CAT002', 'Refrigeradores', 'Subcategoría refrigeradores', 2),
    ('CAT003', 'Televisores', 'Subcategoría televisores', 2);

INSERT INTO dbo.Productos (codigo, nombre, descripcion, marca, modelo, idCategoria, precioCompra, precioVenta, stockActual, stockMinimo, stockMaximo, unidadMedida, garantiaMeses, ubicacionAlmacen)
VALUES
    ('P001', 'Refrigerador 420L', 'Refrigerador de dos puertas', 'LG', 'X200', 2, 1200, 1500, 10, 2, 20, 'UNIDAD', 24, 'Bodega A'),
    ('P002', 'Televisor 55"', 'Televisor QLED 55 pulgadas', 'Samsung', 'QLED55', 3, 800, 1200, 5, 1, 15, 'UNIDAD', 12, 'Bodega B');

INSERT INTO dbo.Inventarios (idProducto, cantidadActual, stockMinimo, stockMaximo)
SELECT idProducto, stockActual, stockMinimo, stockMaximo
FROM dbo.Productos;

GO

*/
