DROP DATABASE IF EXISTS p_backhome;
CREATE DATABASE p_backhome;
USE p_backhome;


CREATE TABLE tipo_documento (
    id_t_doc VARCHAR(3) PRIMARY KEY,
    n_doc VARCHAR(25) NOT NULL
);

CREATE TABLE localidades (
    id_localidad TINYINT UNSIGNED PRIMARY KEY,
    n_localidad VARCHAR(100) NOT NULL,
    poblacion INT UNSIGNED NULL
);

CREATE TABLE lugares (
    id_lugar INT PRIMARY KEY AUTO_INCREMENT,
    direccion VARCHAR(255) NOT NULL,
    localidad_id TINYINT UNSIGNED NOT NULL,

    CONSTRAINT fk_lugar_localidad
        FOREIGN KEY (localidad_id)
        REFERENCES localidades(id_localidad)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE personas (
    id_persona INT AUTO_INCREMENT PRIMARY KEY,

    t_documento_id VARCHAR(3) NOT NULL,
    n_documento VARCHAR(20) NOT NULL UNIQUE,

    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),

    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),

    email VARCHAR(150) NOT NULL UNIQUE,
    email_verified_at TIMESTAMP NULL,

    numero_tel VARCHAR(20) NOT NULL,

    password VARCHAR(255) NOT NULL,

    estado ENUM('activo', 'bloqueado', 'suspendido') DEFAULT 'activo',

    remember_token VARCHAR(100) NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    foto_perfil VARCHAR(255),

    CONSTRAINT fk_persona_tipo_documento
        FOREIGN KEY (t_documento_id)
        REFERENCES tipo_documento(id_t_doc)
        ON UPDATE CASCADE
);

