module stemplatform.stem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
opens stemplatform.stem.gui to javafx.graphics;
exports stemplatform.stem.gui;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens stemplatform.stem to javafx.fxml;
    exports stemplatform.stem;
}