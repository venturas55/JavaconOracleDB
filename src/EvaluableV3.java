
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.InputMismatchException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.text.MaskFormatter;

/*      A MEJORAR
 *      + Capturar error  en la busqueda de introducir salario
 *      + Colocar el LOG en el lugar correcto, que lo registre cuando sea una operacion exitosa
 *      + Instalar las dependencias automaticamente (Desde el menu).
 *      + Gestionar correctamente las UNIQUE KEY, PRIMARY KEY!
 *      + Controlar los input missmatch en los edit text.
 *      + El string de tareas hacerlo automatico mediante una consulta.
 *      + Las tareas han de introducirse en mayusculas para que se reconozcan!!!
 *      + Pasarlo todo a mayusculas
 *      + Eliminacion/modificacion de empleados o departamentos, gestionar la eleccion correcta. 
 *
 *      - introducir salario minimo y maximo en un solo joptionpane y vigilar su coherencia min < max
 *      - Al insertar algo, siempre se inserta en penultima posición.
*/

/**
 *
 * @author ventu
 */
public class EvaluableV3 extends javax.swing.JFrame {
    
    String[] buttonsSiNo = {"Si", "No"};
    String ruta=System.getProperty("user.dir");
    String log="\\log.txt";
    String help="\\help.pdf";
    DateFormat formatof = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat formatofh = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    String []tareas;
    public static ConectaBD empleados;
    public static ConectaBD departamentos;
    public static ConectaBD auxiliar;       //Conexion a usar para usar y desconectar.
    String usuario="plsql";
    String contrasena="plsql";
    String puerto="1521";
    String triggerBBDD="triggerAdrian";  //
    String funcionBBDD="funcionAdrian";
    //public static String current; //variable que nos indica el elemento que se muestra en curso (empleado o departamento)
    
    /**
     * Creates new form NewJFrame
     */
    public EvaluableV3() {
        
        startLog();
        
        java.util.Date now = new java.util.Date();
	System.out.println("FECHA SIMPLE: " + formatofh.format(now)); 
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
       
        empleados = new ConectaBD("empleado");
        departamentos = new ConectaBD("departamento");
        auxiliar = new ConectaBD("auxiliar");
        
        //this.setUndecorated(true);
        initComponents();   //Inicia todos los componentes.
        Fillcombos();       //Rellenamos los combos
        iniciarTareas();    //y guardamos todas las tareas.
        
        JOptionPane.showMessageDialog(null, "Bienvenido a 'Evaluable V2.2', software para operar sobre una BBDD en Oracle."
                + "\n\nDicho software tiene una opción para instalar el trigger y la funcion en PLSQL para Oracle."
                + "\nEsto hace que no sea necesario exportar la base de datos del alumno, puesto que ya está diseñado"
                + "\npara funcionar en cualquier TABLESPACE donde previamente deben estar creadas las tablas."
                + "\n\nSe conecta con una BBDD local, donde se le puede configurar los credenciales de loggin y el puerto."
                + "\nEl usuario por defecto es plsql, (al igual que la contraseña), y el puerto por defecto es el 1521."
                + "\nSi desea cambiar dicha configuración vaya a 'Herramientas => Configurar BBDD'."
                + "\n\nPulse aceptar para empezar a evaluar el programa.", "Bienvenido", JOptionPane.DEFAULT_OPTION, null);
      
        
        try 
        {
            empleados.conecta(usuario,contrasena,puerto); // Conectamos con la base de datos
            departamentos.conecta(usuario,contrasena,puerto); // Conectamos con la base de datos
            empleados.crearSentencias(); // Creamos las sentaencias para empleado    
            departamentos.crearSentencias(); // Creamos las sentaencias para dpto  
 
            //Ejecutamos la SELECT sobre la tabla clientes
            empleados.ejecutaSQLq("select empleado.* from empleado");
            departamentos.ejecutaSQLq("select dpto.* from dpto");
            
            // Nos posicionamos en el primer registro
            empleados.irAlPrimero();
            departamentos.irAlPrimero();
            
            //La botonera de confirmacion estara desactivada de inicio, pues no hay comando que aceptar/cancelar
            botoneraConfirmacion(false,'e');
            botoneraConfirmacion(false,'d');
            //Imprimimos por pantalla los empleados y los departamentos en sus respectivos tabs.
            printEmpleados(empleados.rs); 
            printDepartamentos(departamentos.rs);
        }
            catch (SQLException ex) 
        {
            toastMessage("Aviso SQL", "Error al conectar con la base de datos");
            ex.printStackTrace();
        }
    }
    
    private void printEmpleados(ResultSet rs) throws SQLException{
        tituloE.setText("LISTADO DE EMPLEADOS");
        empleados.esprimero();
        empleados.esultimo();
        enabledFields(false,'e');            //Deshabilitar los campos de la pestaña Empleados.
        botoneraConfirmacion(false,'e');    // Dehabilitamos los botones Aceptar/Cancelar
        botoneraNavegacion(true,'e');       // Habilitamos la botonera de navegación   
        // Mostramos los datos
        if (empleados.rs.getRow() != 0) 
        {
            numE.setText(""+rs.getInt("num_emp")); 
            nombreE.setText(rs.getString("nombre")); 
            tarea.setText(rs.getString("tarea"));
            jefe.setText(""+rs.getInt("jefe"));
            if (rs.getDate("FECHA_ALTA")!=null)   //Si se obtiene una fecha
                fecha.setText(formatof.format(rs.getDate("FECHA_ALTA")));   //se escribe
            else            //si no...
                fecha.setText("");      //se deja en blanco
            
            if( !String.valueOf(rs.getFloat("SALARIO")).equals("0.0")) //Si se obtiene un salario
                salario.setText(""+rs.getFloat("SALARIO"));              //se escribe
            else                                //si no...
                salario.setText("");        //se deja en blanco
            if(rs.getString("num_dpto")!=null)      //Si se obtiene un departameto
                departamento.setText(""+rs.getString("num_dpto"));   //se escribe
            else{                                    //si no...
                departamento.setText("");       //se deja en blanco
            }
            
            // Habilitamos o desabilitamos los botones de búsqueda
            primeroE.setEnabled(!rs.isFirst());
            anteriorE.setEnabled(!rs.isFirst());
            ultimoE.setEnabled(!rs.isLast());
            siguienteE.setEnabled(!rs.isLast());
        }
        else
        {
            // Borramos campos y deshabilitamos los botones de búsqueda y actualizar y borrar de Empleados ('e')
            cleanFields('e');
            botoneraNavegacion(false,'e');      //deshabilitamos la navegacion
            insertarE.setVisible(true);         //Pero permitimos insertar nuevo empleado
        }
    }    

    private void printDepartamentos(ResultSet rs) throws SQLException{
        tituloD.setText("LISTADO DE DEPARTAMENTOS");
        enabledFields(false,'d');            //Deshabilitar los campos de la pestaña Departamentos.
        botoneraConfirmacion(false,'d');    // Dehabilitamos los botones Aceptar/Cancelar
        botoneraNavegacion(true,'d');       // Habilitamos la botonera de navegación        
        
        // Mostramos los datos
        if (rs.getRow() != 0) 
        {
            numD.setText(""+rs.getInt("num_dpto")); 
            nombreD.setText(rs.getString("nombre_dpto")); 
            localidad.setText(rs.getString("localidad")); 
                       
            // Habilitamos o desabilitamos los botones de búsqueda
            primeroD.setEnabled(!rs.isFirst());
            anteriorD.setEnabled(!rs.isFirst());
            ultimoD.setEnabled(!rs.isLast());
            siguienteD.setEnabled(!rs.isLast());
        }
        else
        {
       // Borramos campos y deshabilitamos los botones de búsqueda y actualizar y borrar
            cleanFields('d');
            botoneraNavegacion(false,'d');      //deshabilitamos la navegacion
            insertarD.setVisible(true);         //Pero permitimos insertar nuevo departamento
        }
    }

