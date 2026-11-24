# Sistema POS para Zapatería

Aplicación web de punto de venta pensada para reemplazar el registro de ventas en libreta de una zapatería: inventario por talla y color, ventas en mostrador, clientes, **fiados con abonos** y reportes del negocio.

Desarrollado con Java 17, Spring Boot 3 y PostgreSQL para un negocio real.


[Pantalla de venta](docs/screenshots/ventas.png)
[Pantalla de venta](docs/screenshots/historial.png)
[Pantalla de venta](docs/screenshots/productos.png)
[Pantalla de venta](docs/screenshots/login.png)
[Pantalla de venta](docs/screenshots/reportes.png)

## Funcionalidades

- **Ventas:** buscador de productos por modelo, marca, talla o color; carrito con total en vivo y rebaja fija por par.
- **Fiados:** anticipo al vender, abonos posteriores con fecha y quién los recibió, y estados de la venta (pagada, fiada, saldada, cancelada).
- **Cancelaciones:** devuelven el stock al inventario y quedan en el historial.
- **Inventario:** productos con variantes por talla y color, control de stock y alta/baja de productos.
- **Clientes y usuarios:** alta de clientes, y alta de vendedores con roles.
- **Reportes:** vendido y cobrado por día, deudores, productos más vendidos y stock bajo, con gráficas.
- **Seguridad:** inicio de sesión, tres roles y contraseñas cifradas con BCrypt.



## Roles

| Rol | Puede |
|---|---|
| `SYSADMIN` | Todo: reportes, usuarios, productos y ventas |
| `ADMIN` | Productos, inventario, ventas y cancelaciones |
| `USER` | Registrar ventas, abonos y consultar clientes e historial |

## Tecnologías

| Área | Herramientas |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring MVC, Spring Data JPA / Hibernate, Spring Security, Bean Validation |
| Base de datos | PostgreSQL |
| Frontend | Thymeleaf, Tailwind CSS, JavaScript, Chart.js |
| Pruebas | JUnit 5, Mockito, AssertJ |
| Herramientas | Maven (multimódulo), Git |

## Arquitectura

Proyecto Maven multimódulo con dependencias en una sola dirección:

```
zapateria-model     entidades JPA y enums
zapateria-dao       repositories de Spring Data
zapateria-service   lógica de negocio y DTOs
zapateria-security  configuración de Spring Security
zapateria-web       controllers, vistas Thymeleaf y arranque de la aplicación
```

`web` depende de `security` y `service`; `service` depende de `dao`; `dao` depende de `model`. `model` no depende de ningún otro módulo.

## Cómo ejecutarlo

**Requisitos:** JDK 17, Maven 3.9 o superior y PostgreSQL 16.

1. Creá la base de datos y cargá la estructura con los datos de demostración:

   ```bash
   psql -U postgres -c "CREATE DATABASE zapateria_db;"
   psql -U postgres -d zapateria_db -f database/zapateria_db_demo.sql
   ```

2. Copiá la configuración de ejemplo y completá tu contraseña de PostgreSQL:

   ```bash
   cp zapateria-web/src/main/resources/application-dev.properties.example \
      zapateria-web/src/main/resources/application-dev.properties
   ```

3. Compilá y arrancá:

   ```bash
   mvn clean install
   mvn spring-boot:run -pl zapateria-web
   ```

4. Abre <http://localhost:8080> e iniciá sesión con el usuario de demostración:

   | Email | Contraseña |
   |---|---|
   | `admin@demo.com` | `Demo1234!` |

   > Estas credenciales son solo para pruebas locales. En un uso real, cambiá la contraseña de inmediato.

## Pruebas

```bash
mvn test
```

Las pruebas unitarias cubren la lógica de ventas con JUnit 5 y Mockito: aplicación de la rebaja, descuento de stock, validación de stock insuficiente, reglas de fiado, abonos y cancelaciones con devolución de inventario.

## Decisiones técnicas

- **`BigDecimal` para el dinero**, nunca `double`, para evitar errores de redondeo.
- **Transacciones (`@Transactional`)** en ventas y cancelaciones: o se guarda todo (venta, detalle, pagos y stock) o no se guarda nada.
- **El precio unitario se guarda en cada línea de venta**, así un cambio de precio posterior no altera el historial.
- **DTOs** entre las vistas y los services, para no exponer las entidades.
- **Cada cobro se registra como un pago con fecha**, lo que permite saber cuánto dinero entró cada día y no solo cuánto se vendió.
- **Perfiles `dev` y `prod`:** la configuración de producción lee las credenciales de variables de entorno; en producción Hibernate solo valida el esquema (`ddl-auto=validate`).

## Estructura de la base de datos

El esquema completo está en [`database/zapateria_db_demo.sql`](database/zapateria_db_demo.sql): usuarios, roles, categorías, productos, variantes, clientes, ventas, detalle de venta y pagos.

## Autor

**Angel de Jesus Estrada Estrada** — Desarrollador Backend Java Jr.

- GitHub: [angelestradaestrada65-glitch](https://github.com/angelestradaestrada65-glitch)
- Correo: angelestradaestrada65@gmail.com
