
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ventu
 */
public class ConectaBD { 
      // Declaramos los atributos
    private final String nombre;          //Al iniciar se especificara el nombre de la tabla, para luego mostarrlo en el Log.
    Connection conn; // Para crear la conexión de la base de datos
    Statement sentenciaSQL; // Para realizar las operaciones SQL
    ResultSet rs; // Para almacenar los datos devueltos en la consulta empleado
    String usuario = "plsql";
    String contrasena = "plsql";
    String puerto = "1521";
    boolean nuevoRegistro;

    public ConectaBD(String nom) {
        nombre = nom;
        conn = null; 
        sentenciaSQL = null; 
        rs = null;
        nuevoRegistro = false;
    }  
    
    public void conecta(String usuario, String contrasena, String puerto) throws SQLException {
        String jdbcUrl;
        try {
            //Registramos el Driver de Oracle
            String driver = "oracle.jdbc.OracleDriver";
            Class.forName(driver).newInstance();
            System.out.println("Driver "+ driver +" Registrado correctamente");
            
            //Abrimos la conexión con la Base de Datos
            System.out.println("Conectando con la Base de datos...");
            jdbcUrl = "jdbc:oracle:thin:@localhost:"+puerto+":XE"; // Dirección donde se ubica la base de datos tienda
            // Conectamos con la base de datos usando la dirección, el usuario y la contraseña (por defecto usuario "root" contraseña ""
            conn = DriverManager.getConnection(jdbcUrl,usuario,contrasena);
            
            System.out.println("Conexión establecida con la Base de datos...");
        } 
        catch(SQLException se) 
        {
            //Errores de JDBC
            se.printStackTrace();
        } 
        catch(Exception e) 
        {
            //Errores de Class.forName
            e.printStackTrace();
        }
    }
    
    public void crearSentencias() throws java.sql.SQLException{
        // Crear una sentencia para enviar consultas a la base de datos
        sentenciaSQL = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        System.out.println("\nSentencia creada con éxito.");
    }
    
    // Método para cerrar la conexión de la base de datos
    public void cerrarConexion() throws java.sql.SQLException  {
        // Cerramos la conexión con la BBDD
        if (rs != null) 
            rs.close();

        if (sentenciaSQL != null) 
            sentenciaSQL.close();

        if (conn != null) 
            conn.close();
        
        System.out.println("\nConexion cerrada con éxito.");
    }
    
    // Método para ejecutar la sentencia sql pasada por parámetro
    public void ejecutaSQLq(String sql) throws java.sql.SQLException{
        // Realizamos la consulta y obtenemos los resultados
        rs = sentenciaSQL.executeQuery(sql);
    }
    
    public boolean ejecutaSQL(String sql) throws java.sql.SQLException{
        // Realizamos la consulta y obtenemos los resultados
        return sentenciaSQL.execute(sql);
    }
    
    // Métodos para obtener los diferentes registros de la tabla
    public void irAlFinal() throws java.sql.SQLException{
        try 
        {
            rs.last(); // Obtenemos el último registro
        } 
        catch (Exception e ) 
        {
            e.printStackTrace();
        }
    }
    
    public void irAlSiguiente() throws java.sql.SQLException{
        try 
        {
            rs.next(); // Obtenemos el siguiente registro
        } 
        catch (Exception e ) 
        {
            e.printStackTrace();
        }
    }

    public void irAlAnterior() throws java.sql.SQLException{
        try 
        {
            rs.previous(); // Obtenemos el registro anterior
        } 
        catch (Exception e ) 
        {
            e.printStackTrace();
        }
    }
    