    //Condiciones cuando se activan los campos para insertar valores!!!
    private void enabledFields(boolean estado,char c){
        
        if(c=='e'){
            numE.requestFocus();
            numE.setEnabled(estado);
            nombreE.setEnabled(estado);
            tarea.setEnabled(estado);
                                        //Para insertar valores hay que...
            jefe.setVisible(!estado);   //ocultar el campo de jefe
            jefeCB.setVisible(estado);  //y mostrar el ComboBox para la eleccion
            jefeCB.setSelectedItem(jefe.getText()); //Pasandole el valor que tenia de la BD => textfield => comboBox
            jefe.setEnabled(estado);        //Este campo al estar oculto en principio daría igual
            
            fecha.setEnabled(estado);
            salario.setEnabled(estado);
            
            //Mismo razonamiento que para jefe y jefeCB
            departamento.setVisible(!estado);
            departamentoCB.setVisible(estado);
            departamentoCB.setSelectedItem(departamento.getText());
            departamento.setEnabled(estado);
            
            
            }
        if(c=='d'){
            numD.requestFocus();
            numD.setEnabled(estado);
            nombreD.setEnabled(estado); 
            localidad.setEnabled(estado);
        }
    }
    
    //Metodo para limpiar todos los campos en caso de insertar una nueva fila 
    private void cleanFields(char c){
        if(c=='e'){
            numE.setText("");
            nombreE.setText("");
            tarea.setText("");
            jefe.setText("");
            fecha.setText("");
            salario.setText("");
            departamento.setText("");}
        if(c=='d'){
            numD.setText("");
            nombreD.setText("");
            localidad.setText(""); 
        }
    }   
    
    // Método para activar o desactivar los botones de Aceptar y Cancelar
    private void botoneraConfirmacion(boolean estado,char c) {
        if(c=='e'){
            aceptarE.setEnabled(estado);
            cancelarE.setEnabled(estado);}
        if(c=='d'){
            aceptarD.setEnabled(estado);
            cancelarD.setEnabled(estado); 
        }
    }    
    
