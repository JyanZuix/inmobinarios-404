import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Abre un formulario pequeño con los campos de una operación. */
final class FormularioInmobiliario {

    private FormularioInmobiliario() {
    }

    static VBox campo(String texto, Control entrada) {
        Label etiqueta = new Label(texto);
        etiqueta.setLabelFor(entrada);
        entrada.setMaxWidth(Double.MAX_VALUE);
        return new VBox(3, etiqueta, entrada);
    }

    static boolean mostrar(Window ventana, String titulo, Node... campos) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.initOwner(ventana);
        dialogo.setTitle(titulo);

        ButtonType aceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(aceptar, cancelar);
        dialogo.getDialogPane().setPrefWidth(590);
        dialogo.getDialogPane().setContent(new VBox(8, campos));
        return dialogo.showAndWait().orElse(cancelar) == aceptar;
    }
}
