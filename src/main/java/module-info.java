@SuppressWarnings("module")
module br.com.washii {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires jbcrypt;
    requires java.datatransfer;
    requires org.kordamp.ikonli.javafx;
     requires java.net.http;

    opens br.com.washii.domain.entities to javafx.base, javafx.fxml;
    opens br.com.washii.presentation.layout to javafx.fxml;
    opens br.com.washii.presentation.screens.acesso to javafx.fxml;
    opens br.com.washii.presentation.screens.servico to javafx.fxml;
    opens br.com.washii.presentation.screens.agendamentos to javafx.fxml;
    opens br.com.washii.presentation.screens.home to javafx.fxml;
    opens br.com.washii.presentation.screens.perfil to javafx.fxml;
    opens br.com.washii.presentation.screens.relatorio to javafx.fxml;
    opens br.com.washii.presentation.components.cards to javafx.fxml;
    
    exports br.com.washii.app;
}