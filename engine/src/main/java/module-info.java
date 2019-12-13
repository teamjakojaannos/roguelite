module roguelite.engine {
    requires static lombok;
    requires org.slf4j;
    requires org.joml;
    requires com.google.gson;

    requires roguelite.engine.utilities;

    requires transitive java.desktop;

    exports fi.jakojaannos.roguelite.engine;
    exports fi.jakojaannos.roguelite.engine.tilemap;
    exports fi.jakojaannos.roguelite.engine.input;
    exports fi.jakojaannos.roguelite.engine.view;
    exports fi.jakojaannos.roguelite.engine.content;
    exports fi.jakojaannos.roguelite.engine.content.view;
    exports fi.jakojaannos.roguelite.engine.view.rendering;
}
