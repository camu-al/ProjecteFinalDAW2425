package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MenuController implements Initializable {

	// Objecte a compartir amb l'altra escena
	private Usuaris usuariActual;

	public void setUsuari(Usuaris usuariActual) {
		this.usuariActual = usuariActual;
	}

	public Usuaris getUsuari() {
		return this.usuariActual;
	}

	// Atributos Usuaris
	private String emailUsuari;

	// variables
	@FXML
	private VBox root;
	@FXML
	private ImageView avatarImg;
	@FXML
	private Label textoBienvenido;
	@FXML
	private Label textoJuegos;
	@FXML
	private ImageView imgAjustes;
	@FXML
	private ImageView imgBuscaMines;
	@FXML
	private ImageView imgPixelArt;
	@FXML
	private ImageView imgJocVida;
	@FXML
	private ImageView imgWordles;
	@FXML
	private Button botoBuscaMines;
	@FXML
	private Button botoPixelArt;
	@FXML
	private Button botoJocVida;
	@FXML
	private Button botoWordles;

	// Variables menu ajustes
	private ContextMenu menuFoto;
	private MenuItem itemCambiarFoto;
	private MenuItem itemBorrarUsuari;
	private MenuItem itemTanca;
	private SeparatorMenuItem separador;

	// Menu ajustes desplegable
	@FXML
	public void menuDesplegable(MouseEvent event) {
		Platform.runLater(() -> {

			try {
				// menu de austes
				menuFoto = new ContextMenu();
				// opcions del menu
				itemCambiarFoto = new MenuItem("Cambiar foto de perfil");
				itemBorrarUsuari = new MenuItem("Eliminar cuenta");
				itemTanca = new MenuItem("Cerrar sesión");
				separador = new SeparatorMenuItem();

				itemCambiarFoto.setStyle("-fx-font-family: 'CutePixel'; -fx-font-size: 14px;");
				itemBorrarUsuari.setStyle("-fx-font-family: 'CutePixel'; -fx-font-size: 14px;");
				itemTanca.setStyle("-fx-font-family: 'CutePixel'; -fx-font-size: 14px;");

				// Añadir les opcions al menu
				menuFoto.getItems().addAll(itemCambiarFoto, separador, itemBorrarUsuari, itemTanca);

				// Funcio cambiar foto de perfil desde el menu
				itemCambiarFoto.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						System.out.println("Cambiar foto de perfil. ");
						try {
							cambiarAvatar(event);
						} catch (ClassNotFoundException | IOException e) {
							e.printStackTrace();
						}
					}
				});

				// Tancar sesio desde el menu
				itemTanca.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							logout(event);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				// Borrar usuari de la base de datos desde el menu
				itemBorrarUsuari.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						System.out.println("Borrar usuari. ");
						try {
							borrarUsuari(event);
						} catch (ClassNotFoundException | IOException e) {
							e.printStackTrace();
						}
					}
				});

				// Mostrar Menu Ajustes
				Node source = (Node) event.getSource();
				menuFoto.show(source, event.getScreenX(), event.getScreenY());

			} catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	// Boton borrar usuario
	@FXML
	public void borrarUsuari(ActionEvent event) throws ClassNotFoundException, IOException {
		try {
			// Carregar el controlador per a la BD
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Establir la connexió
			String urlBaseDades = "jdbc:mysql://localhost:3306/cal";
			String usuari = "root";
			String contrasenya = "";

			Connection c = DriverManager.getConnection(urlBaseDades, usuari, contrasenya);
			String sentencia = "DELETE FROM usuarios where email = ?";
			PreparedStatement s = c.prepareStatement(sentencia);
			s.setString(1, emailUsuari);

			int resultat = s.executeUpdate();

			if (resultat > 0) {
				System.out.println("Usuari eliminat. ");
				logout(event);
			} else {
				System.out.println("No existeix ningun email .");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// Cambiar img del avatar
	// Pasar la img a fichero bite i el fichero guardarlo en la base de datos
	@FXML
	public void cambiarAvatar(ActionEvent event) throws ClassNotFoundException, IOException {
		try {
			try {
				// Carregar el controlador per a la BD
				Class.forName("com.mysql.cj.jdbc.Driver");

				// Establir la connexió
				String urlBaseDades = "jdbc:mysql://localhost:3306/cal";
				String usuari = "root";
				String contrasenya = "";
				Connection c = DriverManager.getConnection(urlBaseDades, usuari, contrasenya);

				// Elegir el path de la img
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Selecciona una imagen");

				File file = fileChooser.showOpenDialog(null);

				if (file != null) {
					// Actualizar img en la base de datos segun el email
					FileInputStream inputStream = new FileInputStream(file);
					String sentencia = "UPDATE usuarios SET img = ? WHERE email = ?";
					PreparedStatement s = c.prepareStatement(sentencia);
					s.setBinaryStream(1, inputStream, (int) file.length());
					s.setString(2, emailUsuari);
					int filas = s.executeUpdate();

					if (filas > 0) {
						// Añadir la foto visualment
						System.out.println("Avatar actualizat");
						Image imgAvatar = new Image(file.toURI().toString());
						avatarImg.setImage(imgAvatar);
					} else {
						System.out.println("No se pudo actualizar la imagen");
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {

		}
	}

	// Boto per a entrar als jocs
	@FXML
	public void obrirBuscaMines(MouseEvent event) {
	    finestraOberta dades = finestraOberta.getInstancia();
	    System.out.println("¿Está abierta la ventana del juego?: " + dades.isOberta());

	    if (!dades.isOberta()) {
	        try {
	            // Crear el FXMLLoader para acceder al controlador
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("TamanyBuscamines.fxml"));
	            VBox rootBuscaMines = loader.load(); // Cargar el FXML

	            Scene pantallaBuscaMines = new Scene(rootBuscaMines);
	            pantallaBuscaMines.getStylesheets().add(
	                getClass().getResource("tamanyBuscamines.css").toExternalForm()
	            );

	            // Obtener el controlador
	            TamanyBuscaminesController controlador = loader.getController();
	            controlador.setUsername(usuariActual.getEmail());


	            // Crear y configurar la nueva ventana
	            Stage windowBuscamines = new Stage();
	            windowBuscamines.setScene(pantallaBuscaMines);
	            windowBuscamines.setTitle("Busca Mines");

	            Stage windowMenu = (Stage) ((Node) event.getSource()).getScene().getWindow();
	            windowBuscamines.initOwner(windowMenu);
	            windowBuscamines.initModality(Modality.APPLICATION_MODAL);

	            // Marcar ventana como abierta
	            dades.setOberta(true);
	            System.out.println("Abriendo ventana de BuscaMines...");

	            // Manejar cierre de ventana
	            windowBuscamines.setOnCloseRequest(x -> {
	                System.out.println("Cerrando BuscaMines...");
	                dades.setOberta(false);
	            });

	            windowBuscamines.show();

	        } catch (Exception e) {
	            e.printStackTrace();
	            alertaError("Error al abrir BuscaMines", "Ha ocurrido un error al intentar abrir el juego. Intenta de nuevo.");
	        }
	    } else {
	        alertaError("Ventana abierta", "Ya hay una partida de BuscaMines en curso. Ciérrala antes de abrir otra.");
	    }
	}

	@FXML
	public void obrirPixelArt(MouseEvent event) {
		finestraOberta dades = finestraOberta.getInstancia();
		if (!dades.isOberta()) {
			try {
				VBox rootPixelArt = (VBox) FXMLLoader.load(getClass().getResource("TamanyPixelart.fxml"));
				Scene pantallaPixelArt = new Scene(rootPixelArt);
				pantallaPixelArt.getStylesheets().add(getClass().getResource("tamanyPixelart.css").toExternalForm());

				Stage windoePixelArt = new Stage();
				windoePixelArt.setScene(pantallaPixelArt);
				windoePixelArt.setTitle("Pixel Art");

				Stage windowMenu = (Stage) ((Node) event.getSource()).getScene().getWindow();
				windoePixelArt.initOwner(windowMenu);
				windoePixelArt.initModality(Modality.APPLICATION_MODAL);

				dades.setOberta(true);
				System.out.println("Abrierto");

				windoePixelArt.setOnCloseRequest(x -> {
					System.out.println("Cerrando");
					dades.setOberta(false);
				});

				windoePixelArt.show();

			} catch (Exception e) {
				e.printStackTrace();
				alertaError("Error al abrir BuscaMines","Ha ocurrido un error al intentar abrir el juego. Intenta de nuevo.");
			}
		} else {
			alertaError("Ventana abierta", "Ya hay una partida de Pixel Art en curso. Ciérrala antes de abrir otra.");
		}
	}

	@FXML
	public void obrirJocVida(MouseEvent event) {
		finestraOberta dades = finestraOberta.getInstancia();
		if (!dades.isOberta()) {
			try {
				VBox rootJocVida = (VBox) FXMLLoader.load(getClass().getResource("ElegirTamanyJocDeLaVida.fxml"));
				Scene pantallaJocVida = new Scene(rootJocVida);
				pantallaJocVida.getStylesheets()
						.add(getClass().getResource("ElegirTamanyJocDeLaVida.css").toExternalForm());

				Stage windowJocVida = new Stage();
				windowJocVida.setScene(pantallaJocVida);
				windowJocVida.setTitle("Joc De La Vida");

				Stage windowMenu = (Stage) ((Node) event.getSource()).getScene().getWindow();
				windowJocVida.initOwner(windowMenu);
				windowJocVida.initModality(Modality.APPLICATION_MODAL);

				dades.setOberta(true);	
				System.out.println("Abrierto");

				windowJocVida.setOnCloseRequest(x -> {
					System.out.println("Cerrando");
					dades.setOberta(false);
				});

				windowJocVida.show();

			} catch (Exception e) {
				e.printStackTrace();
				alertaError("Error al abrir BuscaMines","Ha ocurrido un error al intentar abrir el juego. Intenta de nuevo.");
			}
		} else {
			alertaError("Ventana abierta", "Ya hay una partida del Joc de la vida en curso. Ciérrala antes de abrir otra.");
		}
	}

	@FXML
	public void obrirWordles(MouseEvent event) {
		finestraOberta dades = finestraOberta.getInstancia();
		if (!dades.isOberta()) {
			try {
				VBox rootWordles = (VBox) FXMLLoader.load(getClass().getResource("Wordle.fxml"));
				Scene pantallaWordles = new Scene(rootWordles);
				pantallaWordles.getStylesheets().add(getClass().getResource("Wordle.css").toExternalForm());

				Stage windowWordle = new Stage();
				windowWordle.setScene(pantallaWordles);
				windowWordle.setTitle("Wordle");

				Stage windowMenu = (Stage) ((Node) event.getSource()).getScene().getWindow();
				windowWordle.initOwner(windowMenu);
				windowWordle.initModality(Modality.APPLICATION_MODAL);

				dades.setOberta(true);
				System.out.println("Abrierto");

				windowWordle.setOnCloseRequest(x -> {
					System.out.println("Cerrando");
					dades.setOberta(false);
				});

				windowWordle.show();

			} catch (Exception e) {
				e.printStackTrace();
				alertaError("Error al abrir BuscaMines","Ha ocurrido un error al intentar abrir el juego. Intenta de nuevo.");
			}
		} else {
			alertaError("Ventana abierta", "Ya hay una partida de Wordle en curso. Ciérrala antes de abrir otra.");
		}
	}

	// Boton cerrar sesion
	public void logout(ActionEvent event) {
		try {
			VBox rootLogout = (VBox) FXMLLoader.load(getClass().getResource("Login.fxml"));
			Scene pantallaIrLogin = new Scene(rootLogout);
			MenuItem menuItem = (MenuItem) event.getSource();
			Stage window = (Stage) menuItem.getParentPopup().getOwnerWindow();
			window.setScene(pantallaIrLogin);
			window.setTitle("Login");
			window.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Funcio de les alertes
	public void alertaError(String camp, String error) {
		Alert alerta = new Alert(AlertType.WARNING);
		alerta.setTitle("Error abriendo un juego. ");
		alerta.setHeaderText("Error en el '" + camp + "'. ");
		alerta.setContentText(error);
		alerta.showAndWait();
	}

	// Recuperar els datos del objecte usaris
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		Platform.runLater(() -> {
			Stage window = (Stage) root.getScene().getWindow();
			if (window != null) {
				this.usuariActual = (Usuaris) window.getUserData();
				if (usuariActual != null) {
					this.emailUsuari = usuariActual.getEmail(); // recuperar el email per a poder eliminar el usuari
																// segun el email
					textoBienvenido.setText("Bienvenido " + usuariActual.getNom());// Texto de bienvenida
				} else {
					System.out.println("El objecte usuari es null. ");
				}
			} else {
				System.out.println("La stage es null, no se mostra. ");
			}

		});
	}

}
