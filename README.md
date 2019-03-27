# Simple Caché con Map<K,V>

## Qué es una Caché?
Es una parte de la memoria utilizada para almacenar datos de uso frecuente. El objetivo principal de utilizar una Caché es para no tener que ir a recuperar datos desde un disco, porque son medios muy lentos y hacen bajar el rendimiento de cualquier aplicación que requiera de acceso a datos.

## Caché en Java
En Java Enterprise Edition, (**JavaEE**) existe el paquete [javax.cache](https://static.javadoc.io/javax.cache/cache-api/1.0.0/index.html?javax/cache/) con un conjunto de interfaces que deben ser implementadas para tener a disposición a toda esta funcionalidad de una Caché; es significativamente complejo realizar una implementación de estas interfaces, así que por lo general se utilizan implementaciones de terceros como **Hazelcast** y  **Oracle Coherence**; estas disponen de funcionalidades impresionantes y muy útiles que van más allá de una Caché; implementan los conceptos de **IMDG** (In Memory Data Grid), donde se puede temporizar la vida de los datos en caché, configurar listener para saber cuando se modifica un dato de la caché, incluso definir procedimientos **loader** que se ocupan de cargar los datos a la caché cuando son solicitados, también hay procedimientos **writer** que escriben los cambios en la base de datos después que son actualizados en caché.