    public void irAlPrimero() throws java.sql.SQLException{
        try 
        {
            rs.first(); // Obtenemos el primer registro
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
  
    // Método para insertar un nuevo1 registro
    public void nuevo() throws java.sql.SQLException{
        nuevoRegistro = true;
        
        try 
        {
            rs.last(); //para que inserte en la ultima fila, parece ser. Si no, inserta siempre en la penultima (Por que?)
            rs.moveToInsertRow(); // Nos preparamos para insertar el nuevo1 registro
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
  
    public void aceptar(int numemp, String nomb, String tarea,String jefe,String fecha, String salario, String departamento) throws java.sql.SQLException{   
        Float salariof; 
        System.out.println("Dentro de aceptar.\nDepartamento:"+departamento+".\t"+"Jefe:"+jefe+".\t"+"NumE:"+numemp+".\t"+"Salario:"+salario+".\t"+"Date:"+fecha+".\t");

         //Hacemos trim de todos los String que nos llegan (numemp, Jefe, Departamento y fecha no es necesario)
         nomb=nomb.trim();
         tarea=tarea.trim();
         salario=salario.trim();
         
        // Rellenamos los valores a insertar
        rs.updateInt("num_emp", numemp);
        
        if (nomb!= null)
            rs.updateString("nombre", nomb);
        rs.updateString("tarea", tarea);
        if (jefe.matches("[0-9]+") && !jefe.equals("0"))        //Si tiene digitos y no es el CERO 
            rs.updateInt("jefe", Integer.parseInt(jefe));       //parseamos y actualizamos su valor
        if (fecha.matches("^\\d{4}-\\d{2}-\\d{2}"))   {        //Esta condicion es un poco "fula", se puede mejorar.... pero para salir del paso vale...
                
                Date fecha_;
                fecha_=Date.valueOf(fecha);
                System.out.println("matches funciona. Date:"+fecha_+".");
                rs.updateDate("fecha_alta", fecha_);} 
        else
            JOptionPane.showMessageDialog(null, "Fecha no actualizada porque no se introdujo correctamente.");
        
        //Son dos IF anidados porque al hacer un equals a un null, da error.... Por eso primero nos aseguramos de que no es NULL, y despues aplicamos al siguiente condificon/if.
        System.out.println("salario antes del if. Dentro de aceptar. Salario:"+salario+".");
        if(salario!=null)                                                       //Si es null fuera, si no 
            if(!".".equals(salario)) {                                           //si no es un punto(del decimanl) 
                salariof = (Float) Float.parseFloat(salario);
                System.out.println("hace update de salario!! Salario:" + salario +".\tSalariof:"+salariof+".");
                rs.updateFloat("salario",salariof ); }          //parseamos y actualizamos su valor
            else
                JOptionPane.showMessageDialog(null, "Salario no actualizado por ser NULL. \nIntroduzca el salario y para asegurar que el sistema lo acepta, haga click en otra casilla.");

        else
            JOptionPane.showMessageDialog(null, "Salario no actualizado por ser NULL. \nIntroduzca el salario y para asegurar que el sistema lo acepta, haga click en otra casilla.");
    
        if(departamento.matches("[0-9]+"))      //Si el departamento tiene algun digito
                rs.updateInt("num_dpto", Integer.parseInt(departamento));   //entonces parseo
        
        
        if (nuevoRegistro) {
            rs.insertRow(); // Insertamos el registro
            rs.last(); // Nos vamos al final de todos los registros
            System.out.println("Nuevo registro "+nombre+" introducido");
        }
        else{
            rs.updateRow(); // Si la variable nuevoRegistro es false actualizamos el registro
            System.out.println("Registro de "+nombre+" actualizado");
        }
    }
      // Método para aceptar1 la operación a realizar (Insertar o editar)
    
    
    public void aceptar(int num, String nomb, String localidad) throws java.sql.SQLException{
        // Rellenamos los valores a insertar
        rs.updateInt("num_dpto", num);
        rs.updateString("nombre_dpto", nomb);
        rs.updateString("localidad", localidad);

        if (nuevoRegistro) {
            rs.insertRow(); // Insertamos el registro
            rs.last(); // Nos vamos al final de todos los registros
            System.out.println("Nuevo registro "+nombre+" introducido");
        }
        else{
            rs.updateRow(); // Si la variable nuevoRegistro2 es false actualizamos el registro
            System.out.println("Registro de "+nombre+" actualizado");}
    }    
    
    // Método para borrar el registro seleccionado
    public void borrar() throws java.sql.SQLException{
        rs.deleteRow(); // Borramos el registro
        rs.moveToCurrentRow();
        if(esultimo())              //Para que no se salga del rango
            rs.previous();       //De no ser asi, al borrar el ultimo registro, se "sale del rango" y no se activan los botones de navegacion.
        if(esprimero())
            rs.next();
        rs.next(); //Nos vamos uno atras porque me da la gana
        System.out.println("Registro de "+nombre+" eliminado");

    }    
    
    // Método para cancelar la acción nos vale para los dos RS
    public void cancela() throws java.sql.SQLException{
        rs.cancelRowUpdates(); // Cancelamos la operación
        System.out.println("Operacion de registro cancelada");
    }
    
    public boolean esultimo() throws SQLException{
        int pos;
        int size;
        pos=rs.getRow();
        rs.last();
        size=rs.getRow();
        rs.absolute(pos);
       return (size==pos);
        
    }
    
    public boolean esprimero() throws SQLException{
        int pos;
        pos=rs.getRow();
        rs.first();
        rs.absolute(pos);
        return (1==pos);
        
    }
    
     /**
     * @return the nombre
     */

}
