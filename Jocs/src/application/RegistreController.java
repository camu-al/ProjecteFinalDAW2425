package application;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javafx.scene.image.ImageView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistreController implements Initializable {

	// Variables
	@FXML
	private ImageView avatarImg;
	@FXML
	private TextField textoNomRegistre;
	@FXML
	private TextField textoCognomsRegistre;
	@FXML
	private TextField textoEmailRegistre;
	@FXML
	private TextField textoPoblacioRegistre;
	@FXML
	private TextField textoContrasenyaRegistre;
	@FXML
	private TextField textoConfirmarContrasenyaRegistre;
	@FXML
	private Button botoRegistreEnviar;
	@FXML
	private Button botoVolverInici;

	// Comprobar que ninguna funcio dona null i finalment crida a la funcio guardar
	// usuari
	@FXML
	public void registrarUsuari(ActionEvent event) throws ClassNotFoundException {
		// Variables
		String nom = textoNomRegistre.getText();
		String cognoms = textoCognomsRegistre.getText();
		String email = textoEmailRegistre.getText();
		String poblacio = textoPoblacioRegistre.getText();
		String contrasenya = textoContrasenyaRegistre.getText();
		String confirmarContrasenya = textoConfirmarContrasenyaRegistre.getText();

		// Funciones comprovacion i insert BD

		String inserirNom = nomRegistre(nom);
		String inserirCognoms = cognomsRegistre(cognoms);
		String inserirEmail = emailRegistre(email);
		String inserirPoblacio = poblacioRegistre(poblacio);
		String inserirContrasenya = contrasenyaRegistre(contrasenya, confirmarContrasenya);

		if (inserirNom == null || inserirCognoms == null || inserirEmail == null || inserirPoblacio == null
				|| inserirContrasenya == null) {
			return;
		}

		byte[] salt = crearSalt();
		String inserirContrasenyaHash = crearHashSalt(contrasenya, salt);
		String inserirSalt = Base64.getEncoder().encodeToString(salt);

		if (inserirContrasenyaHash == null || inserirSalt == null) {
			return;
		}

		try {
			guardarUsuari(inserirNom, inserirCognoms, inserirEmail, inserirPoblacio, inserirContrasenyaHash,
					inserirSalt);

			obrirLogin(event);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Alertes de errors
	public void alertaError(String camp, String error) {
		Alert alerta = new Alert(AlertType.WARNING);
		alerta.setTitle("Error en el registro del usuari ");
		alerta.setHeaderText("Error en el '" + camp + "'. ");
		alerta.setContentText(error);
		alerta.showAndWait();
	}

	// Comprobar parametros del usuari antes del registre
	public String nomRegistre(String nom) {
		nom = textoNomRegistre.getText();
		if (!nom.isBlank()) {
			if (nom.matches("^[a-zA-Z_-]{3,15}$")) { // no pot tindre numeros
			} else {
				alertaError("Nom ", "El formato no es valid. ");
				return null;
			}
		} else {
			alertaError("Nom ", "No pot estar en blanc. ");
			return null;
		}
		return nom;
	}

	public String cognomsRegistre(String cognoms) {
		cognoms = textoCognomsRegistre.getText();
		if (!cognoms.isBlank()) {
			if (cognoms.matches(
					"^[a-zA-ZñÑçÇáÁéÉíÍóÓúÚüÜàèìòùÀÈÌÒÙâêîôûÂÊÎÔÛ]+(?:\\s[a-zA-ZñÑçÇáÁéÉíÍóÓúÚüÜàèìòùÀÈÌÒÙâêîôûÂÊÎÔÛ]+){0,2}$")) {
			} else {
				alertaError("Cognoms ", "Formato invalid. ");
				return null;
			}
		} else {
			alertaError("Cognoms ", "No pot estar en blanc. ");
			return null;
		}
		return cognoms;
	}

	public String emailRegistre(String email) throws ClassNotFoundException {
		email = textoEmailRegistre.getText();
		if (!email.isBlank()) {
			if (email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { 
			} else {
				alertaError("Email ", "Formato invalid. ");
				return null;
			}
		} else {
			alertaError("Email ", "No pot estar en blanc.  ");
			return null;
		}

		// Si el email no esta en la base de datos la guarda

		try {
			// Carregar el controlador per a la BD
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Establir la connexió
			String urlBaseDades = "jdbc:mysql://localhost:3306/cal";
			String usuari = "root";
			String contrasenya = "";

			Connection c = DriverManager.getConnection(urlBaseDades, usuari, contrasenya);

			String sentencia = "SELECT email FROM usuarios WHERE email = ?"; // Consulta si email no esta en la tabla
			PreparedStatement s = c.prepareStatement(sentencia);
			s.setString(1, email);
			ResultSet r = s.executeQuery();

			if (r.next()) {
				alertaError("Email ", "Ja esta registreat. ");
				return null;
			} else {
				return email;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String poblacioRegistre(String poblacio) {
		poblacio = textoPoblacioRegistre.getText();
		if (!poblacio.isBlank()) {
			if (poblacio.length() > 3 && poblacio.length() <= 20) {
			} else {
				alertaError("Poblacio ", "El formato no es valid. ");
				return null;
			}
		} else {
			alertaError("Poblacio ", "No pot estar en blanc.  ");
			return null;
		}
		return poblacio;
	}

	public String contrasenyaRegistre(String contrasenya, String confirmarContrasenya) {
		if (!contrasenya.isBlank()) {
			if (contrasenya.matches("^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])\\S{8,16}$")) {
				if (contrasenya.equals(confirmarContrasenya)) {
					return contrasenya;
				} else {
					alertaError("Contraseña ", "La contraseña no coincideixen ");
					return null;
				}
			} else {
				alertaError("Contraseña ", "El formato no es valid ");
				return null;
			}
		} else {
			alertaError("Contraseña ", "No pot estar en blanc.  ");
			return null;
		}
	}

	// crear un salt de hash per a major seguretat
	public byte[] crearSalt() {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		return salt;
	}

	// generar el hash amb el salt 
	public String crearHashSalt(String contrasenya, byte[] salt) {
		String password = contrasenya;
		String contrasenyaHash = null;

		// aquests valors es poden establir per defecte, igual per a tots els hash
		int fortalesa = 65536; // iteracions que realitzarà l'algorisme
		int longitudHash = 64 * 8; // quantitat de bytes * 8

		// Procés de Hash
		try {
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, fortalesa, longitudHash);
			// Obtenim algoritme PBKDF2WithHmacSHA
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			// generem el hash, un array de bytes
			byte[] hash = factory.generateSecret(spec).getEncoded();
			System.out.println("Contrasenya inicial: " + password);
			// pasem el array de bytes en String
			contrasenyaHash = Base64.getEncoder().encodeToString(hash);
			System.out.println("Hash Contrasenya inicial:\n" + contrasenyaHash);
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
		} catch (InvalidKeySpecException e) {
			System.out.println(e);
		}
		return contrasenyaHash;
	}

	// Añadir el usuari a la base de datos
	// guardar usuari en la base de datos
	public String guardarUsuari(String inserirNom, String inserirCognoms, String inserirEmail, String inserirPoblacio,
			String inserirContrasenyaHash, String inserirSalt) throws ClassNotFoundException {
		try {

			// Carregar el controlador per a la BD
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Establir la connexió
			String urlBaseDades = "jdbc:mysql://localhost:3306/cal";
			String usuari = "root";
			String contrasenya = "";

			Connection c = DriverManager.getConnection(urlBaseDades, usuari, contrasenya);

			String sentencia = "INSERT INTO usuarios (nom, cognoms, email, poblacio, contasenyaHash, salt) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(sentencia);

			s.setString(1, inserirNom);
			s.setString(2, inserirCognoms);
			s.setString(3, inserirEmail);
			s.setString(4, inserirPoblacio);
			s.setString(5, inserirContrasenyaHash);
			s.setString(6, inserirSalt);

			System.out.println("Executant: " + sentencia);
			int res = s.executeUpdate();

			if (res != 0) {
				System.out.println("Usuari inserit correctament");
			} else {
				alertaError("Registre ", "No se a registrat correctament el usuari");
				return null;
			}

			s.close();
			c.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Boto registrar envia a la escena login
	@FXML
	public void obrirLogin(ActionEvent event) {
		try {
			VBox rootMenu = (VBox) FXMLLoader.load(getClass().getResource("Login.fxml"));
			Scene pantallaMenu = new Scene(rootMenu);
			Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
			pantallaMenu.getStylesheets().add(getClass().getResource("login.css").toExternalForm());
			String nom = textoNomRegistre.getText();
			window.setUserData(nom);
			window.setScene(pantallaMenu);
			window.setTitle("Login");
			window.show();
	        finestraOberta.getInstancia().setOberta(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Boto registrar envia a la escena login
	@FXML
	public void logout(ActionEvent event) {
		try {
			VBox rootMenu = (VBox) FXMLLoader.load(getClass().getResource("Login.fxml"));
			Scene pantallaMenu = new Scene(rootMenu);
			Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
			pantallaMenu.getStylesheets().add(getClass().getResource("login.css").toExternalForm());
			String nom = textoNomRegistre.getText();
			window.setUserData(nom);
			window.setScene(pantallaMenu);
			window.setTitle("Login");
			window.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

}
