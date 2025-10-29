package com.taller.proyecto_bd.ui.entidades;

import com.taller.proyecto_bd.dao.CategoriaDAO;
import com.taller.proyecto_bd.dao.ClienteDAO;
import com.taller.proyecto_bd.dao.ProductoDAO;
import com.taller.proyecto_bd.dao.UsuarioDAO;
import com.taller.proyecto_bd.models.Categoria;
import com.taller.proyecto_bd.models.Cliente;
import com.taller.proyecto_bd.models.Producto;
import com.taller.proyecto_bd.models.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;

/**
 * Controlador JavaFX para gestionar las entidades maestras del sistema.
 */
public class EntidadesController {

    // DAOs
    private final ProductoDAO productoDAO = ProductoDAO.getInstance();
    private final CategoriaDAO categoriaDAO = CategoriaDAO.getInstance();
    private final ClienteDAO clienteDAO = ClienteDAO.getInstance();
    private final UsuarioDAO usuarioDAO = UsuarioDAO.getInstance();

    // Listas observables
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    // --- Productos ---
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, String> colProductoCodigo;
    @FXML
    private TableColumn<Producto, String> colProductoNombre;
    @FXML
    private TableColumn<Producto, String> colProductoCategoria;
    @FXML
    private TableColumn<Producto, String> colProductoPrecio;
    @FXML
    private TableColumn<Producto, String> colProductoStock;
    @FXML
    private TextField txtProductoCodigo;
    @FXML
    private TextField txtProductoNombre;
    @FXML
    private TextField txtProductoMarca;
    @FXML
    private TextField txtProductoModelo;
    @FXML
    private ComboBox<Categoria> cbProductoCategoria;
    @FXML
    private TextField txtProductoPrecioCompra;
    @FXML
    private TextField txtProductoPrecioVenta;
    @FXML
    private TextField txtProductoStock;
    @FXML
    private TextField txtProductoStockMinimo;
    @FXML
    private Label lblProductoMensaje;

    // --- Clientes ---
    @FXML
    private TableView<Cliente> tablaClientes;
    @FXML
    private TableColumn<Cliente, String> colClienteCedula;
    @FXML
    private TableColumn<Cliente, String> colClienteNombre;
    @FXML
    private TableColumn<Cliente, String> colClienteTelefono;
    @FXML
    private TableColumn<Cliente, String> colClienteSaldo;
    @FXML
    private TextField txtClienteCedula;
    @FXML
    private TextField txtClienteNombre;
    @FXML
    private TextField txtClienteApellido;
    @FXML
    private TextField txtClienteDireccion;
    @FXML
    private TextField txtClienteTelefono;
    @FXML
    private TextField txtClienteEmail;
    @FXML
    private TextField txtClienteLimite;
    @FXML
    private TextField txtClienteSaldo;
    @FXML
    private CheckBox chkClienteActivo;
    @FXML
    private Label lblClienteMensaje;

    // --- Categorías ---
    @FXML
    private TableView<Categoria> tablaCategorias;
    @FXML
    private TableColumn<Categoria, String> colCategoriaCodigo;
    @FXML
    private TableColumn<Categoria, String> colCategoriaNombre;
    @FXML
    private TableColumn<Categoria, String> colCategoriaNivel;
    @FXML
    private TableColumn<Categoria, String> colCategoriaEstado;
    @FXML
    private TextField txtCategoriaCodigo;
    @FXML
    private TextField txtCategoriaNombre;
    @FXML
    private TextArea txtCategoriaDescripcion;
    @FXML
    private ComboBox<Categoria> cbCategoriaPadre;
    @FXML
    private ComboBox<Integer> cbCategoriaNivel;
    @FXML
    private CheckBox chkCategoriaActiva;
    @FXML
    private Label lblCategoriaMensaje;

