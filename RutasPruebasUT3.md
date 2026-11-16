Prueba de importación de JSON válido:
datos/ejemplos/catalogo_valido.json
RESULTADO DE LA PRUEBA:

--> AQUÍ APARECE LA UI DEL MENÚ

Selecciona una opción: 2
Introduce la ruta del JSON: datos/ejemplos/catalogo_valido.json

===== IMPORTAR JSON =====
Productos encontrados: 2
Importados: 2 | Rechazados: 0
[OK] Fichero JSON leído e importación finalizada.
Pulsa Intro para continuar...

--> AQUÍ APARECE LA UI DEL MENÚ

Selecciona una opción: 4

---------- CONSULTA DE PRODUCTOS ----------
1. Listar todos los productos
2. Buscar producto por código
3. Buscar productos por nombre
0. Volver al menú principal
Selecciona una opción: 1

===== LISTADO DE PRODUCTOS =====
--------------------------------
Código: RAT-002
Nombre: Ratón inalámbrico
Precio: 14.50 ?
Stock: 25
Categoría: 1
Proveedor: 1
--------------------------------
Código: TEC-001
Nombre: Teclado USB
Precio: 19.95 ?
Stock: 12
Categoría: 1
Proveedor: 1

---------- CONSULTA DE PRODUCTOS ----------
1. Listar todos los productos
2. Buscar producto por código
3. Buscar productos por nombre
0. Volver al menú principal
Selecciona una opción: 

Prueba de importación de JSON inexistente o incorrecto:
datos/ejemplos/catalogo_malformado.json
RESULTADO DE LA PRUEBA:

--> AQUÍ APARECE LA UI DEL MENÚ
Selecciona una opción: 2
Introduce la ruta del JSON: datos/ejemplos/catalogo_malformado.json
[ERROR] El fichero JSON está mal formado o tiene una estructura incorrecta: java.io.EOFException: End of input at line 2 column 1 path $.productos[1]
Pulsa Intro para continuar...

Prueba de MySQL existente
RESULTADO DE LA PRUEBA:

--> AQUÍ APARECE LA UI DEL MENÚ

Selecciona una opción: 11

---------- CONSULTAS Y BÚSQUEDAS EN BD ----------
1. Listar todos los productos (con JOIN)
2. Buscar producto por código exacto
3. Buscar productos por nombre parcial (LIKE)
4. Consultar productos con stock inferior a umbral
0. Volver al menú principal

Selecciona una opción: 2

=== BÚSQUEDA EXACTA POR CÓDIGO ===
Introduce el código exacto del producto: CODIGO_EXISTENTE

[OK] Producto encontrado:
Código    : TEC-001
Nombre    : Teclado USB
Precio    : 19.95 EUR
Stock     : 12
Categoría : 1
Proveedor : 1

Prueba de MySQL inexistente
RESULTADO DE LA PRUEBA:

---------- CONSULTAS Y BÚSQUEDAS EN BD ----------
1. Listar todos los productos (con JOIN)
2. Buscar producto por código exacto
3. Buscar productos por nombre parcial (LIKE)
4. Consultar productos con stock inferior a umbral
0. Volver al menú principal

Selecciona una opción: 2

=== BÚSQUEDA EXACTA POR CÓDIGO ===
Introduce el código exacto del producto: CODIGO_INEXISTENTE

[AVISO] No se ha encontrado ningún producto con el código CODIGO_INEXISTENTE.
