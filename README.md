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

### Definición de la Clase MapCache<K,V>**
La clase **MapCache** está definida con genéricos (**K**,**V**) para que pueda manejar cualquier tipo de objeto como clave (K) y como valor (V).  Primero tenemos la declaración de la clase  **``public class MapCache<K,T>``** He usado **T** en lugar de *V*, pero da igual, no pasa nada.<br/><br/> 
Seguido tenemos la variable **mapCache** que es un *HashMap* concurrente, utilizado como Caché, donde se guardan los valores de tipo **T** con sus correspondiente *key (**K**).
```java
public class MapCache<K,T> {
    private static final String     KEYMAPPER_NOPRESENT = "KeyMapper no definido.";
    
    private ConcurrentHashMap<K,T>  mapCache;
    private Optional<Function<T,K>> keyMapper;
    
    public MapCache(){
        this.initialize(null);
    }
    
    public MapCache(Function<T,K> keyMapper){
        this.initialize(keyMapper);
    }
}
```
<br/>La variable **keyMapper** es un [Function<T,K>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html) que se ocupa de obtener el *Key* (**K**) de un valor  (**T**). El **keyMapper** es una propiedad de la clase y este debe ser definido con el método **setKeyMapper**. En la sección de código puede verse este método; también se puede establecer con uno de los constructores de la clase.
```java
public Function<T,K>    getKeyMapper(){
    return(this.keyMapper.orElse(null));
}

public void             setKeyMapper(Function<T,K> keyMapper){
    this.keyMapper = Optional.ofNullable(keyMapper);
}
```
<br/><br/>
### Los Métodos get
Luego tenemos los métodos **get** y sus variantes: **_getOrDefault_**, **_getOrElse_**, **_getOrElseThrow_**.  Primero los get sencillos, **`get(K key)`** que permite recuperar el valor asociado al *key* (**K**) dado en parámetro; y **`get(Predicate\<T> filter)`**  devuelve el primer valor que encuentre en la caché que cumpla con la condición del *Predicate\<T>*. **`getOrDefault(K key, T defaultValue)`** es similar a *get(K key)* con la diferencia que si no encuentra el valor en la caché, devuelve *defaultValue*.
```java
//---Métodos get sencillos---
public T        get(K key){
    return(this.mapCache.get(key));
}

public T        get(Predicate<T> filter){
    return(this.getByFilter(filter).orElse(null));
}
```
```java
public T        getOrDefault(K key, T defaultValue){
    return(this.mapCache.getOrDefault(key, defaultValue));
}
```
<br/>El método **`getOrElse(K key, Function<K,T> valueMapper)`** devuelve el valor asociado con el *key* (**K**) y si no lo encuentra llama a la función **valueMapper** mandando como parámetro el *key*; esta función es equivalente a un *loader* porque puede ser definida para recuperar el valor buscado desde una base de datos. igual pasa con el método **`getOrElse(Predicate\<T> filter, Supplier\<T> valueSupplier)`** que si no encuentra un valor que cumpla la condición dada por **filter**, devuelve el valor que proporciona **valueSupplier**, el cual puede ser un loader.
```java
public T        getOrElse(K key, Function<K,T> valueMapper){
    Optional<T> value = Optional.ofNullable(this.mapCache.get(key));
    return(value.orElseGet(() -> {
        T val = valueMapper.apply(key);
        if(val!=null){
            this.mapCache.putIfAbsent(key, val);
        }
        return(val);
    }));
}

public T        getOrElse(Predicate<T> filter, Supplier<T> valueSupplier){
    Optional<T> value = this.getByFilter(filter);
    return(value.orElseGet(() -> {
        T val = valueSupplier.get();
        if(val!=null){
            this.mapCache.putIfAbsent(this.getKey(val), val);
        }
        return(val);
    }));
}
```
<br/>Los métodos **getOrElseThrow**, son similar a los *getOrElse*, pero en lugar de usar un *valueMapper* o un *valueSupplier*, lanzan un exception.
```java
public  <X extends Throwable> T getOrElseThrow(Predicate<T> filter, Supplier<? extends X> exceptionSupplier ) throws X{
    Optional<T> value = this.getByFilter(filter);
    return(value.<X>orElseThrow(exceptionSupplier)); 
}

public <X extends Throwable> T getOrElseThrow(K key, Supplier<? extends X> exceptionSupplier ) throws X{
    Optional<T> value = Optional.ofNullable(this.mapCache.get(key));
    return(value.<X>orElseThrow(exceptionSupplier)); 
}
```
<br/><br/>
### Los Métodos put
Estos métodos agregan valores a la caché. **`put(T value)`** agrega *value* a la caché y el *key* es calculado con el *keyMapper* que se ha definido antes. **`put(K key, T value)`** agrega a la caché el valor dado en *value* con el correspondiente *key* proporcionado. **`put(Supplier\<T> valueSupplier>)`** agrega el valor que proporciona *valueSupplier*.
```java
public T        put(T value){
    return(this.mapCache.put(this.getKey(value), value));
}

public T        put(K key, T value){
    return(this.mapCache.put(key, value));
}

public T        put(Supplier<T> valueSupplier){
    T value = valueSupplier.get();
    return(this.mapCache.put(this.getKey(value), value));
}
```
<br/> Lo métodos **`put(T value, Consumer\<T> action)`**, **`put(K key, T value, BiConsumer<K,T> action)`** y  **`put(Supplier\<T> valueSupplier, Consumer\<T> action)`** son similares a los anteriores, pero con la variante de que al agregar el *valor* a la caché, ejecuta un acción proporcionada por el **Consumer\<T> action** o el **BiConsumer\<T> action**.
```java
public T        put(T value, Consumer<T> action){
    this.mapCache.put(this.getKey(value), value);
    action.accept(value);
    return(value);
}

public T        put(K key, T value, BiConsumer<K,T> action){
    this.mapCache.put(key, value);
    action.accept(key, value);
    return(value);
}

public T        put(Supplier<T> valueSupplier, Consumer<T> action){
    T value = valueSupplier.get();
    this.mapCache.put(this.getKey(value), value);
    action.accept(value);
    return(value);
}
```
<br/> El método **`putAll(List\<T> valueList)`** agrega a la caché todos los valores dados en *valueList*. **`putAll(List\<T> valueList, Consumer\<T> action)`** hace lo mismo con la variante que ejecuta el consumer *action* para cada valor agregado a la caché, como lo haría un *writer*, que se ocupa de escribir en una base de datos cada valor agregado a la caché.
```java
public void     putAll(List<T> valueList){
    for(T value: valueList){
        this.mapCache.put(this.getKey(value), value);
    }
}

public void     putAll(List<T> valueList, Consumer<T> action){
    for(T value: valueList){
        this.mapCache.put(this.getKey(value), value);
        action.accept(value);
    }
}
```
