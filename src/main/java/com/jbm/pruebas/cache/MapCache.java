/*
    Elaborado por: Jairo Medina ©.
    Fecha: Mar 25, 2019
 */

package com.jbm.pruebas.cache;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Una implementación simple de una Caché utilizando un {@link java.util.Map}. <br>
 * Este Map es Thread safety.
 * @author Jairo Medina
 * @param <K> Es el Tipo de objeto utilizado para los key en el {@code Map}.
 * @param <T> Tipo de objeto a manejar en el {@code MapCache}.
 * @since 1.0
 */
public class MapCache<K,T> {
    private static final String     KEYMAPPER_NOPRESENT = "KeyMapper no definido.";
    
    private ConcurrentHashMap<K,T>  mapCache;
    
    private Optional<Function<T,K>> keyMapper;
    
    
    //---Constructores---
    //******************************************************************************************************************
    /**
     * Crea una instancia de esta clase.
     */
    public MapCache(){
        this.initialize(null);
    }
    
    /**
     * Crea una instancia de esta clase con el {@code keyMapper}.
     * @param keyMapper Un mapper que devuelve el {@code key} del valor dado de tipo {@code T}.
     */
    public MapCache(Function<T,K> keyMapper){
        this.initialize(keyMapper);
    }
    
    //---Propiedades---
    //******************************************************************************************************************
    /**
     * Devuelve el {@code keyMapper} establecido. <br>Este mapper, calcula la clave a utilizar en el {@link java.util.Map}
     * para cada valor.
     * @return El mapper que calcula la clave de cada objeto en esta caché.
     */
    public Function<T,K>    getKeyMapper(){
        return(this.keyMapper.orElse(null));
    }
    
    /**
     * Establece el {@code keyMapper} a utilizar.
     * @param keyMapper Mapper que calcula la clave de cada objeto en esta caché.
     */
    public void             setKeyMapper(Function<T,K> keyMapper){
        this.keyMapper = Optional.ofNullable(keyMapper);
    }
    
    //---Operaciones---
    //******************************************************************************************************************
    /**
     * Devuelve el objeto {@code T} asociado al {@code key}.
     * @param key Key del objeto a devolver.
     * @return Objeto recuperado. Null si no existe un objeto asociado al {@code key}.
     */
    public T        get(K key){
        return(this.mapCache.get(key));
    }
    
    /**
     * Devuelve el primer objeto {@code T} que cumpla con la condición dada en {@code filter}.
     * @param filter Condición a evaluar.
     * @return Objeto recuperado. Null si no existe un objeto que cumpla con la condición.
     */
    public T        get(Predicate<T> filter){
        return(this.getByFilter(filter).orElse(null));
    }
    
    /**
     * Devuelve el objeto {@code T} asociado al {@code key}; si no hay un objeto asociado al {@code key}, devuelve
     * {@code defaultValue}. 
     * @param key Key del objeto a devolver.
     * @param defaultValue Valor a devolver si no hay un objeto asociado al {@code key}.
     * @return Objeto recuperado o {@code defaultValue}.
     */
    public T        getOrDefault(K key, T defaultValue){
        return(this.mapCache.getOrDefault(key, defaultValue));
    }
    
    /**
     * Devuelve el objeto {@code T} asociado al {@code key}; si no hay un objeto asociado al {@code key}, llama al 
     * {@code valueMapper} con el {@code key} dado. El valor devuelto por {@code valueMapper} se agrega a la caché,
     * excepto si devuelve null.
     * @param key Key del objeto a devolver.
     * @param valueMapper Una función que es llamada en caso de que no exista un valor asociado al {@code key}.
     * @return Objeto recuperado. Null si no hay un valor en la caché asociado al {@code key} y el {@code valueMapper} retorna null.
     */
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
    
    /**
     * Devuelve el primer objeto {@code T} que cumpla con la condición dada en {@code filter}; si no hay un objeto 
     * que cumpla la condición, llama a {@code valueSupplier} y el valor que retorne se agrega a la caché,
     * excepto si devuelve null.
     * @param filter  Condición a evaluar.
     * @param valueSupplier Provee un objeto en caso que no se encuentre uno en la caché que cumpla la condición.
     * @return Objeto recuperado. Null si no hay un valor en la caché que cumpla la condición y el {@code valueSupplier} 
     * retorna null.
     */
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
    