CREATE TABLE administrador (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL UNIQUE,

    CONSTRAINT fk_administrador_persona
        FOREIGN KEY (persona_id)
        REFERENCES personas(id_persona)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL UNIQUE,

    CONSTRAINT fk_cliente_persona
        FOREIGN KEY (persona_id)
        REFERENCES personas(id_persona)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- 2. ANIMALES


CREATE TABLE animal (
    id_animal INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NULL,

    sexo ENUM('macho', 'hembra', 'desconocido') NOT NULL,
    color VARCHAR(50) NOT NULL,
    tamano ENUM('pequeño', 'mediano', 'grande') NULL,
    descripcion_fisica TEXT
);

CREATE TABLE animal_domestico (
    id_animal_d INT AUTO_INCREMENT PRIMARY KEY,
    animal_id INT NOT NULL UNIQUE,
    especie VARCHAR(100) NOT NULL,
    raza VARCHAR(100) NULL,

    CONSTRAINT fk_domestico_animal
        FOREIGN KEY (animal_id)
        REFERENCES animal(id_animal)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE animal_exotico (
    id_animal_e INT AUTO_INCREMENT PRIMARY KEY,
    animal_id INT NOT NULL UNIQUE,
    especie VARCHAR(100) NOT NULL,

    CONSTRAINT fk_exotico_animal
        FOREIGN KEY (animal_id)
        REFERENCES animal(id_animal)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- 3. ESTADO DE CUSTODIA 


CREATE TABLE estado_custodia (
    id_estado_custodia INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT NULL
);

INSERT INTO estado_custodia (nombre, descripcion) VALUES
('Con la persona que lo encontró', 'El animal permanece temporalmente con quien lo encontró.'),
('En un refugio', 'El animal fue trasladado y confirmado en un refugio.'),
('Devuelto a su dueño', 'El animal fue reunido con su propietario original.'),
('Otro', 'Situación de custodia distinta a las anteriores.');


-- 4. PRIORIDADES


CREATE TABLE prioridades (
    id_prioridad INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    nivel TINYINT UNSIGNED NOT NULL,
    estado ENUM('activo', 'inactivo') DEFAULT 'activo'
);

INSERT INTO prioridades (nombre, descripcion, nivel) VALUES
('Muy alta', 'Animal vulnerable, pequeño, adulto mayor o desaparecido durante mucho tiempo.', 1),
('Alta', 'Animal que requiere atención prioritaria.', 2),
('Media', 'Animal que requiere seguimiento normal.', 3),
('Normal', 'Animal sin condiciones especiales.', 4);


-- 5. REFUGIOS

CREATE TABLE refugios (
    id_refugio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(150),
    localidad_id TINYINT UNSIGNED NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_refugio_localidad
        FOREIGN KEY (localidad_id)
        REFERENCES localidades(id_localidad)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


-- 6. SEGUIMIENTOS


CREATE TABLE seguimiento (
    id_seguimiento INT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,

    fecha_publicacion DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,

   estado_seguimiento ENUM('activo', 'reunido', 'cerrado')
    NOT NULL DEFAULT 'activo',

    estado_moderacion ENUM('pendiente', 'verificado', 'rechazado')
        DEFAULT 'pendiente' NOT NULL,

    tipo_seguimiento ENUM('perdido', 'encontrado') NOT NULL,

    animal_id INT NOT NULL,
    lugar_id INT NOT NULL,
    cliente_id INT NOT NULL,
    prioridad_id INT NULL,

    CONSTRAINT fk_seguimiento_animal
        FOREIGN KEY (animal_id) REFERENCES animal(id_animal)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_seguimiento_lugar
        FOREIGN KEY (lugar_id) REFERENCES lugares(id_lugar)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_seguimiento_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id_cliente)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_seguimiento_prioridad
        FOREIGN KEY (prioridad_id) REFERENCES prioridades(id_prioridad)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE seguimiento_perdido (
    id_perdido INT AUTO_INCREMENT PRIMARY KEY,
    seguimiento_id INT NOT NULL UNIQUE,

    fecha_perdida DATETIME NOT NULL,
    fecha_ultima_vez_visto DATETIME NULL,

    CONSTRAINT fk_seguimiento_perdido
        FOREIGN KEY (seguimiento_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE seguimiento_encontrado (
    id_encontrado INT AUTO_INCREMENT PRIMARY KEY,
    seguimiento_id INT NOT NULL UNIQUE,

    fecha_encontrado DATETIME NOT NULL,
    estado_custodia_id INT NOT NULL,

    CONSTRAINT fk_seguimiento_encontrado
        FOREIGN KEY (seguimiento_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_custodia_encontrado
        FOREIGN KEY (estado_custodia_id) REFERENCES estado_custodia(id_estado_custodia)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


-- 7. HISTORIALES, ACTUALIZACIONES, IMÁGENES Y GESTIÓN


-- Se agrega modificado_por para saber quién generó el cambio de estado,
-- cuando esa información está disponible (ver trigger tr_historial_estado).
CREATE TABLE historial_estado_seguimiento (
    id_historial INT PRIMARY KEY AUTO_INCREMENT,
    seguimiento_id INT NOT NULL,

    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50),
    modificado_por INT NULL,

    fecha_cambio DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_seguimiento
        FOREIGN KEY (seguimiento_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_historial_admin_estado
        FOREIGN KEY (modificado_por) REFERENCES administrador(id_admin)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE historial_estado_custodia (
    id_historial_custodia INT AUTO_INCREMENT PRIMARY KEY,
    seguimiento_encontrado_id INT NOT NULL,
    estado_custodia_anterior INT NULL,
    estado_custodia_nuevo INT NOT NULL,
    modificado_por INT NULL,
    fecha_cambio DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_hcustodia_encontrado
        FOREIGN KEY (seguimiento_encontrado_id) REFERENCES seguimiento_encontrado(id_encontrado)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hcustodia_anterior
        FOREIGN KEY (estado_custodia_anterior) REFERENCES estado_custodia(id_estado_custodia)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_hcustodia_nuevo
        FOREIGN KEY (estado_custodia_nuevo) REFERENCES estado_custodia(id_estado_custodia)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_hcustodia_admin
        FOREIGN KEY (modificado_por) REFERENCES administrador(id_admin)
        ON DELETE SET NULL ON UPDATE CASCADE
);


CREATE TABLE actualizaciones_seguimiento (
    id_actualizacion INT PRIMARY KEY AUTO_INCREMENT,
    seguimiento_id INT NOT NULL,
    mensaje TEXT NOT NULL,
    persona_id INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_actualizacion_seguimiento
        FOREIGN KEY (seguimiento_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_persona_actualiza
        FOREIGN KEY (persona_id) REFERENCES personas(id_persona)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE imagenes_seguimiento (
    id_imagen INT PRIMARY KEY AUTO_INCREMENT,
    seguimiento_id INT NOT NULL,
    ruta_imagen VARCHAR(255) NOT NULL,
    imagen_principal BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_imagen_seguimiento
        FOREIGN KEY (seguimiento_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE
);



CREATE TABLE gestion_seguimiento (
    id_gestion INT PRIMARY KEY AUTO_INCREMENT,
    seguimiento_id INT NOT NULL,
    fecha_seguimiento DATETIME NOT NULL,
    accion VARCHAR(50) NOT NULL,
    observacion TEXT,
    administrador_id INT NULL,

    CONSTRAINT fk_gestion_seguimiento
        FOREIGN KEY (seguimiento_id) REFERENCES seguimiento(id_seguimiento)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_gestion_administrador
        FOREIGN KEY (administrador_id) REFERENCES administrador(id_admin)
        ON UPDATE CASCADE ON DELETE SET NULL
);


-- 8. SOPORTE, CONSEJOS Y CONTROL DE ACCIONES


CREATE TABLE mensajes_de_soporte (
    id_mensaje INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    mensaje_cliente TEXT NOT NULL,
    fecha_mensaje DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_soporte_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id_cliente)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE consejos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO consejos (titulo, descripcion) VALUES
('Hidratación', 'Mantén siempre agua fresca disponible para tu mascota.'),
('Vacunación', 'Lleva a tu mascota al veterinario para mantener sus vacunas al día.'),
('Alimentación', 'Proporciona una dieta equilibrada adecuada para su edad.'),
('Ejercicio', 'Realiza paseos diarios y actividades físicas con tu mascota.');

CREATE TABLE control_acciones (
    id_control INT AUTO_INCREMENT PRIMARY KEY,
    descripcion TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE ingresos_refugio (
    id_ingreso INT AUTO_INCREMENT PRIMARY KEY,
    refugio_id INT NOT NULL,
    seguimiento_id INT NOT NULL,

    fecha_ingreso DATETIME DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,

    estado ENUM('pendiente', 'confirmado', 'rechazado') DEFAULT 'pendiente',

    administrador_id INT NULL,
    fecha_verificacion DATETIME NULL,

    CONSTRAINT fk_ingreso_refugio
        FOREIGN KEY (refugio_id) REFERENCES refugios(id_refugio)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_ingreso_seguimiento
        FOREIGN KEY (seguimiento_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_ingreso_administrador
        FOREIGN KEY (administrador_id) REFERENCES administrador(id_admin)
        ON DELETE SET NULL ON UPDATE CASCADE
);


-- 10. DONACIONES Y COINCIDENCIAS

CREATE TABLE donaciones (
    id_donacion INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,

    monto DECIMAL(10,2) NOT NULL,
    mensaje VARCHAR(255),
    fecha_donacion DATETIME DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_donacion_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id_cliente)
        ON DELETE CASCADE ON UPDATE CASCADE
);



CREATE TABLE coincidencias (
    id_coincidencia INT AUTO_INCREMENT PRIMARY KEY,
    seguimiento_perdido_id INT NOT NULL,
    seguimiento_encontrado_id INT NOT NULL,

    porcentaje_coincidencia DECIMAL(5,2) NULL,
    observacion TEXT,

    estado ENUM('pendiente', 'confirmada', 'descartada') DEFAULT 'pendiente',
    fecha_coincidencia DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_coincidencia_perdido
        FOREIGN KEY (seguimiento_perdido_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_coincidencia_encontrado
        FOREIGN KEY (seguimiento_encontrado_id) REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT uq_coincidencia
        UNIQUE (seguimiento_perdido_id, seguimiento_encontrado_id),

    CONSTRAINT chk_coincidencia_porcentaje
        CHECK (porcentaje_coincidencia IS NULL OR (porcentaje_coincidencia BETWEEN 0 AND 100)),

    CONSTRAINT chk_coincidencia_distintos
        CHECK (seguimiento_perdido_id <> seguimiento_encontrado_id)
);


-- 11. VISTAS

CREATE VIEW estadisticas_localidades AS
SELECT
    l.id_localidad,
    l.n_localidad,
    l.poblacion,
    COUNT(s.id_seguimiento) AS total_seguimientos,
    CASE
        WHEN l.poblacion IS NULL OR l.poblacion = 0 THEN 0
        ELSE ROUND((COUNT(s.id_seguimiento) / l.poblacion) * 100000, 2)
    END AS seguimientos_por_100000_habitantes
FROM localidades l
LEFT JOIN lugares lu ON lu.localidad_id = l.id_localidad
LEFT JOIN seguimiento s ON s.lugar_id = lu.id_lugar
GROUP BY l.id_localidad, l.n_localidad, l.poblacion;


-- 12. TRIGGERS
-- =========================================================
 
DELIMITER //
CREATE TRIGGER tr_historial_estado
AFTER UPDATE ON seguimiento
FOR EACH ROW
BEGIN
    IF NOT (OLD.estado_seguimiento <=> NEW.estado_seguimiento) THEN
        INSERT INTO historial_estado_seguimiento (
            seguimiento_id, estado_anterior, estado_nuevo, modificado_por, fecha_cambio
        )
        VALUES (
            NEW.id_seguimiento, OLD.estado_seguimiento, NEW.estado_seguimiento,
            @admin_actual, CURRENT_TIMESTAMP
        );
    END IF;
END //
DELIMITER ;
 
-- Bitácora mínima que sobrevive al ON DELETE CASCADE de seguimiento
-- (que de otro modo borraría todo el historial normalizado sin dejar rastro).
DELIMITER //
CREATE TRIGGER tr_seguimiento_eliminado
BEFORE DELETE ON seguimiento
FOR EACH ROW
BEGIN
    INSERT INTO control_acciones (descripcion)
    VALUES (
        CONCAT(
            'Se eliminó el seguimiento ID: ', OLD.id_seguimiento,
            ' - Título: ', OLD.titulo,
            ' - Tipo: ', OLD.tipo_seguimiento,
            ' - Animal ID: ', OLD.animal_id,
            ' - Cliente ID: ', OLD.cliente_id,
            ' - Estado al eliminar: ', OLD.estado_seguimiento
        )
    );
END //
DELIMITER ;
 

 
-- Al confirmarse un ingreso a refugio, sincroniza automáticamente la
-- custodia del seguimiento_encontrado correspondiente
DELIMITER //
CREATE TRIGGER tr_sync_custodia_refugio
AFTER UPDATE ON ingresos_refugio
FOR EACH ROW
BEGIN
    IF NOT (OLD.estado <=> NEW.estado) AND NEW.estado = 'confirmado' THEN
        SET @admin_actual = NEW.administrador_id;
 
        UPDATE seguimiento_encontrado
        SET estado_custodia_id = (
            SELECT id_estado_custodia FROM estado_custodia
            WHERE nombre = 'En un refugio' LIMIT 1
        )
        WHERE seguimiento_id = NEW.seguimiento_id;
 
        SET @admin_actual = NULL;
    END IF;
END //
DELIMITER ;
 
-- Historial de cambios de custodia. Se dispara con cualquier UPDATE que
-- realmente cambie estado_custodia_id, sin importar si vino del trigger
-- de sincronización automática o del procedimiento cambiar_estado_custodia.
DELIMITER //
CREATE TRIGGER tr_historial_estado_custodia
AFTER UPDATE ON seguimiento_encontrado
FOR EACH ROW
BEGIN
    IF NOT (OLD.estado_custodia_id <=> NEW.estado_custodia_id) THEN
        INSERT INTO historial_estado_custodia (
            seguimiento_encontrado_id, estado_custodia_anterior, estado_custodia_nuevo,
            modificado_por, fecha_cambio
        )
        VALUES (
            NEW.id_encontrado, OLD.estado_custodia_id, NEW.estado_custodia_id,
            @admin_actual, CURRENT_TIMESTAMP
        );
    END IF;
END //
DELIMITER ;
 

 

 

-- Una coincidencia debe relacionar un reporte perdido con uno encontrado.
DELIMITER //
CREATE TRIGGER trg_check_coincidencia
BEFORE INSERT ON coincidencias
FOR EACH ROW
BEGIN
    DECLARE v_tipo_perdido ENUM('perdido','encontrado');
    DECLARE v_tipo_encontrado ENUM('perdido','encontrado');
 
    SELECT tipo_seguimiento INTO v_tipo_perdido
        FROM seguimiento WHERE id_seguimiento = NEW.seguimiento_perdido_id;
    SELECT tipo_seguimiento INTO v_tipo_encontrado
        FROM seguimiento WHERE id_seguimiento = NEW.seguimiento_encontrado_id;
 
    IF v_tipo_perdido IS NULL OR v_tipo_encontrado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Uno de los seguimientos referenciados no existe';
    END IF;
 
    IF v_tipo_perdido <> 'perdido' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'seguimiento_perdido_id debe corresponder a un seguimiento de tipo perdido';
    END IF;
 
    IF v_tipo_encontrado <> 'encontrado' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'seguimiento_encontrado_id debe corresponder a un seguimiento de tipo encontrado';
    END IF;
END //
DELIMITER ;
 

-- 13. PROCEDIMIENTOS ALMACENADOS

 
DELIMITER //
CREATE PROCEDURE agregar_actualizacion (
    IN p_seguimiento_id INT,
    IN p_persona_id INT,
    IN p_mensaje TEXT
)
BEGIN
    DECLARE v_existe_seg INT DEFAULT 0;
    DECLARE v_existe_persona INT DEFAULT 0;
 
    SELECT COUNT(*) INTO v_existe_seg FROM seguimiento WHERE id_seguimiento = p_seguimiento_id;
    IF v_existe_seg = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seguimiento no encontrado';
    END IF;
 
    IF p_persona_id IS NOT NULL THEN
        SELECT COUNT(*) INTO v_existe_persona FROM personas WHERE id_persona = p_persona_id;
        IF v_existe_persona = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Persona no encontrada';
        END IF;
    END IF;
 
    INSERT INTO actualizaciones_seguimiento (
        seguimiento_id, mensaje, persona_id, created_at, updated_at
    )
    VALUES (
        p_seguimiento_id, p_mensaje, p_persona_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );
END //
DELIMITER ;
 

DELIMITER //
CREATE PROCEDURE registrar_ingreso_refugio (
    IN p_refugio_id INT,
    IN p_seguimiento_id INT,
    IN p_observacion TEXT
)
BEGIN
    DECLARE v_tipo ENUM('perdido','encontrado');
    DECLARE v_existe_refugio INT DEFAULT 0;
    DECLARE v_existe_pendiente INT DEFAULT 0;
 
    SELECT COUNT(*) INTO v_existe_refugio FROM refugios WHERE id_refugio = p_refugio_id;
    IF v_existe_refugio = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Refugio no encontrado';
    END IF;
 
    SELECT tipo_seguimiento INTO v_tipo FROM seguimiento WHERE id_seguimiento = p_seguimiento_id;
    IF v_tipo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seguimiento no encontrado';
    END IF;
    IF v_tipo <> 'encontrado' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se pueden ingresar a un refugio seguimientos de tipo encontrado';
    END IF;
 
    SELECT COUNT(*) INTO v_existe_pendiente
    FROM ingresos_refugio
    WHERE seguimiento_id = p_seguimiento_id AND estado IN ('pendiente', 'confirmado');
    IF v_existe_pendiente > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ya existe un ingreso pendiente o confirmado para este seguimiento';
    END IF;
 
    INSERT INTO ingresos_refugio (refugio_id, seguimiento_id, observacion, estado)
    VALUES (p_refugio_id, p_seguimiento_id, p_observacion, 'pendiente');
END //
DELIMITER ;
 

DELIMITER //
CREATE PROCEDURE cambiar_estado_seguimiento (
    IN p_seguimiento_id INT,
    IN p_nuevo_estado VARCHAR(30),
    IN p_admin_id INT
)
BEGIN
    DECLARE v_existe INT DEFAULT 0;
 
    SELECT COUNT(*) INTO v_existe FROM seguimiento WHERE id_seguimiento = p_seguimiento_id;
    IF v_existe = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El seguimiento no existe';
    END IF;
 
    IF p_nuevo_estado NOT IN ('activo', 'reunido', 'cerrado') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Estado no válido para un seguimiento';
    END IF;
 
    SET @admin_actual = p_admin_id;
 
    UPDATE seguimiento
    SET estado_seguimiento = p_nuevo_estado
    WHERE id_seguimiento = p_seguimiento_id;
 
    SET @admin_actual = NULL;
END //
DELIMITER ;
 

DELIMITER //
CREATE PROCEDURE cambiar_estado_custodia (
    IN p_seguimiento_encontrado_id INT,
    IN p_nuevo_estado_custodia_id INT,
    IN p_admin_id INT
)
BEGIN
    DECLARE v_existe_seg INT DEFAULT 0;
    DECLARE v_existe_estado INT DEFAULT 0;
 
    SELECT COUNT(*) INTO v_existe_seg
    FROM seguimiento_encontrado
    WHERE id_encontrado = p_seguimiento_encontrado_id;
    IF v_existe_seg = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El seguimiento encontrado no existe';
    END IF;
 
    SELECT COUNT(*) INTO v_existe_estado
    FROM estado_custodia
    WHERE id_estado_custodia = p_nuevo_estado_custodia_id;
    IF v_existe_estado = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El estado de custodia indicado no existe';
    END IF;
 
    SET @admin_actual = p_admin_id;
 
    UPDATE seguimiento_encontrado
    SET estado_custodia_id = p_nuevo_estado_custodia_id
    WHERE id_encontrado = p_seguimiento_encontrado_id;
 
    SET @admin_actual = NULL;
END //
DELIMITER ;
 