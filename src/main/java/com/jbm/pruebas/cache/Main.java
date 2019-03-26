/*
    Elaborado por: Jairo Medina ©.
    Fecha: Mar 26, 2019
 */

package com.jbm.pruebas.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Jairo Medina
 * @since 1.0
 */
public class Main {
    public static void main(String[] args) {
        //---Creación del caché de Personas---
        MapCache<Integer,Persona> personCache = new MapCache<>();
        
        //---Estableciendo el keyMapper---
        //---Se usa el Id de la persona como Key dentro de la caché---
        personCache.setKeyMapper((Persona p) -> p.getId());
        
        Personas personas = getListPersonas(20);
        
        //---Definición del Function.
        Function<Date,Integer> calculadorEdad = (Date fechaNacimiento) -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaNacimiento);
            Calendar calHoy = Calendar.getInstance();
            calHoy.setTimeInMillis(System.currentTimeMillis());
            int edad = calHoy.get(Calendar.YEAR) - cal.get(Calendar.YEAR);
            return(edad);
        };
        personas.forEach((Persona p) -> p.setCalculadorEdad(calculadorEdad));
        
        Consumer<Persona> imprimePersona = (Persona p) -> {
            System.out.println(p.getId() +" -> "+p.getNombre()+" "+p.getApellido() + " edad("+p.getEdad()+ ") sexo("+p.getGenero()+")");
        };
        
        //---Agregando las 20 personas a la caché---
        personCache.putAll(personas);
        System.out.println("Se agregan 20 personas a la caché");
        personCache.getValues().forEach(imprimePersona);
        
        //---Recuperando la persona con el id 17---
        Persona persona = personCache.get(17);
        System.out.println("\nPersona con id 17");
        imprimePersona.accept(persona);
        
        //---Recuperando la persona de 20 años---
        persona = personCache.get((Persona p) -> p.getEdad()==20);
        System.out.println("\nPersona de 20 años recuperada");
        imprimePersona.accept(persona);
        
        //---Recuperando persona con el id 21 - No existe en la chaché---
        //---Devuelve un objeto Persona por defecto.
        Persona persona21 = getNewPersona(21);
        persona21.setCalculadorEdad(calculadorEdad);
        persona = personCache.getOrDefault(21, persona21);
        System.out.println("\nPersona con id 21 recuperada");
        imprimePersona.accept(persona);
        
        //---Recuperando persona con el id 22 - No existe en la chaché---
        //---Llama al valueMapper para obtener la persona con id 22---
        //---El valueMapper puede ser el acceso a una base de datos para recuperarlo---
        persona = personCache.getOrElse(22, (Integer id) -> {
            Persona per = getNewPersona(id);
            per.setCalculadorEdad(calculadorEdad);
            return(per);
        });
        System.out.println("\nPersona con id 22 recuperada");
        imprimePersona.accept(persona);
        
        //---Agrega una persona con el id 23---
        persona = getNewPersona(23);
        persona.setCalculadorEdad(calculadorEdad);
        personCache.put(persona);
        System.out.println("\nPersona con id 23 agregada");
        imprimePersona.accept(persona);
        
        //---recupera la lista de Mujeres---
        Personas personasMujeres = personCache.<Personas>subList((Persona p) -> p.getGenero().equals("F"), () -> new Personas());
        System.out.println("\nMujeres recuperadas");
        personasMujeres.forEach(imprimePersona);
    }
    
    
    
    public static Personas getListPersonas(int size){
        Personas listaPersonas = new Personas(size);
        int ano = 1995, mes = 1, dia = 1;
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia);
        for(int i=1; i<=size; i++){
            Persona persona = new Persona(i,"Nombre-"+i,"Apellido-"+i);
            persona.setFechaNacimiento(calendario.getTime());
            persona.setGenero(i%2!=0? "F" : "M");
            listaPersonas.add(persona);
            calendario.add(Calendar.YEAR, 1);
        }
        return(listaPersonas);
    }
    
    public static Persona getNewPersona(int idPersona){
        Persona persona = new Persona(idPersona,"Nombre-"+idPersona,"Apellido-"+idPersona);
        int ano = 1995, mes = 1, dia = 1;
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia);
        calendario.add(Calendar.DAY_OF_YEAR, idPersona);
        persona.setFechaNacimiento(calendario.getTime());
        persona.setGenero(idPersona%2!=0? "F" : "M");
        return(persona);
    }
}