    // Método para activar o deactivar los botones de navegación. Actualizar y borrar incluido.
    private void botoneraNavegacion(boolean estado,char c) {
        // Activamos o desactivamos los botones de navegación
        if(c=='e'){
            primeroE.setEnabled(estado);
            anteriorE.setEnabled(estado);
            siguienteE.setEnabled(estado);
            ultimoE.setEnabled(estado);
            actualizarE.setEnabled(estado);
            borrarE.setEnabled(estado);
            insertarE.setEnabled(estado);}
        if(c=='d'){
            primeroD.setEnabled(estado);
            anteriorD.setEnabled(estado);
            siguienteD.setEnabled(estado);
            ultimoD.setEnabled(estado);
            actualizarD.setEnabled(estado);
            borrarD.setEnabled(estado);
            insertarD.setEnabled(estado);  
        }
    }    
      

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane = new javax.swing.JTabbedPane();
        Empleados = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        MaskFormatter maskNOME = null;
        try
        {
            maskNOME = new MaskFormatter("**********");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        nombreE = new javax.swing.JTextField();
        MaskFormatter maskTAR = null;
        try
        {
            maskTAR = new MaskFormatter("***************");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        tarea = new javax.swing.JTextField();
        borrarE = new javax.swing.JButton();
        aceptarE = new javax.swing.JButton();
        cancelarE = new javax.swing.JButton();
        insertarE = new javax.swing.JButton();
        tituloE = new javax.swing.JLabel();
        primeroE = new javax.swing.JButton();
        refrescarE = new javax.swing.JButton();
        actualizarE = new javax.swing.JButton();
        anteriorE = new javax.swing.JButton();
        siguienteE = new javax.swing.JButton();
        ultimoE = new javax.swing.JButton();
        filtro = new javax.swing.JLabel();
        MaskFormatter maskFECHA = null;
        try
        {
            maskFECHA = new MaskFormatter("####-##-##");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        fecha = new JFormattedTextField(maskFECHA);
        MaskFormatter maskJEF = null;
        try
        {
            maskJEF = new MaskFormatter("####");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        jefe = new JFormattedTextField(maskJEF);
        MaskFormatter maskEMP = null;
        try
        {
            maskEMP = new MaskFormatter("####");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        numE = new JFormattedTextField(maskEMP);
        MaskFormatter maskDEPP = null;
        try
        {
            maskDEPP = new MaskFormatter("##");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        departamento = new JFormattedTextField(maskDEPP);
        jefeCB = new javax.swing.JComboBox<>();
        departamentoCB = new javax.swing.JComboBox<>();
        MaskFormatter maskSAL = null;
        try
        {
            maskSAL = new MaskFormatter("####.##");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        salario = new JFormattedTextField(maskSAL);
        Departamentos = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        MaskFormatter maskLOC = null;
        try
        {
            maskLOC = new MaskFormatter("*************");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        localidad = new javax.swing.JTextField();
        borrarD = new javax.swing.JButton();
        aceptarD = new javax.swing.JButton();
        cancelarD = new javax.swing.JButton();
        insertarD = new javax.swing.JButton();
        tituloD = new javax.swing.JLabel();
        refrescarD = new javax.swing.JButton();
        actualizarD = new javax.swing.JButton();
        primeroD = new javax.swing.JButton();
        anteriorD = new javax.swing.JButton();
        siguienteD = new javax.swing.JButton();
        ultimoD = new javax.swing.JButton();
        MaskFormatter maskDEP = null;
        try
        {
            maskDEP = new MaskFormatter("##");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        numD = new JFormattedTextField(maskDEP);
        MaskFormatter maskNDEP = null;
        try
        {
            maskNDEP = new MaskFormatter("**************");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        nombreD = new javax.swing.JFormattedTextField();
        Busqueda = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        areatexto = new javax.swing.JTextArea();
        gastoTarea = new javax.swing.JButton();
        buscarEmpleados = new javax.swing.JButton();
        ModificacionesSalario = new javax.swing.JButton();
        verLogBoton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        Archivo = new javax.swing.JMenu();
        verLog = new javax.swing.JMenuItem();
        BorrarLog = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        Salir = new javax.swing.JMenuItem();
        Herramientas = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        configurarBBDD = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        InstalarDependencia = new javax.swing.JMenuItem();
        desinstalarDependencia = new javax.swing.JMenuItem();
        habilitarTrigger = new javax.swing.JMenuItem();
        deshabilitarTrigger = new javax.swing.JMenuItem();
        borrarTablaEmpMod = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane.setBackground(new java.awt.Color(153, 153, 255));

        Empleados.setBackground(new java.awt.Color(153, 153, 255));

        jLabel1.setText("Nombre:");

        jLabel2.setText("Numero:");

        jLabel3.setText("Tarea:");

        jLabel4.setText("Jefe:");

        jLabel5.setText("Fecha:");

        jLabel6.setText("Departamento:");

        jLabel7.setText("Salario:");

        borrarE.setText("Borrar");
        borrarE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarEActionPerformed(evt);
            }
        });

        aceptarE.setText("Aceptar");
        aceptarE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aceptarEActionPerformed(evt);
            }
        });

        cancelarE.setText("Cancelar");
        cancelarE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarEActionPerformed(evt);
            }
        });

        insertarE.setText("Insertar");
        insertarE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertarEActionPerformed(evt);
            }
        });

        tituloE.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        tituloE.setText("LISTADO DE EMPLEADOS");

        primeroE.setText("|<");
        primeroE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primeroEActionPerformed(evt);
            }
        });

        refrescarE.setText("Limpiar filtro");
        refrescarE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refrescarEActionPerformed(evt);
            }
        });

        actualizarE.setText("Modificar");
        actualizarE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarEActionPerformed(evt);
            }
        });

        anteriorE.setText("<<");
        anteriorE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anteriorEActionPerformed(evt);
            }
        });

        siguienteE.setText(">>");
        siguienteE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                siguienteEActionPerformed(evt);
            }
        });

        ultimoE.setText(">>");
        ultimoE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ultimoEActionPerformed(evt);
            }
        });

        jefe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jefeActionPerformed(evt);
            }
        });

        numE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numEActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EmpleadosLayout = new javax.swing.GroupLayout(Empleados);
        Empleados.setLayout(EmpleadosLayout);
        EmpleadosLayout.setHorizontalGroup(
            EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EmpleadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(EmpleadosLayout.createSequentialGroup()
                            .addComponent(refrescarE)
                            .addGap(110, 110, 110))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, EmpleadosLayout.createSequentialGroup()
                            .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, EmpleadosLayout.createSequentialGroup()
                                    .addGap(2, 2, 2)
                                    .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(primeroE)
                                        .addGroup(EmpleadosLayout.createSequentialGroup()
                                            .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel4)
                                                .addComponent(jLabel2)
                                                .addComponent(jLabel7))
                                            .addGap(4, 4, 4)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(EmpleadosLayout.createSequentialGroup()
                                            .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(EmpleadosLayout.createSequentialGroup()
                                                    .addComponent(salario, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(jLabel6))
                                                .addGroup(EmpleadosLayout.createSequentialGroup()
                                                    .addComponent(jefe, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jefeCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(46, 46, 46)
                                                    .addComponent(jLabel3)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(EmpleadosLayout.createSequentialGroup()
                                                    .addComponent(departamento, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(departamentoCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(EmpleadosLayout.createSequentialGroup()
                                                    .addComponent(tarea, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(jLabel5)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(fecha, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(EmpleadosLayout.createSequentialGroup()
                                            .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(numE, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(anteriorE, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGap(20, 20, 20)
                                            .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(siguienteE)
                                                .addComponent(jLabel1))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(ultimoE)
                                                .addComponent(nombreE, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, EmpleadosLayout.createSequentialGroup()
                                    .addComponent(tituloE, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(EmpleadosLayout.createSequentialGroup()
                                            .addComponent(actualizarE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(insertarE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(borrarE))
                                        .addComponent(filtro, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addGroup(EmpleadosLayout.createSequentialGroup()
                        .addComponent(aceptarE)
                        .addGap(50, 50, 50)
                        .addComponent(cancelarE)))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        EmpleadosLayout.setVerticalGroup(
            EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EmpleadosLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(refrescarE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tituloE, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filtro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertarE)
                    .addComponent(borrarE)
                    .addComponent(actualizarE)
                    .addComponent(primeroE)
                    .addComponent(anteriorE)
                    .addComponent(siguienteE)
                    .addComponent(ultimoE))
                .addGap(22, 22, 22)
                .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(nombreE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(tarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jefe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jefeCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(departamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(departamentoCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(salario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(EmpleadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aceptarE)
                    .addComponent(cancelarE))
                .addGap(121, 121, 121))
        );

        jTabbedPane.addTab("Empleados", Empleados);

        Departamentos.setBackground(new java.awt.Color(153, 153, 255));

        jLabel8.setText("Nombre:");

        jLabel9.setText("Numero:");

        jLabel11.setText("Localidad:");

        borrarD.setText("Borrar");
        borrarD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarDActionPerformed(evt);
            }
        });

        aceptarD.setText("Aceptar");
        aceptarD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aceptarDActionPerformed(evt);
            }
        });

        cancelarD.setText("Cancelar");
        cancelarD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarDActionPerformed(evt);
            }
        });

        insertarD.setText("Insertar");
        insertarD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertarDActionPerformed(evt);
            }
        });

        tituloD.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        tituloD.setText("LISTADO DE DEPARTAMENTOS");

        refrescarD.setText("Refrescar");
        refrescarD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refrescarDActionPerformed(evt);
            }
        });

        actualizarD.setText("Modificar");
        actualizarD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarDActionPerformed(evt);
            }
        });

        primeroD.setText("|<");
        primeroD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primeroDActionPerformed(evt);
            }
        });

        anteriorD.setText("<<");
        anteriorD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anteriorDActionPerformed(evt);
            }
        });

        siguienteD.setText(">>");
        siguienteD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                siguienteDActionPerformed(evt);
            }
        });

        ultimoD.setText(">|");
        ultimoD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ultimoDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout DepartamentosLayout = new javax.swing.GroupLayout(Departamentos);
        Departamentos.setLayout(DepartamentosLayout);
        DepartamentosLayout.setHorizontalGroup(
            DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DepartamentosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DepartamentosLayout.createSequentialGroup()
                        .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(DepartamentosLayout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addComponent(anteriorD)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(siguienteD))
                            .addGroup(DepartamentosLayout.createSequentialGroup()
                                .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel9)
                                    .addComponent(primeroD))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(numD, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(DepartamentosLayout.createSequentialGroup()
                                .addComponent(ultimoD)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 185, Short.MAX_VALUE)
                                .addComponent(actualizarD)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(insertarD))
                            .addGroup(DepartamentosLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nombreD, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(22, 22, 22)
                        .addComponent(borrarD)
                        .addGap(34, 34, 34))
                    .addGroup(DepartamentosLayout.createSequentialGroup()
                        .addComponent(tituloD, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refrescarD)
                        .addGap(89, 89, 89))
                    .addGroup(DepartamentosLayout.createSequentialGroup()
                        .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, DepartamentosLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(localidad, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, DepartamentosLayout.createSequentialGroup()
                                .addComponent(aceptarD)
                                .addGap(50, 50, 50)
                                .addComponent(cancelarD)))
                        .addGap(0, 449, Short.MAX_VALUE))))
        );
        DepartamentosLayout.setVerticalGroup(
            DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DepartamentosLayout.createSequentialGroup()
                .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DepartamentosLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(tituloD, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(refrescarD))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(borrarD)
                    .addComponent(insertarD)
                    .addComponent(actualizarD)
                    .addComponent(primeroD)
                    .addComponent(anteriorD)
                    .addComponent(siguienteD)
                    .addComponent(ultimoD))
                .addGap(22, 22, 22)
                .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(numD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nombreD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(localidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(86, 86, 86)
                .addGroup(DepartamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aceptarD)
                    .addComponent(cancelarD))
                .addContainerGap(139, Short.MAX_VALUE))
        );

        jTabbedPane.addTab("Departamentos", Departamentos);

        Busqueda.setBackground(new java.awt.Color(204, 204, 255));

        areatexto.setColumns(20);
        areatexto.setRows(5);
        jScrollPane1.setViewportView(areatexto);

        gastoTarea.setText("Calcular gasto por Tarea");
        gastoTarea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gastoTareaActionPerformed(evt);
            }
        });

        buscarEmpleados.setText("Buscar empleados");
        buscarEmpleados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buscarEmpleadosActionPerformed(evt);
            }
        });

        ModificacionesSalario.setText("Modificaciones Salario");
        ModificacionesSalario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ModificacionesSalarioActionPerformed(evt);
            }
        });

        verLogBoton.setText("Ver Log");
        verLogBoton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verLogBotonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout BusquedaLayout = new javax.swing.GroupLayout(Busqueda);
        Busqueda.setLayout(BusquedaLayout);
        BusquedaLayout.setHorizontalGroup(
            BusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(BusquedaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(BusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buscarEmpleados, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gastoTarea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 283, Short.MAX_VALUE)
                .addGroup(BusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ModificacionesSalario)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BusquedaLayout.createSequentialGroup()
                        .addComponent(verLogBoton)
                        .addGap(22, 22, 22)))
                .addGap(81, 81, 81))
        );
        BusquedaLayout.setVerticalGroup(
            BusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BusquedaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(BusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gastoTarea)
                    .addComponent(ModificacionesSalario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(BusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buscarEmpleados)
                    .addComponent(verLogBoton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))
        );

        jTabbedPane.addTab("Busqueda", Busqueda);

        jMenuBar1.setBackground(new java.awt.Color(153, 153, 255));

        Archivo.setBackground(new java.awt.Color(204, 204, 204));
        Archivo.setText("Archivo");

        verLog.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        verLog.setText("Ver Log");
        verLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verLogActionPerformed(evt);
            }
        });
        Archivo.add(verLog);

        BorrarLog.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        BorrarLog.setText("Borrar Log");
        BorrarLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BorrarLogActionPerformed(evt);
            }
        });
        Archivo.add(BorrarLog);
        Archivo.add(jSeparator2);

        Salir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        Salir.setText("Salir");
        Salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SalirActionPerformed(evt);
            }
        });
        Archivo.add(Salir);

        jMenuBar1.add(Archivo);

        Herramientas.setBackground(new java.awt.Color(36, 12, 5));
        Herramientas.setText("Herramientas");
        Herramientas.add(jSeparator1);

        configurarBBDD.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        configurarBBDD.setText("Configurar BBDD");
        configurarBBDD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurarBBDDActionPerformed(evt);
            }
        });
        Herramientas.add(configurarBBDD);

        jMenu1.setText("Dependencias BBDD");

        InstalarDependencia.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        InstalarDependencia.setText("Instalar dependencias");
        InstalarDependencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstalarDependenciaActionPerformed(evt);
            }
        });
        jMenu1.add(InstalarDependencia);

        desinstalarDependencia.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        desinstalarDependencia.setText("Desinstalar dependencias");
        desinstalarDependencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                desinstalarDependenciaActionPerformed(evt);
            }
        });
        jMenu1.add(desinstalarDependencia);

        habilitarTrigger.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        habilitarTrigger.setText("Activar Trigger");
        habilitarTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                habilitarTriggerActionPerformed(evt);
            }
        });
        jMenu1.add(habilitarTrigger);

        deshabilitarTrigger.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        deshabilitarTrigger.setText("Desactivar Trigger");
        deshabilitarTrigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deshabilitarTriggerActionPerformed(evt);
            }
        });
        jMenu1.add(deshabilitarTrigger);

        Herramientas.add(jMenu1);

        borrarTablaEmpMod.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        borrarTablaEmpMod.setText("Borrar tabla Empleados_Modificados");
        borrarTablaEmpMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarTablaEmpModActionPerformed(evt);
            }
        });
        Herramientas.add(borrarTablaEmpMod);

        jMenuBar1.add(Herramientas);

        jMenu2.setText("Ayuda");

        jMenuItem1.setText("Consultar Ayuda");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelarDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelarDActionPerformed
        try
        {
            departamentos.cancela(); // Cancelamos la operación de insercion dpto
            enabledFields(false,'d'); // Dehabilitamos los campos

            departamentos.nuevoRegistro = false; // Reseteamos la variable nuevoRegistro1
            // NAVEGACION.Deshabilitamos los botones Aceptar/Cancelar y habilitamos la botonera de navegación
            botoneraConfirmacion(false,'d');
            botoneraNavegacion(true,'d');
            toastMessage("Aviso","Operacion cancelada por el usuario.");
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_cancelarDActionPerformed

    private void insertarDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertarDActionPerformed
        this.tituloD.setText("AÑADA DATOS NUEVO DEPARTAMENTO");
        departamentos.nuevoRegistro = true;
        try
        {
            departamentos.nuevo(); // Preparamos para insertar el empleado

            cleanFields('d');   // Limpiamos los campos
            
            // Dehabilitamos la botonera de navegación y habilitamos los campos
            botoneraNavegacion(false,'d');
            enabledFields(true,'d');
            botoneraConfirmacion(true,'d'); 
            java.util.Date now = new java.util.Date();   addLog( formatofh.format(now)+ " Inserción de departamento.");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_insertarDActionPerformed

    private void borrarDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarDActionPerformed
        try {
            int opc = JOptionPane.showOptionDialog(null,"Realmente desea borrar el departamento con ID = [" + numD.getText() + "]?","Confirmation",JOptionPane.WARNING_MESSAGE,0,null,buttonsSiNo,buttonsSiNo[1]);
            if (opc == 0){
                departamentos.borrar(); // Borramos el cliente seleccionado
                printDepartamentos(departamentos.rs); // Mostramos el siguiente registro
                System.out.println("Departamento borrado");
           java.util.Date now = new java.util.Date();addLog( formatofh.format(now)+ " departamento borrado" );

            }
        } catch (Exception e )
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_borrarDActionPerformed

    private void SalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SalirActionPerformed

        try {
            empleados.cerrarConexion();
            departamentos.cerrarConexion();
            auxiliar.cerrarConexion();
            setVisible(false); //you can't see me!
            dispose(); //cierra el jframe
        } catch (SQLException ex) {
            //Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_SalirActionPerformed

    private void verLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verLogActionPerformed
    areatexto.setText("");
        try {
            BufferedReader in  = new BufferedReader (new FileReader(ruta+log));
            String frase;
            frase = in.readLine();
            
            while(frase!=null){
                areatexto.setText(areatexto.getText()+frase+"\n");
                frase = in.readLine();}
            in.close();
            jTabbedPane.setSelectedIndex(2);
            
        }catch (FileNotFoundException ex) {
            toastMessage("Aviso","Archivo log no encontrado!");
            //Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            toastMessage("Aviso","Error al leer el log!");
            //Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_verLogActionPerformed

    private void configurarBBDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurarBBDDActionPerformed
        String input;    
        input=JOptionPane.showInputDialog("Introduzca usuario");
        if(input!=null && !input.equals(""))
            usuario=input;
        
        input=JOptionPane.showInputDialog("Introduzca contraseña");
        if(input!=null && !input.equals(""))
            usuario=input;
        
        input=JOptionPane.showInputDialog("Introduzca el puerto de la BBDD localhost");
        if(input!=null && !input.equals(""))
            puerto=input;  
        
    }//GEN-LAST:event_configurarBBDDActionPerformed

    private void habilitarTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_habilitarTriggerActionPerformed
            habilitarTrigger();
    }//GEN-LAST:event_habilitarTriggerActionPerformed

    private void deshabilitarTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deshabilitarTriggerActionPerformed
        deshabilitarTrigger();
    }//GEN-LAST:event_deshabilitarTriggerActionPerformed
    
    private void InstalarDependenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InstalarDependenciaActionPerformed
        instalarDependencias();
    }//GEN-LAST:event_InstalarDependenciaActionPerformed

    private void desinstalarDependenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_desinstalarDependenciaActionPerformed
        desinstalarDependencias();
    }//GEN-LAST:event_desinstalarDependenciaActionPerformed

    private void refrescarDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refrescarDActionPerformed
        tituloD.setText("LISTADO DE DEPARTAMENTOS");  
        
        java.util.Date now = new java.util.Date();addLog( formatofh.format(now)+ " Consulta departamentos" ); 
        
        try{ 
            String sql = "select dpto.* from dpto";
            departamentos.ejecutaSQLq(sql);
            
            departamentos.irAlPrimero();
            printDepartamentos(departamentos.rs);
        }
        catch (SQLException ex) 
        {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_refrescarDActionPerformed

    private void actualizarDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizarDActionPerformed
        try {
            tituloD.setText("Modifica el departamento " + this.numD.getText());
            departamentos.nuevoRegistro = false;
            botoneraNavegacion(false,'d'); // Dehabilitamos la botonera de navegación de departamento
            enabledFields(true,'d');         // Habilitamos los campos de departamento
            botoneraConfirmacion(true,'d'); // Habilitamos los botones Aceptar/Cancelar de departamento
            java.util.Date now = new java.util.Date();addLog( formatofh.format(now)+ " departamento modificado" );

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_actualizarDActionPerformed

    private void primeroDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primeroDActionPerformed
        try {
            departamentos.irAlPrimero();
            printDepartamentos(departamentos.rs);
            System.out.println("Al inicio de Departamento");
        } catch (SQLException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_primeroDActionPerformed

    private void anteriorDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anteriorDActionPerformed
        try {
            departamentos.irAlAnterior();
            printDepartamentos(departamentos.rs);
            System.out.println("Retrocede Departamento");
        } catch (SQLException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_anteriorDActionPerformed

    private void siguienteDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_siguienteDActionPerformed
        try {
            departamentos.irAlSiguiente();
            printDepartamentos(departamentos.rs);
            System.out.println("Avanza Departamento");
        } catch (SQLException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_siguienteDActionPerformed

    private void ultimoDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ultimoDActionPerformed
        try {
            departamentos.irAlFinal();
            printDepartamentos(departamentos.rs);
            System.out.println("Avanza a final Departamentos");
        } catch (SQLException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_ultimoDActionPerformed

    private void gastoTareaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gastoTareaActionPerformed
        iniciarTareas();
        try {
            float suma;
            String opc = (String) JOptionPane.showInputDialog(this,"Seleccione tarea para calcular gasto total","Gasto por tarea",JOptionPane.QUESTION_MESSAGE ,null,tareas,tareas[0]);
            if (opc != null){
                opc=opc.toUpperCase();
                System.out.println(opc);
                CallableStatement cstmt = empleados.conn.prepareCall("{? = call "+funcionBBDD+"(?)}");
                cstmt.registerOutParameter(1,oracle.jdbc.OracleTypes.NUMBER);
                cstmt.setString(2, opc);
                cstmt.execute();
                //se recupera el resultado de la funcion pl/sql
                suma = cstmt.getFloat(1);
                System.out.println(suma);
                JOptionPane.showMessageDialog(this, "El importe que gasta la empresa en "+opc+" es de "+suma+" € ", "Consulta",JOptionPane.WARNING_MESSAGE);
                java.util.Date now = new java.util.Date(); addLog( formatofh.format(now)+ " Consulta gasto por tarea" );
            }
            else
                toastMessage("Aviso SQL","Operacion cancelada.");
                
        } catch (SQLException ex) {
            toastMessage("Aviso SQL","Debe instalar las dependencias en el menu Herramientas.");
            //Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_gastoTareaActionPerformed

    private void buscarEmpleadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buscarEmpleadosActionPerformed
    String[] options = new String[] {"Tarea", "Salario", "Cancel"};
    refrescarE.setText("Limpiar filtros");  //Indicamos el boton para limpiar los filtros.
    int response = JOptionPane.showOptionDialog(null, "Seleccion el criterio de busqueda de empleado", "Buscar empleado",JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        System.out.println("response " + response);
    switch (response){
        case 0: try{
                String opc = (String) JOptionPane.showInputDialog(this,"Seleccion la tarea de busqueda de empleado","Buscar empleado",JOptionPane.QUESTION_MESSAGE ,null,tareas,tareas[0]); 
                if(!(opc == null)){    
                    System.out.println("Opcion:" + opc);

                    String sql="select empleado.* from empleado where tarea='"+opc.toUpperCase()+"'";
                    empleados.ejecutaSQLq(sql);
                    filtro.setText("FILTRO activado:\nTarea "+opc); //filtro activado
                    empleados.irAlPrimero();
                    printEmpleados(empleados.rs);
                    java.util.Date now = new java.util.Date(); addLog( formatofh.format(now)+ " Buscar empleado por tarea" );
                    jTabbedPane.setSelectedIndex(0);    
                    break;
                }
                else
                    toastMessage("Aviso","Operacion cancelada.");
                
                }
                catch (SQLException ex) 
                {
                    ex.printStackTrace();
                }
        
        case 1: try{
                String num1=leerFloat("Introduzca salario minimo");
                if (num1==(null))  {    //así no hay que esperar a introducir el segundo valor en caso de cancelar el primero.
                    toastMessage("Aviso","Operacion cancelada."); break;}
                String num2=leerFloat("Introduzca salario maximo");
                System.out.println("num1 " + num1);
                System.out.println("num2 "+ num2);
                if ((num1 != null) && (num2 != null)) { 
                    String sql="select empleado.* from empleado where salario between "+num1+" and "+num2;
                    empleados.ejecutaSQLq(sql);
                    empleados.irAlPrimero();
                    filtro.setText(" FILTRO activado:\nSalario entre"+num1+" y "+num2); //filtro activado
                    printEmpleados(empleados.rs);
                    java.util.Date now = new java.util.Date(); addLog( formatofh.format(now)+ " Buscar empleado por salario" );
                    jTabbedPane.setSelectedIndex(0);
                    break;
                }
        
                }
                catch (SQLException ex) 
                {
                    toastMessage("Aviso","Por favor, asegurese de introducir el rango de forma correcta");
                    ex.printStackTrace();
                } 
        default: toastMessage("Aviso","Operacion cancelada.");
    }
            
    }//GEN-LAST:event_buscarEmpleadosActionPerformed

    private void aceptarDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aceptarDActionPerformed

        botoneraNavegacion(true,'d');
        botoneraConfirmacion(false,'d');
        // Aceptamos un cliente nuevo1 o modificación
        try
        {
            departamentos.aceptar(Integer.parseInt(numD.getText()), nombreD.getText(),localidad.getText()); // Pasamos los tareas a insertar o modificar
            departamentos.nuevoRegistro = false;    // Resetamos la variable nuevoRegistro2
            //refrescarD.doClick();                   //Refrescamos consulta
            printDepartamentos(departamentos.rs);   // Mostramos los datos

        }catch (SQLIntegrityConstraintViolationException ex){
            toastMessage("Error SQL","Ya existe un departamento con ese codigo.");  
        }catch( NumberFormatException ex){
            toastMessage("Error SQL","Número de departamento obligatorio.\nRellenelo mediante numeros enteros.");
        }
        catch (SQLException ex){
            ex.printStackTrace();
            toastMessage("Aviso SQL","Error en la insercion/actualizacion a la BBDD");
        }      
        catch (Exception e)
        {e.printStackTrace();
            e.printStackTrace();
        }
        
    }//GEN-LAST:event_aceptarDActionPerformed

    private void ModificacionesSalarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ModificacionesSalarioActionPerformed
        areatexto.setText("NUM_EMP \tNOMBRE\t SALARIO VIEJO     SALARIO NUEVO      MODIFICADO POR    FECHA MODIFICACION");
        areatexto.setText(areatexto.getText()+"\n-------------------------------------------------------------------------------------------------------------------------------------------------------------");
        String sqls= "select empleados_modificados.* from empleados_modificados";
        try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            auxiliar.ejecutaSQLq(sqls);
            auxiliar.rs.last();
            if (auxiliar.rs.getRow() != 0) {
                 auxiliar.rs.first();  
                while(!auxiliar.rs.isLast()) {
                    areatexto.setText(areatexto.getText()+"\n   "+auxiliar.rs.getInt("num_emp")+"\t "+auxiliar.rs.getString("nombre")+"\t      "+auxiliar.rs.getFloat("salario_viejo")+"\t          "+auxiliar.rs.getFloat("salario_nuevo")+"\t\t  "+auxiliar.rs.getString("modificado_por")+"\t         "+auxiliar.rs.getDate("fecha_modificacion")); 
                    auxiliar.rs.next();
                }
                    areatexto.setText(areatexto.getText()+"\n   "+auxiliar.rs.getInt("num_emp")+"\t "+auxiliar.rs.getString("nombre")+"\t      "+auxiliar.rs.getFloat("salario_viejo")+"\t          "+auxiliar.rs.getFloat("salario_nuevo")+"\t\t  "+auxiliar.rs.getString("modificado_por")+"\t         "+auxiliar.rs.getDate("fecha_modificacion")); 
            }else
                JOptionPane.showMessageDialog(this, "No hay modificaciones en salario registradas", "Warning",JOptionPane.WARNING_MESSAGE);
            auxiliar.cerrarConexion();
        } catch (SQLException ex) {
            toastMessage("Error SQL","Hubo algun problema con la BBDD.\nVerifique su configuracion.");
            //Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }

    }//GEN-LAST:event_ModificacionesSalarioActionPerformed

    private void numEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_numEActionPerformed

    private void jefeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jefeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jefeActionPerformed

    // Mostramos el último registro
    private void ultimoEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ultimoEActionPerformed
        try
        {
            empleados.irAlFinal();
            printEmpleados(empleados.rs);
            System.out.println("A final de empleados");
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_ultimoEActionPerformed

    // Mostramos el siguiente registro
    private void siguienteEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_siguienteEActionPerformed
        try
        {
            empleados.irAlSiguiente();
            printEmpleados(empleados.rs);
            System.out.println("Avanza empleado");
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_siguienteEActionPerformed

    private void anteriorEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anteriorEActionPerformed
        try {
            empleados.irAlAnterior();
            printEmpleados(empleados.rs);
            System.out.println("Retrocede empleado");
        } catch (SQLException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_anteriorEActionPerformed

    private void actualizarEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizarEActionPerformed
        try {
            this.tituloE.setText("Modifica el empleado " + this.numE.getText());
            empleados.nuevoRegistro = false; // Indicamos que no será nuevo registro para luego variar el comportamiento al vlickar sobre aceptar
            botoneraNavegacion(false,'e'); // Dehabilitamos la botonera de navegación
            enabledFields(true,'e');         // Ponemos el cursor en el campo de numero empleado y habilitamos los campos
            botoneraConfirmacion(true,'e'); // Habilitamos los botones Aceptar/Cancelar
            java.util.Date now = new java.util.Date();addLog( formatofh.format(now)+ " empleado modificado" );

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_actualizarEActionPerformed

    private void refrescarEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refrescarEActionPerformed
        filtro.setText((""));
        refrescarE.setText("Refrescar");

        java.util.Date now = new java.util.Date();addLog( formatofh.format(now)+ " Consulta empleados" );

        try{
            String sql = "select empleado.* from empleado";
            empleados.ejecutaSQLq(sql);

            empleados.irAlPrimero(); // Nos posicionamos en el primer registro
            printEmpleados(empleados.rs);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_refrescarEActionPerformed

    private void primeroEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primeroEActionPerformed
        try {
            empleados.irAlPrimero();
            printEmpleados(empleados.rs);
            System.out.println("A inicio de empleados");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_primeroEActionPerformed

    private void insertarEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertarEActionPerformed
        this.tituloE.setText("AÑADA DATOS NUEVO EMPLEADO");
        empleados.nuevoRegistro = true;
        try
        {
            empleados.nuevo(); // Preparamos para insertar el empleado
            cleanFields('e');   // Limpiamos los campos

            // Dehabilitamos la botonera de navegación y habilitamos los campos
            botoneraNavegacion(false,'e');
            enabledFields(true,'e');
            botoneraConfirmacion(true,'e');
            java.util.Date now = new java.util.Date();   addLog( formatofh.format(now)+ " Inserción de empleado.");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_insertarEActionPerformed

    private void cancelarEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelarEActionPerformed
        try
        {
            empleados.cancela(); // Cancelamos la operación de insercion de empleado
            enabledFields(false,'e'); // Dehabilitamos los campos
            empleados.nuevoRegistro = false; // Reseteamos la variable nuevoRegistro
            // NAVEGACION.Deshabilitamos los botones Aceptar/Cancelar y habilitamos la botonera de navegación
            botoneraConfirmacion(false,'e');
            botoneraNavegacion(true,'e');
            toastMessage("Aviso","Operacion cancelada por el usuario.");
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_cancelarEActionPerformed

    private void aceptarEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aceptarEActionPerformed
        //Pasamos todos los "campos formateados" a variables para tratarlos ya que los
        //"campos formateados" estan forzados a tener una longitud fija por el JFormatterMask
                //Ademas se pasa todo a uppercase!
        String numE_ = numE.getText().toUpperCase();                  
        String nombreE_= nombreE.getText().toUpperCase();           
        String jefe_= (String) jefeCB.getSelectedItem();  jefe_ = jefe_;
        String tarea_= tarea.getText().toUpperCase();                
        String fecha_ = fecha.getText();              
        String departamento_= (String) departamentoCB.getSelectedItem(); departamento_=departamento_.toUpperCase();
        String salario_= salario.getText();            

        //Volvemos a habilitar la botonera de navegacion y deshabilitar la de confirmacion
        botoneraNavegacion(true,'e');
        botoneraConfirmacion(false,'e');

        // Aceptamos un cliente nuevo1 o modificación
        try
        {
            if(empleados.nuevoRegistro && existeEmpleado(Integer.parseInt(numE_))==1)
                toastMessage("Error SQL PRIMARY KEY","El numero de empleado ya existe.\nAsegurese de que el número de empleado insertado no exista.");
            else
            {
                empleados.aceptar(Integer.parseInt(numE_), nombreE_,tarea_,jefe_, fecha_, salario_,departamento_ ); // Pasamos los tareas a insertar o modificar
                empleados.nuevoRegistro = false; // Resetamos la variable nuevoRegistro
            }
                
                //refrescarE.doClick(); // Refrescar la consulta de empleados.
            printEmpleados(empleados.rs); // Mostramos los datos

            }
            catch (SQLIntegrityConstraintViolationException ex){
                ex.printStackTrace();
                toastMessage("Error SQL","El numero de departamento suministrado no existe.\nIntentelo de nuevo y asegurese de que exista el departamento asignado.");
            }
            catch (SQLException ex){
                ex.printStackTrace();
                toastMessage("Aviso SQL","Error en la insercion/actualizacion a la BBDD");
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
    }//GEN-LAST:event_aceptarEActionPerformed

      // Borramos un empleado
    private void borrarEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarEActionPerformed
        try {
            int opc = JOptionPane.showOptionDialog(null,"Realmente desea borrar el empleado con ID = [" + numE.getText() + "]?","Confirmation",JOptionPane.WARNING_MESSAGE,0,null,buttonsSiNo,buttonsSiNo[1]);
            if (opc == 0){
                empleados.borrar(); // Borramos el cliente seleccionado
                printEmpleados(empleados.rs); // Mostramos el siguiente registro
            java.util.Date now = new java.util.Date();addLog( formatofh.format(now)+ " empleado borrado" );

            }
        } catch (Exception e )
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_borrarEActionPerformed

    private void verLogBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verLogBotonActionPerformed
    areatexto.setText("");
        try {
            BufferedReader in  = new BufferedReader (new FileReader(ruta+log)); System.out.println(ruta);
            String frase;
            frase = in.readLine();

            while(frase!=null){
                areatexto.setText(areatexto.getText()+frase+"\n");
                frase = in.readLine();}
            in.close();
            jTabbedPane.setSelectedIndex(2);
            
        }catch (FileNotFoundException ex) {
            toastMessage("Aviso","Archivo log no encontrado!");
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            toastMessage("Aviso","Error al leer el log!");
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_verLogBotonActionPerformed

    private void BorrarLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BorrarLogActionPerformed

        int opc= JOptionPane.showConfirmDialog (null, "Está seguro que desea eliminar todos los registros del LOG??????\n\n   No hay marcha atras","AVISO", JOptionPane.YES_NO_OPTION);
        System.out.println("Opcion:"+opc);
        if(opc==0){
        File fichero = new File(ruta);
        
        if (!fichero.exists()) {
            toastMessage("Aviso","El Log no se ha podido borrar porque no existía.");
        System.out.println("El archivo data no existe.");
    } else {
        fichero.delete();
        toastMessage("Aviso","El Log ha sido eliminado.");
        System.out.println("El archivo data fue eliminado.");
    }}
        else{}
    }//GEN-LAST:event_BorrarLogActionPerformed

    private void borrarTablaEmpModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarTablaEmpModActionPerformed
int opc= JOptionPane.showConfirmDialog (null, "Está seguro que desea eliminar todos los registros la tabla\n\n    EMPLEADOS_MODIFICADOS???\n\n    No hay marcha atras","ATENCION", JOptionPane.YES_NO_OPTION);
        System.out.println("Opcion:"+opc);
        if(opc==0){
            
            try {
                auxiliar.conecta(usuario,contrasena,puerto);
                auxiliar.crearSentencias();
                auxiliar.ejecutaSQLq("delete from empleados_modificados");
                auxiliar.cerrarConexion();
                toastMessage("Aviso SQL","Tabla EMPLEADOS_MODIFICADOS eliminada!!");
            } catch (SQLException ex) {
                toastMessage("Aviso SQL","No se pudo eliminar la tabla EMPLEADOS_MODIFICADOS.");}
        
        }
        else{toastMessage("Aviso","Operación cancelada por el usuario.");}
    }//GEN-LAST:event_borrarTablaEmpModActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
            abrirarchivo(ruta+help);
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EvaluableV3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EvaluableV3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EvaluableV3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EvaluableV3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                new EvaluableV3().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu Archivo;
    private javax.swing.JMenuItem BorrarLog;
    private javax.swing.JPanel Busqueda;
    private javax.swing.JPanel Departamentos;
    private javax.swing.JPanel Empleados;
    private javax.swing.JMenu Herramientas;
    private javax.swing.JMenuItem InstalarDependencia;
    private javax.swing.JButton ModificacionesSalario;
    private javax.swing.JMenuItem Salir;
    private javax.swing.JButton aceptarD;
    private javax.swing.JButton aceptarE;
    private javax.swing.JButton actualizarD;
    private javax.swing.JButton actualizarE;
    private javax.swing.JButton anteriorD;
    private javax.swing.JButton anteriorE;
    private javax.swing.JTextArea areatexto;
    private javax.swing.JButton borrarD;
    private javax.swing.JButton borrarE;
    private javax.swing.JMenuItem borrarTablaEmpMod;
    private javax.swing.JButton buscarEmpleados;
    private javax.swing.JButton cancelarD;
    private javax.swing.JButton cancelarE;
    private javax.swing.JMenuItem configurarBBDD;
    private javax.swing.JFormattedTextField departamento;
    private javax.swing.JComboBox<String> departamentoCB;
    private javax.swing.JMenuItem deshabilitarTrigger;
    private javax.swing.JMenuItem desinstalarDependencia;
    private javax.swing.JFormattedTextField fecha;
    private javax.swing.JLabel filtro;
    private javax.swing.JButton gastoTarea;
    private javax.swing.JMenuItem habilitarTrigger;
    private javax.swing.JButton insertarD;
    private javax.swing.JButton insertarE;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JFormattedTextField jefe;
    private javax.swing.JComboBox<String> jefeCB;
    private javax.swing.JTextField localidad;
    private javax.swing.JFormattedTextField nombreD;
    private javax.swing.JTextField nombreE;
    private javax.swing.JFormattedTextField numD;
    private javax.swing.JFormattedTextField numE;
    private javax.swing.JButton primeroD;
    private javax.swing.JButton primeroE;
    private javax.swing.JButton refrescarD;
    private javax.swing.JButton refrescarE;
    private javax.swing.JFormattedTextField salario;
    private javax.swing.JButton siguienteD;
    private javax.swing.JButton siguienteE;
    private javax.swing.JTextField tarea;
    private javax.swing.JLabel tituloD;
    private javax.swing.JLabel tituloE;
    private javax.swing.JButton ultimoD;
    private javax.swing.JButton ultimoE;
    private javax.swing.JMenuItem verLog;
    private javax.swing.JButton verLogBoton;
    // End of variables declaration//GEN-END:variables

     //funcion para no tener que escribir joptionpane blablabla todo el rato
    private void toastMessage(String aviso, String mensaje/*, String tipo_*/){
        
        /*switch(tipo_){
            case "error": tipo="JOptionPane.ERROR_MESSAGE"; break;
            case "informacion": tipo="JOptionPane.INFORMATION_MESSAGE"; break;
            case "aviso": tipo="JOptionPane.WARNING_MESSAGE"; break;
            case "pregunta": tipo="JOptionPane.QUESTION_MESSAGE"; break;
            default: tipo="JOptionPane.PLAIN_MESSAGE"; break;
    }*/
        
        //System.out.println(tipo);
        JOptionPane.showMessageDialog(this, mensaje, aviso,JOptionPane.ERROR_MESSAGE);
    }

    //Funcion para comenzar el Log. Lo crea si no existe en el working directory.
    private void startLog(){
    
    File archivo = new File(ruta+log);
    BufferedWriter bw;
      
        try {
            if(archivo.createNewFile()) {
                bw = new BufferedWriter(new FileWriter(archivo));
                bw.write("FICHERO DE TEXTO CREADO");
                bw.newLine();
            } else {
                bw = new BufferedWriter(new FileWriter(archivo,true));
                java.util.Date now = new java.util.Date();
                bw.write("    Se inicia una nueva sesión a las " + formatofh.format(now));
                bw.newLine();
            }  
            bw.close(); 
        } catch (IOException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
    
    //Funcion para añadir texto al log.
    public void addLog(String registro){
    File archivo = new File(ruta+log);
    BufferedWriter bw;
      
        try {
                bw = new BufferedWriter(new FileWriter(archivo,true));
                bw.write(registro);
                bw.newLine();
            bw.close(); 
        } catch (IOException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
    
    //Seguro que te imaginas para que es...
    private String leerFloat(String pregunta){
        float b=0; //Inicializamos a cualquier valor para que no de compilacion en error en netbeans (variable a might not have been initializated)
        String a= null;
        boolean error;
    
        do{
            try{
                error = false;
                a=(JOptionPane.showInputDialog(pregunta));
                if (a!=null)
                    b = Float.parseFloat(a);
            }
            catch(InputMismatchException e)
            {
                toastMessage("Aviso","Valor introducido por teclado no válido.");
                System.out.println("Valor introducido por teclado no válido."+e);    
                error = true;
            }
              catch(NumberFormatException e)
            {
                System.out.println("Valor introducido por teclado no válido2."+e);    
                error = true;
            }
        }while(error);
    return a;
    }
    
    //Devuelve 1 si el empleado existe, 0 si no. ( y -1 en algun caso raro. xD )
    private int existeEmpleado(int numero){
        try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            auxiliar.ejecutaSQLq("select distinct empleado.num_emp from empleado");
            auxiliar.irAlFinal();
            int total=auxiliar.rs.getRow();
            auxiliar.irAlPrimero();
            for (int i=0;i<total;i++){
                if(numero==auxiliar.rs.getInt(1)){
                    auxiliar.cerrarConexion();
                    return 1;//Almacenamos la tarea en la variable global tareas[], que es un array de Strings 
                }
                auxiliar.irAlSiguiente();
            }
            auxiliar.cerrarConexion();
            return 0;
            
        } catch (SQLException ex) {
            Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);}
    return -1;
    }
    
     //Funcion que rellena todos los combos automaticamente.  
    private void Fillcombos(){
            String item;
            try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            //Rellenamos el combo de jefes, donde cualquier empleado puede serlo en principio.
            auxiliar.ejecutaSQLq("select num_emp from empleado");  //No hace falta usar 'distinct' pues num_emp es PK.
            auxiliar.irAlFinal();
            int total=auxiliar.rs.getRow();
            auxiliar.irAlPrimero();
            tareas=new String[total];  
            jefeCB.addItem("0");
            for (int i=0;i<total;i++){
                item=auxiliar.rs.getString(1);  //Almacenamos la tarea en la variable global tareas[], que es un array de Strings 
                jefeCB.addItem(item);
                auxiliar.irAlSiguiente();}
            
            //Rellenamos el combo de departamentos.
            auxiliar.ejecutaSQLq("select distinct num_dpto from dpto");
            auxiliar.irAlFinal();
            total=auxiliar.rs.getRow();
            auxiliar.irAlPrimero();
            tareas=new String[total];  
            for (int i=0;i<total;i++){
                item=auxiliar.rs.getString(1);  //Almacenamos la tarea en la variable global tareas[], que es un array de Strings 
                departamentoCB.addItem(item);
                auxiliar.irAlSiguiente();}
            
            auxiliar.cerrarConexion();
        } catch (SQLException ex) {Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);}
        //
            
        }
     
    //(Funcion para recoger automaticamente todas las tareas en la BBD) Se usa para la consulta de gasto por tareas.
    private void iniciarTareas(){
             try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            auxiliar.ejecutaSQLq("select distinct tarea from empleado");
            auxiliar.irAlFinal();
            int total=auxiliar.rs.getRow();
            auxiliar.irAlPrimero();
            tareas=new String[total];   
            for (int i=0;i<total;i++){
                tareas[i]=auxiliar.rs.getString(1);  //Almacenamos la tarea en la variable global tareas[], que es un array de Strings 
                auxiliar.irAlSiguiente();}
            auxiliar.cerrarConexion();
        } catch (SQLException ex) {Logger.getLogger(EvaluableV3.class.getName()).log(Level.SEVERE, null, ex);}
        // FIN.Solo
        }
        
    //Funcion que instala las dependencias de la BBDD automaticamente.
    public boolean instalarDependencias(){
            boolean bool;
        try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            String sqlt= "CREATE OR REPLACE FUNCTION "+funcionBBDD+" (tarea_ IN VARCHAR2) RETURN NUMBER IS suma NUMBER:=0; BEGIN SELECT SUM(salario) into suma from empleado WHERE tarea=tarea_; RETURN suma; END;";
            auxiliar.ejecutaSQL(sqlt);
            System.out.println("Funcion "+funcionBBDD+" instalada con éxito");
            sqlt="CREATE OR REPLACE TRIGGER "+triggerBBDD+" \n" +
            "AFTER UPDATE\nON empleado\nFOR EACH ROW \nBEGIN\nIF (:NEW.salario<>:OLD.salario) THEN\n" +
            "INSERT INTO empleados_modificados VALUES (:OLD.num_emp,:OLD.nombre,:OLD.salario,:NEW.salario,user,TO_DATE(SYSDATE,'dd/mm/yyyy'));\n" +
            "DBMS_OUTPUT.PUT_LINE('MODIFICACION DE SALARIO REGISTRADO');\nEND IF;\nEND;" ;  
            auxiliar.ejecutaSQL(sqlt);
            System.out.println("Trigger "+triggerBBDD+" instalada con éxito");
            auxiliar.cerrarConexion();
            toastMessage("Aviso SQL","Trigger "+triggerBBDD+" y Funcion "+funcionBBDD+" \n instalados en la BBDD con éxito");
            bool= true;
         } catch (SQLException ex) {
             bool=false;
            toastMessage("Aviso SQL","Hubo algun problema al instalar las dependencias en la BBDD.\nVerifique su configuracion.");
            //Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bool;
        }
    
    //Funcion que desinstala las dependencias de la BBDD automaticamente.    
    public boolean desinstalarDependencias(){
            boolean bool;
        
        try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            String sqlt= "drop TRIGGER "+triggerBBDD+"";
            auxiliar.ejecutaSQL(sqlt);
            System.out.println("Trigger "+triggerBBDD+" desinstalada con éxito");
            sqlt= "drop FUNCTION "+funcionBBDD;
            auxiliar.ejecutaSQL(sqlt);
            System.out.println("Funcion "+funcionBBDD+" desinstalada con éxito");
            auxiliar.cerrarConexion();
            toastMessage("Aviso SQL", "Trigger "+triggerBBDD+" y Funcion "+funcionBBDD+" \n desinstalados de la BBDD con éxito");
            bool=true;
        }catch (SQLException ex) {
            bool=false;
            toastMessage("Aviso SQL","Hubo algun problema al desinstalar las dependencias en la BBDD.\nVerifique su configuracion.");
           // Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
                return bool;
        }  
    
    //Funcion que habilita el trigger de la BBDD automaticamente.
    public boolean habilitarTrigger(){
        boolean bool;
        try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            String sqla= "ALTER TRIGGER "+triggerBBDD+" enable";  
            auxiliar.ejecutaSQL(sqla);
            auxiliar.cerrarConexion();
            toastMessage("Aviso SQL","Trigger "+triggerBBDD+" activado");
            bool = true;
        } catch (SQLException ex) {
            bool = false;
            toastMessage("Aviso SQL","No se encontraron las Dependencias. Instalelas");
            //Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bool;
    }
    
    //Funcion que deshabilita el trigger de la BBDD automaticamente.
    public boolean deshabilitarTrigger(){
        boolean bool;
                try {
            auxiliar.conecta(usuario,contrasena,puerto);
            auxiliar.crearSentencias();
            String sqla= "ALTER TRIGGER "+triggerBBDD+" disable";  
            auxiliar.ejecutaSQL(sqla);
            auxiliar.cerrarConexion();
            toastMessage("Aviso SQL","Trigger "+triggerBBDD+" desactivado");
            bool = true;
        } catch (SQLException ex) {
            bool = false;
            toastMessage("Aviso SQL","No se encontraron las Dependencias. Instalelas");
            //Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bool;
    } 
    
    
    public void abrirarchivo(String archivo){

     try {

            File objetofile = new File (archivo);
            Desktop.getDesktop().open(objetofile);

     }catch (IOException ex) {
          toastMessage("Aviso","disculpe, no se encontró el archivo de Ayuda.");
            System.out.println("No se encontro el archivo de ayuda"+ex);

     }

}  
    
   
    
}