    // --- Usuarios ---
    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, String> colUsuarioNombre;
    @FXML
    private TableColumn<Usuario, String> colUsuarioUsername;
    @FXML
    private TableColumn<Usuario, String> colUsuarioRol;
    @FXML
    private TableColumn<Usuario, String> colUsuarioEstado;
    @FXML
    private TextField txtUsuarioNombre;
    @FXML
    private TextField txtUsuarioUsername;
    @FXML
    private PasswordField txtUsuarioPassword;
    @FXML
    private ComboBox<String> cbUsuarioRol;
    @FXML
    private TextField txtUsuarioEmail;
    @FXML
    private TextField txtUsuarioTelefono;
    @FXML
    private CheckBox chkUsuarioActivo;
    @FXML
    private Label lblUsuarioMensaje;

    @FXML
    private void initialize() {
        configurarTablas();
        configurarCombos();
        cargarDatosIniciales();
        configurarListeners();
    }

    private void configurarTablas() {
        tablaProductos.setItems(productos);
        colProductoCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProductoNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProductoCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(obtenerNombreCategoria(data.getValue().getIdCategoria())));
        colProductoPrecio.setCellValueFactory(data ->
                new SimpleStringProperty(String.format(Locale.US, "$%.2f", data.getValue().getPrecioVenta())));
        colProductoStock.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getStockActual())));

        tablaClientes.setItems(clientes);
        colClienteCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colClienteNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colClienteTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colClienteSaldo.setCellValueFactory(data ->
                new SimpleStringProperty(String.format(Locale.US, "$%.2f", data.getValue().getSaldoPendiente())));

        tablaCategorias.setItems(categorias);
        colCategoriaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCategoriaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoriaNivel.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getNivel())));
        colCategoriaEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActivo() ? "Activa" : "Inactiva"));

        tablaUsuarios.setItems(usuarios);
        colUsuarioNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colUsuarioUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUsuarioRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colUsuarioEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Bloqueado"));
    }

    private void configurarCombos() {
        cbProductoCategoria.setConverter(crearConvertidorCategoria());
        cbProductoCategoria.setCellFactory(listView -> new CategoriaListCell());
        cbCategoriaPadre.setConverter(crearConvertidorCategoria());
        cbCategoriaPadre.setCellFactory(listView -> new CategoriaListCell());

        cbCategoriaNivel.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        cbCategoriaNivel.getSelectionModel().selectFirst();

        cbUsuarioRol.setItems(FXCollections.observableArrayList("ADMIN", "VENDEDOR", "GERENTE", "CAJERO"));
        cbUsuarioRol.getSelectionModel().selectFirst();
    }

    private void cargarDatosIniciales() {
        refrescarCategorias();
        refrescarProductos();
        refrescarClientes();
        refrescarUsuarios();
    }

    private void configurarListeners() {
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarProducto(seleccionado));

        tablaClientes.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarCliente(seleccionado));

        tablaCategorias.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarCategoria(seleccionado));

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarUsuario(seleccionado));
    }

    // ----- Productos -----

    @FXML
    private void guardarProducto() {
        limpiarMensaje(lblProductoMensaje);
        Categoria categoria = cbProductoCategoria.getValue();
        if (categoria == null) {
            mostrarMensaje(lblProductoMensaje, "Seleccione una categoria.", true);
            return;
        }

        try {
            String codigo = txtProductoCodigo.getText().trim();
            String nombre = txtProductoNombre.getText().trim();
            String marca = txtProductoMarca.getText().trim();
            String modelo = txtProductoModelo.getText().trim();
            double precioCompra = Double.parseDouble(txtProductoPrecioCompra.getText().trim());
            double precioVenta = Double.parseDouble(txtProductoPrecioVenta.getText().trim());
            int stockActual = Integer.parseInt(txtProductoStock.getText().trim());
            int stockMinimo = Integer.parseInt(txtProductoStockMinimo.getText().trim());

            Producto producto = new Producto(codigo, nombre, marca, modelo,
                    categoria.getIdCategoria(), precioCompra, precioVenta,
                    stockActual, stockMinimo);
            producto.setNombreCategoria(categoria.getNombre());

            if (productoDAO.agregar(producto)) {
                mostrarMensaje(lblProductoMensaje, "Producto registrado correctamente.", false);
                refrescarProductos();
                limpiarProducto();
            } else {
                mostrarMensaje(lblProductoMensaje, "No se pudo guardar el producto. Revise los datos ingresados.", true);
            }
        } catch (NumberFormatException ex) {
            mostrarMensaje(lblProductoMensaje, "Valores numéricos inválidos.", true);
        }
    }

    @FXML
    private void actualizarProducto() {
        limpiarMensaje(lblProductoMensaje);
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje(lblProductoMensaje, "Seleccione un producto para actualizar.", true);
            return;
        }

        Categoria categoria = cbProductoCategoria.getValue();
        if (categoria == null) {
            mostrarMensaje(lblProductoMensaje, "Seleccione una categoria.", true);
            return;
        }

        try {
            seleccionado.setCodigo(txtProductoCodigo.getText().trim());
            seleccionado.setNombre(txtProductoNombre.getText().trim());
            seleccionado.setMarca(txtProductoMarca.getText().trim());
            seleccionado.setModelo(txtProductoModelo.getText().trim());
            seleccionado.setIdCategoria(categoria.getIdCategoria());
            seleccionado.setNombreCategoria(categoria.getNombre());
            seleccionado.setPrecioCompra(Double.parseDouble(txtProductoPrecioCompra.getText().trim()));
            seleccionado.setPrecioVenta(Double.parseDouble(txtProductoPrecioVenta.getText().trim()));
            seleccionado.setStockActual(Integer.parseInt(txtProductoStock.getText().trim()));
            seleccionado.setStockMinimo(Integer.parseInt(txtProductoStockMinimo.getText().trim()));

            if (productoDAO.actualizar(seleccionado)) {
                mostrarMensaje(lblProductoMensaje, "Producto actualizado correctamente.", false);
                refrescarProductos();
            } else {
                mostrarMensaje(lblProductoMensaje, "No se pudo actualizar el producto.", true);
            }
        } catch (NumberFormatException ex) {
            mostrarMensaje(lblProductoMensaje, "Valores numéricos inválidos.", true);
        }
    }

    @FXML
    private void eliminarProducto() {
        limpiarMensaje(lblProductoMensaje);
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje(lblProductoMensaje, "Seleccione un producto para eliminar.", true);
            return;
        }

        if (productoDAO.eliminar(seleccionado.getIdProducto())) {
            mostrarMensaje(lblProductoMensaje, "Producto eliminado correctamente.", false);
            refrescarProductos();
            limpiarProducto();
        } else {
            mostrarMensaje(lblProductoMensaje, "No se pudo eliminar el producto.", true);
        }
    }

    @FXML
    private void limpiarProducto() {
        tablaProductos.getSelectionModel().clearSelection();
        txtProductoCodigo.clear();
        txtProductoNombre.clear();
        txtProductoMarca.clear();
        txtProductoModelo.clear();
        txtProductoPrecioCompra.clear();
        txtProductoPrecioVenta.clear();
        txtProductoStock.clear();
        txtProductoStockMinimo.clear();
        cbProductoCategoria.getSelectionModel().clearSelection();
        limpiarMensaje(lblProductoMensaje);
    }

    private void mostrarProducto(Producto producto) {
        if (producto == null) {
            return;
        }
        txtProductoCodigo.setText(producto.getCodigo());
        txtProductoNombre.setText(producto.getNombre());
        txtProductoMarca.setText(producto.getMarca());
        txtProductoModelo.setText(producto.getModelo());
        txtProductoPrecioCompra.setText(String.valueOf(producto.getPrecioCompra()));
        txtProductoPrecioVenta.setText(String.valueOf(producto.getPrecioVenta()));
        txtProductoStock.setText(String.valueOf(producto.getStockActual()));
        txtProductoStockMinimo.setText(String.valueOf(producto.getStockMinimo()));

        Categoria categoria = buscarCategoriaPorId(producto.getIdCategoria());
        if (categoria != null) {
            cbProductoCategoria.getSelectionModel().select(categoria);
        } else {
            cbProductoCategoria.getSelectionModel().clearSelection();
        }
    }

    private void refrescarProductos() {
        productos.setAll(productoDAO.obtenerTodos());
        for (Producto producto : productos) {
            Categoria categoria = buscarCategoriaPorId(producto.getIdCategoria());
            if (categoria != null) {
                producto.setNombreCategoria(categoria.getNombre());
            } else {
                producto.setNombreCategoria("Sin categoria");
            }
        }
        tablaProductos.refresh();
    }

    // ----- Clientes -----

    @FXML
    private void guardarCliente() {
        limpiarMensaje(lblClienteMensaje);
        try {
            Cliente cliente = new Cliente(
                    txtClienteCedula.getText().trim(),
                    txtClienteNombre.getText().trim(),
                    txtClienteApellido.getText().trim(),
                    txtClienteDireccion.getText().trim(),
                    txtClienteTelefono.getText().trim(),
                    txtClienteEmail.getText().trim()
            );
            cliente.setLimiteCredito(Double.parseDouble(obtenerTextoNumerico(txtClienteLimite)));
            cliente.setSaldoPendiente(Double.parseDouble(obtenerTextoNumerico(txtClienteSaldo)));
            cliente.setActivo(chkClienteActivo.isSelected());

            if (clienteDAO.agregar(cliente)) {
                mostrarMensaje(lblClienteMensaje, "Cliente registrado correctamente.", false);
                refrescarClientes();
                limpiarCliente();
            } else {
                mostrarMensaje(lblClienteMensaje, "No se pudo guardar el cliente. Verifique los datos obligatorios.", true);
            }
        } catch (NumberFormatException ex) {
            mostrarMensaje(lblClienteMensaje, "Valores numéricos inválidos para crédito o saldo.", true);
        }
    }

    @FXML
    private void actualizarCliente() {
        limpiarMensaje(lblClienteMensaje);
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje(lblClienteMensaje, "Seleccione un cliente para actualizar.", true);
            return;
        }

        try {
            seleccionado.setCedula(txtClienteCedula.getText().trim());
            seleccionado.setNombre(txtClienteNombre.getText().trim());
            seleccionado.setApellido(txtClienteApellido.getText().trim());
            seleccionado.setDireccion(txtClienteDireccion.getText().trim());
            seleccionado.setTelefono(txtClienteTelefono.getText().trim());
            seleccionado.setEmail(txtClienteEmail.getText().trim());
            seleccionado.setLimiteCredito(Double.parseDouble(obtenerTextoNumerico(txtClienteLimite)));
            seleccionado.setSaldoPendiente(Double.parseDouble(obtenerTextoNumerico(txtClienteSaldo)));
            seleccionado.setActivo(chkClienteActivo.isSelected());

            if (clienteDAO.actualizar(seleccionado)) {
                mostrarMensaje(lblClienteMensaje, "Cliente actualizado correctamente.", false);
                refrescarClientes();
            } else {
                mostrarMensaje(lblClienteMensaje, "No se pudo actualizar el cliente.", true);
            }
        } catch (NumberFormatException ex) {
            mostrarMensaje(lblClienteMensaje, "Valores numéricos inválidos.", true);
        }
    }

    @FXML
    private void eliminarCliente() {
        limpiarMensaje(lblClienteMensaje);
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje(lblClienteMensaje, "Seleccione un cliente para eliminar.", true);
            return;
        }

        if (clienteDAO.eliminar(seleccionado.getIdCliente())) {
            mostrarMensaje(lblClienteMensaje, "Cliente eliminado correctamente.", false);
            refrescarClientes();
            limpiarCliente();
        } else {
            mostrarMensaje(lblClienteMensaje, "No se pudo eliminar el cliente.", true);
        }
    }

    @FXML
    private void limpiarCliente() {
        tablaClientes.getSelectionModel().clearSelection();
        txtClienteCedula.clear();
        txtClienteNombre.clear();
        txtClienteApellido.clear();
        txtClienteDireccion.clear();
        txtClienteTelefono.clear();
        txtClienteEmail.clear();
        txtClienteLimite.clear();
        txtClienteSaldo.clear();
        chkClienteActivo.setSelected(true);
        limpiarMensaje(lblClienteMensaje);
    }

    private void mostrarCliente(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        txtClienteCedula.setText(cliente.getCedula());
        txtClienteNombre.setText(cliente.getNombre());
        txtClienteApellido.setText(cliente.getApellido());
        txtClienteDireccion.setText(cliente.getDireccion());
        txtClienteTelefono.setText(cliente.getTelefono());
        txtClienteEmail.setText(cliente.getEmail());
        txtClienteLimite.setText(String.valueOf(cliente.getLimiteCredito()));
        txtClienteSaldo.setText(String.valueOf(cliente.getSaldoPendiente()));
        chkClienteActivo.setSelected(cliente.isActivo());
    }

    private void refrescarClientes() {
        clientes.setAll(clienteDAO.obtenerTodos());
        tablaClientes.refresh();
    }

    // ----- Categorías -----

    @FXML
    private void guardarCategoria() {
        limpiarMensaje(lblCategoriaMensaje);
        try {
            String codigo = txtCategoriaCodigo.getText().trim();
            String nombre = txtCategoriaNombre.getText().trim();
            String descripcion = txtCategoriaDescripcion.getText().trim();
            Integer nivel = cbCategoriaNivel.getValue();
            Categoria categoriaPadre = cbCategoriaPadre.getValue();

            Categoria categoria = new Categoria(codigo, nombre, descripcion);
            categoria.setActivo(chkCategoriaActiva.isSelected());
            categoria.setNivel(nivel);
            if (categoriaPadre != null) {
                categoria.setIdCategoriaPadre(categoriaPadre.getIdCategoria());
            }

            if (categoriaDAO.agregar(categoria)) {
                mostrarMensaje(lblCategoriaMensaje, "Categoría registrada correctamente.", false);
                refrescarCategorias();
                limpiarCategoria();
            } else {
                mostrarMensaje(lblCategoriaMensaje, "No se pudo guardar la categoría. Revise los datos ingresados.", true);
            }
        } catch (Exception ex) {
            mostrarMensaje(lblCategoriaMensaje, "Ocurrió un error al guardar la categoría.", true);
        }
    }

    @FXML
    private void actualizarCategoria() {
        limpiarMensaje(lblCategoriaMensaje);
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensaje(lblCategoriaMensaje, "Seleccione una categoría para actualizar.", true);
            return;
        }

        try {
            seleccionada.setCodigo(txtCategoriaCodigo.getText().trim());
            seleccionada.setNombre(txtCategoriaNombre.getText().trim());
            seleccionada.setDescripcion(txtCategoriaDescripcion.getText().trim());
            seleccionada.setNivel(cbCategoriaNivel.getValue());
            seleccionada.setActivo(chkCategoriaActiva.isSelected());

            Categoria padre = cbCategoriaPadre.getValue();
            if (padre != null && padre.getIdCategoria() == seleccionada.getIdCategoria()) {
                mostrarMensaje(lblCategoriaMensaje, "Una categoría no puede ser su propio padre.", true);
                return;
            }
            seleccionada.setIdCategoriaPadre(padre != null ? padre.getIdCategoria() : null);

            if (categoriaDAO.actualizar(seleccionada)) {
                mostrarMensaje(lblCategoriaMensaje, "Categoría actualizada correctamente.", false);
                refrescarCategorias();
                refrescarProductos();
            } else {
                mostrarMensaje(lblCategoriaMensaje, "No se pudo actualizar la categoría.", true);
            }
        } catch (Exception ex) {
            mostrarMensaje(lblCategoriaMensaje, "Ocurrió un error al actualizar la categoría.", true);
        }
    }

    @FXML
    private void eliminarCategoria() {
        limpiarMensaje(lblCategoriaMensaje);
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensaje(lblCategoriaMensaje, "Seleccione una categoría para eliminar.", true);
            return;
        }

        if (categoriaDAO.eliminar(seleccionada.getIdCategoria())) {
            mostrarMensaje(lblCategoriaMensaje, "Categoría eliminada correctamente.", false);
            refrescarCategorias();
            refrescarProductos();
            limpiarCategoria();
        } else {
            mostrarMensaje(lblCategoriaMensaje, "No se pudo eliminar la categoría (puede tener productos asociados).", true);
        }
    }

    @FXML
    private void limpiarCategoria() {
        tablaCategorias.getSelectionModel().clearSelection();
        txtCategoriaCodigo.clear();
        txtCategoriaNombre.clear();
        txtCategoriaDescripcion.clear();
        cbCategoriaPadre.getSelectionModel().clearSelection();
        cbCategoriaNivel.getSelectionModel().selectFirst();
        chkCategoriaActiva.setSelected(true);
        limpiarMensaje(lblCategoriaMensaje);
    }

    private void mostrarCategoria(Categoria categoria) {
        if (categoria == null) {
            return;
        }
        txtCategoriaCodigo.setText(categoria.getCodigo());
        txtCategoriaNombre.setText(categoria.getNombre());
        txtCategoriaDescripcion.setText(categoria.getDescripcion());
        cbCategoriaNivel.getSelectionModel().select(Integer.valueOf(categoria.getNivel()));
        chkCategoriaActiva.setSelected(categoria.isActivo());

        if (categoria.getIdCategoriaPadre() != null) {
            Categoria padre = buscarCategoriaPorId(categoria.getIdCategoriaPadre());
            if (padre != null) {
                cbCategoriaPadre.getSelectionModel().select(padre);
            } else {
                cbCategoriaPadre.getSelectionModel().clearSelection();
            }
        } else {
            cbCategoriaPadre.getSelectionModel().clearSelection();
        }
    }

    private void refrescarCategorias() {
        categorias.setAll(categoriaDAO.obtenerTodas());
        cbProductoCategoria.setItems(FXCollections.observableArrayList(categorias));
        cbCategoriaPadre.setItems(FXCollections.observableArrayList(categorias));
        tablaCategorias.refresh();
    }

    // ----- Usuarios -----

    @FXML
    private void guardarUsuario() {
        limpiarMensaje(lblUsuarioMensaje);
        String nombreCompleto = txtUsuarioNombre.getText().trim();
        String username = txtUsuarioUsername.getText().trim();
        String password = txtUsuarioPassword.getText();
        String rol = cbUsuarioRol.getValue();

        if (nombreCompleto.isEmpty() || username.isEmpty() || password.isEmpty()) {
            mostrarMensaje(lblUsuarioMensaje, "Nombre, usuario y contraseña son obligatorios.", true);
            return;
        }

        Usuario usuario = new Usuario(nombreCompleto, username, password, rol);
        usuario.setEmail(txtUsuarioEmail.getText().trim());
        usuario.setTelefono(txtUsuarioTelefono.getText().trim());
        usuario.setActivo(chkUsuarioActivo.isSelected());

        if (usuarioDAO.agregar(usuario)) {
            mostrarMensaje(lblUsuarioMensaje, "Usuario registrado correctamente.", false);
            refrescarUsuarios();
            limpiarUsuario();
        } else {
            mostrarMensaje(lblUsuarioMensaje, "No se pudo guardar el usuario (verifique duplicados).", true);
        }
    }

    @FXML
    private void actualizarUsuario() {
        limpiarMensaje(lblUsuarioMensaje);
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje(lblUsuarioMensaje, "Seleccione un usuario para actualizar.", true);
            return;
        }

        seleccionado.setNombreCompleto(txtUsuarioNombre.getText().trim());
        seleccionado.setUsername(txtUsuarioUsername.getText().trim());
        seleccionado.setRol(cbUsuarioRol.getValue());
        seleccionado.setEmail(txtUsuarioEmail.getText().trim());
        seleccionado.setTelefono(txtUsuarioTelefono.getText().trim());
        seleccionado.setActivo(chkUsuarioActivo.isSelected());

        String nuevaPassword = txtUsuarioPassword.getText();
        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            seleccionado.setPassword(nuevaPassword);
        }

        if (usuarioDAO.actualizar(seleccionado)) {
            mostrarMensaje(lblUsuarioMensaje, "Usuario actualizado correctamente.", false);
            refrescarUsuarios();
        } else {
            mostrarMensaje(lblUsuarioMensaje, "No se pudo actualizar el usuario.", true);
        }
    }

    @FXML
    private void eliminarUsuario() {
        limpiarMensaje(lblUsuarioMensaje);
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje(lblUsuarioMensaje, "Seleccione un usuario para eliminar.", true);
            return;
        }

        if (usuarioDAO.eliminar(seleccionado.getIdUsuario())) {
            mostrarMensaje(lblUsuarioMensaje, "Usuario eliminado correctamente.", false);
            refrescarUsuarios();
            limpiarUsuario();
        } else {
            mostrarMensaje(lblUsuarioMensaje, "No se pudo eliminar el usuario.", true);
        }
    }

    @FXML
    private void limpiarUsuario() {
        tablaUsuarios.getSelectionModel().clearSelection();
        txtUsuarioNombre.clear();
        txtUsuarioUsername.clear();
        txtUsuarioPassword.clear();
        txtUsuarioEmail.clear();
        txtUsuarioTelefono.clear();
        cbUsuarioRol.getSelectionModel().selectFirst();
        chkUsuarioActivo.setSelected(true);
        limpiarMensaje(lblUsuarioMensaje);
    }

    private void mostrarUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        txtUsuarioNombre.setText(usuario.getNombreCompleto());
        txtUsuarioUsername.setText(usuario.getUsername());
        txtUsuarioPassword.clear();
        txtUsuarioEmail.setText(usuario.getEmail());
        txtUsuarioTelefono.setText(usuario.getTelefono());
        cbUsuarioRol.getSelectionModel().select(usuario.getRol());
        chkUsuarioActivo.setSelected(usuario.isActivo());
    }

    private void refrescarUsuarios() {
        usuarios.setAll(usuarioDAO.obtenerTodos());
        tablaUsuarios.refresh();
    }

    // ----- Utilitarios -----

    private Categoria buscarCategoriaPorId(int idCategoria) {
        for (Categoria categoria : categorias) {
            if (categoria.getIdCategoria() == idCategoria) {
                return categoria;
            }
        }
        return categoriaDAO.obtenerPorId(idCategoria);
    }

    private String obtenerNombreCategoria(int idCategoria) {
        Categoria categoria = buscarCategoriaPorId(idCategoria);
        return categoria != null ? categoria.getNombre() : "Sin categoria";
    }

    private void mostrarMensaje(Label label, String texto, boolean esError) {
        if (label == null) {
            return;
        }
        label.setText(texto);
        label.setStyle(esError ? "-fx-text-fill: #f94144;" : "-fx-text-fill: #43aa8b;");
    }

    private void limpiarMensaje(Label label) {
        if (label != null) {
            label.setText("");
            label.setStyle("");
        }
    }

    private String obtenerTextoNumerico(TextField textField) {
        String texto = textField.getText();
        return (texto == null || texto.isBlank()) ? "0" : texto.trim();
    }

    private StringConverter<Categoria> crearConvertidorCategoria() {
        return new StringConverter<>() {
            @Override
            public String toString(Categoria categoria) {
                if (categoria == null) {
                    return "";
                }
                return categoria.getNombre() != null ? categoria.getNombre() : categoria.getCodigo();
            }

            @Override
            public Categoria fromString(String string) {
                if (string == null) {
                    return null;
                }
                List<Categoria> actuales = cbProductoCategoria.getItems();
                return actuales.stream()
                        .filter(c -> string.equals(c.getNombre()) || string.equals(c.getCodigo()))
                        .findFirst()
                        .orElse(null);
            }
        };
    }

    private static class CategoriaListCell extends javafx.scene.control.ListCell<Categoria> {
        @Override
        protected void updateItem(Categoria categoria, boolean empty) {
            super.updateItem(categoria, empty);
            if (empty || categoria == null) {
                setText(null);
            } else {
                setText(categoria.getNombre());
            }
        }
    }
}