    /**
     * Devuelve el primer objeto {@code T} que cumpla con la condición dada en {@code filter}; si no hay un objeto
     * que cumpla la condición, lanza la excepción que proporciona {@code exceptionSupplier}.
     * @param <X> Tipo que proporciona el {@code exceptionSupplier}.
     * @param filter Condición a evaluar.
     * @param exceptionSupplier Proveedor de la excepción que será lanzada si no encuentra un objeto que cumpla la condición.
     * @return Objeto recuperado.
     * @throws X Si no encuentra un objeto que cumpla la condición.
     */
    public  <X extends Throwable> T getOrElseThrow(Predicate<T> filter, Supplier<? extends X> exceptionSupplier ) throws X{
        Optional<T> value = this.getByFilter(filter);
        return(value.<X>orElseThrow(exceptionSupplier)); 
    }
    
    /**
     * Devuelve el objeto {@code T} asociado al {@code key} o lanza la {@code exception} proporcionada por {@code exceptionSupplier}.
     * @param <X> Tipo que proporciona el {@code exceptionSupplier}.
     * @param key Key del objeto a devolver.
     * @param exceptionSupplier Proveedor de la excepción.
     * @return Objeto recuperado.
     * @throws X Si no hay un objeto asociado al {@code key}.
     */
    public <X extends Throwable> T getOrElseThrow(K key, Supplier<? extends X> exceptionSupplier ) throws X{
        Optional<T> value = Optional.ofNullable(this.mapCache.get(key));
        return(value.<X>orElseThrow(exceptionSupplier)); 
    }
    
    /**
     * Devuelve los objetos que cumplen con la condición dada por {@code filter}.
     * @param filter Condición a evaluar.
     * @return Lista de objetos que cumplen la condición.
     */
    public List<T>  subList(Predicate<T> filter){
        return(this.getLlistByFilter(filter));
    }
    
    /**
     * Devuelve los objetos que cumplen con la condición dada por {@code filter}, en una colección proporcionada por
     * {@code collectionFactory}.
     * @param <C> Tipo de Colección.
     * @param filter Condición a evaluar.
     * @param collectionFactory Proveedor de la colección.
     * @return Lista de objetos que cumplen la condición.
     */
    public <C extends Collection<T>> C  subList(Predicate<T> filter, Supplier<C> collectionFactory){
        return(this.mapCache.values().stream().filter(filter).collect(Collectors.toCollection(collectionFactory)));
    }
    
    /**
     * Devuelve todos lo objetos registrados en la caché como un {@link java.util.List}.
     * @return Lista de objetos.
     */
    public List<T>  getValues(){
        return(this.mapCache.values().stream().collect(Collectors.toList()));
    }
    
    /**
     * Agrega el objeto {@code value} al caché utilizando el {@code keyMapper} que se ha establecido.
     * @param value Objeto a agregar.
     * @return Objeto agregado. Null si no se agregó.
     */
    public T        put(T value){
        return(this.mapCache.put(this.getKey(value), value));
    }
    
    /**
     * Agrega el objeto {@code value} al caché con el {@code key} dado.
     * @param key key del valor a agregar.
     * @param value Objeto a agrergar.
     * @return Objeto agregado. Null si no se agregó.
     */
    public T        put(K key, T value){
        return(this.mapCache.put(key, value));
    }
    
    /**
     * Agrega el objeto proporcionado por {@code valueSupplier} con el {@code key} que proporciona el {@code keyMapper}
     * que se ha establecido.
     * @param valueSupplier Proveedor del valor a agregar.
     * @return Objeto agregado. Null si no se agregó.
     */
    public T        put(Supplier<T> valueSupplier){
        T value = valueSupplier.get();
        return(this.mapCache.put(this.getKey(value), value));
    }
    
    /**
     * Agrega el objeto {@code value} al caché utilizando el {@code keyMapper} que se ha establecido y después 
     * llama a {@code action}.
     * @param value Objeto a agregar.
     * @param action Acción a ejecutar después de agregar {@code value} a la caché.
     * @return Objeto agregado. Null si no se agregó.
     */
    public T        put(T value, Consumer<T> action){
        this.mapCache.put(this.getKey(value), value);
        action.accept(value);
        return(value);
    }
    
    /**
     * Agrega el objeto {@code value} al caché con el {@code key} dado y después llama a {@code action}.
     * @param key key del valor a agregar.
     * @param value Objeto a agrergar.
     * @param action Acción a ejecutar después de agregar {@code value} a la caché.
     * @return Objeto agregado. Null si no se agregó.
     */
    public T        put(K key, T value, BiConsumer<K,T> action){
        this.mapCache.put(key, value);
        action.accept(key, value);
        return(value);
    }
    
