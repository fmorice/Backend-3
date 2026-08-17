CREATE TABLE IF NOT EXISTS transaccion_procesada (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cuenta_id INT,
  fecha DATE,
  transaccion VARCHAR(100),
  monto DECIMAL(15,2),
  descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS interes_procesado (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cuenta_id INT,
  nombre VARCHAR(200),
  saldo DECIMAL(15,2),
  tipo VARCHAR(50),
  tasa DECIMAL(10,4),
  interes DECIMAL(15,2)
);

CREATE TABLE IF NOT EXISTS CUENTA_ANUAL_PROCESADA (
  id INT PRIMARY KEY,
  fecha DATE,
  monto DECIMAL(15,2),
  tipo VARCHAR(50)
);
