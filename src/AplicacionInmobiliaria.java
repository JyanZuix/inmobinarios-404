import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/** Abre la ventana definida en FXML y la conecta con el sistema inmobiliario. */
public class AplicacionInmobiliaria extends Application {

    @Override
    public void start(Stage ventana) throws IOException {
        SistemaInmobiliario sistema = new SistemaInmobiliario();
        try {
            sistema.cargarTodo();
        } catch (RuntimeException error) {
            Alert aviso = new Alert(Alert.AlertType.ERROR, error.getMessage());
            aviso.setTitle("Inmobinarios");
            aviso.setHeaderText("No se pudieron cargar los datos de la inmobiliaria");
            aviso.showAndWait();
            return;
        }

        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/inmobiliaria.fxml"));
        Scene escena = new Scene(cargador.load());
        ControladorInmobiliaria controlador = cargador.getController();
        controlador.conectar(sistema);

        ventana.setTitle("Inmobinarios");
        ventana.setMinWidth(720);
        ventana.setMinHeight(300);
        ventana.setScene(escena);
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