    /**
     * Agrega el objeto proporcionado por {@code valueSupplier} con el {@code key} que proporciona el {@code keyMapper}
     * que se ha establecido y después llama a {@code action}.
     * @param valueSupplier Proveedor del valor a agregar.
     * @param action Acción a ejecutar después de agregar el valor a la caché.
     * @return Objeto agregado. Null si no se agregó.
     */
    public T        put(Supplier<T> valueSupplier, Consumer<T> action){
        T value = valueSupplier.get();
        this.mapCache.put(this.getKey(value), value);
        action.accept(value);
        return(value);
    }
    
    /**
     * Agrega a la caché todos los objetos de la lista {@code valueList}.
     * @param valueList Lista de objetos a agregar.
     */
    public void     putAll(List<T> valueList){
        for(T value: valueList){
            this.mapCache.put(this.getKey(value), value);
        }
    }
    
    /**
     * Agrega a la caché todos los objetos de la lista {@code valueList}. Para cada objeto agregado ejecuta {@code action}.
     * @param valueList Lista de objetos a agregar.
     * @param action Acción a ejecutar para cada objeto agregado.
     */
    public void     putAll(List<T> valueList, Consumer<T> action){
        for(T value: valueList){
            this.mapCache.put(this.getKey(value), value);
            action.accept(value);
        }
    }
    
    /**
     * Elimina de la caché el objeto asociado al {@code key}.
     * @param key Clave del objeto a eliminar de la caché.
     * @return Objeto eliminado. Null si no encontró un objeto asociado al {@code key}.
     */
    public T        remove(K key){
        return(this.mapCache.remove(key));
    }
    
    /**
     * Elimina de la caché los objetos que cumplan con la condición dada en {@code filter}.
     * @param filter Condición a evaluar.
     * @return Lista de objetos eliminados.
     */
    public List<T>  removeIf(Predicate<T> filter){
        List<T> valueList = this.getLlistByFilter(filter);
        for(T value: valueList){
            this.mapCache.remove(this.getKey(value));
        }
        return(valueList);
    }
    
    /**
     * Elimina de la caché el objeto dado en {@code value}.
     * @param value Objeto a eliminar.
     * @return {@code True}: Objeto eliminado. {@code False}: Objeto no existe.
     */
    public boolean  removeIfPresent(T value){
        return(this.mapCache.remove(this.getKey(value), value));
    }
    
    /**
     * Determina si hay un objeto asociado al {@code key}.
     * @param key Clave del objeto buscado.
     * @return {@code True}: Existe un objeto asociado al {@code key}. 
     */
    public boolean  containsKey(K key){
        return(this.mapCache.containsKey(key));
    }
    
    /**
     * Determina si el {@code value} existe en la chacé.
     * @param value Valor a buscar.
     * @return {@code True}: El valor existe. {@code False}: El valor no existe.
     */
    public boolean  containsValue(T value){
        return(this.mapCache.containsValue(value));
    }
    
    /**
     * Devuelve el {@code key} asociado al objeto dado en {@code value} registrado en la caché.
     * El {@code key} es calculado por el {@code keyMapper} que se ha establecido.
     * @param value Valor del cual se requiere el {@code key}.
     * @return {@code key} recuperado.
     * @throws IllegalStateException Si no se ha definido el {@code keyMapper}.
     */
    public K        getKey(T value) throws IllegalStateException{
        return(this.keyMapper.orElseThrow(this.getExceptionKeyMapperNoPresent()).apply(value));
    }
    
    public int      size(){
        return(this.mapCache.size());
    }
    
    //---Privados---
    //******************************************************************************************************************
    private Optional<T> getByFilter(Predicate<T> filter){
        return(this.mapCache.values().stream().filter(filter).findFirst());
    }
    private List<T>     getLlistByFilter(Predicate<T> filter){
        return(this.mapCache.values().stream().filter(filter).collect(Collectors.toList()));
    }
    
    //---Exception---
    //******************************************************************************************************************
    private Supplier<IllegalStateException> getExceptionKeyMapperNoPresent(){
        return(() -> this.getException(KEYMAPPER_NOPRESENT));
    }
    private IllegalStateException           getException(String message){
        return(new IllegalStateException(message));
    }
    
    //---Initiialize---
    //******************************************************************************************************************
    private void initialize(Function<T,K> keyMapper){
        this.mapCache   = new ConcurrentHashMap<>();
        this.keyMapper  = Optional.ofNullable(keyMapper);
    }
}
