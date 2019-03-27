# Simple Caché con Map<K,V>

## Qué es una Caché?
Es una parte de la memoria utilizada para almacenar datos de uso frecuente. El objetivo principal de utilizar una Caché es para no tener que ir a recuperar datos desde un disco, porque son medios muy lentos y hacen bajar el rendimiento de cualquier aplicación que requiera de acceso a datos.

## Caché en Java
En Java Enterprise Edition, (**JavaEE**) existe el paquete [javax.cache](https://static.javadoc.io/javax.cache/cache-api/1.0.0/index.html?javax/cache/) con un conjunto de interfaces que deben ser implementadas para tener a disposición a toda esta funcionalidad de una Caché; es significativamente complejo realizar una implementación de estas interfaces, así que por lo general se utilizan implementaciones de terceros como **Hazelcast** y  **Oracle Coherence**; estas disponen de funcionalidades impresionantes y muy útiles que van más allá de una Caché; implementan los conceptos de **IMDG** (In Memory Data Grid), donde se puede temporizar la vida de los datos en caché, configurar listener para saber cuando se modifica un dato de la caché, incluso definir procedimientos **loader** que se ocupan de cargar los datos a la caché cuando son solicitados, también hay procedimientos **writer** que escriben los cambios en la base de datos después que son actualizados en caché.

## Ejemplo con Java
En este caso tenemos un ejemplo básico de los conceptos de una caché implementada con una clase que contiene un [Map<K,V>](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html) donde se guardan los valores. La clase la he llamado **MapCache** y tiene dos parámetros genéricos **K** y **V** donde *K* representa el tipo de objeto a utilizar como *Key*, *V* representa el tipo de objeto a utilizar como *valor*. Para los que no conocen la interface **java.util.Map**, esta representa algo así como una tabla, donde cada entrada está identificada por una clave (**K** o key) y que tiene asociado un único valor (**V**) de manera que se puede acceder de forma directa a un valor, conociendo su correspondiente clave (*K*). <br/>
![alt text](https://github.com/jmedinaJBM/SimpleCache/blob/master/Tabla_MAP.png)

### Requisitos del Proyecto Java
1. [NetBeans 8.2](https://netbeans.org/downloads/)
2. [Java SE JDK 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html?fbclid=IwAR21GQMtgfZY7ZzLscX538bwGPkzqT8ap2jXCFUy0Ycnmxqy4hEDja7XPJo) update más reciente.
3. [Apache Maven 3.6](https://www-us.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.zip?fbclid=IwAR2pO8S7v5Frm0eKYDoTemFWSu7w0fIYOIXsDrmrthNlUKGHQbF6uN5TkoM)

## Definición de la Clase MapCache<K,V>
La clase **MapCache** está definida con genéricos (**K**,**V**) para que pueda manejar cualquier tipo de objeto como clave (K) y como valor (V).  Primero tenemos la declaración de la clase  `public class MapCache<K,T>` He usado **T** en lugar de *V*, pero da igual, no pasa nada.
