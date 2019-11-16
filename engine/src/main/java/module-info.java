module roguelite.engine {
    requires static lombok;
    requires org.lwjgl.opengl;
    requires org.lwjgl.glfw;
    requires org.slf4j;
    requires org.joml;

    requires java.desktop;

    exports fi.jakojaannos.roguelite.engine;
    exports fi.jakojaannos.roguelite.engine.input;
    exports fi.jakojaannos.roguelite.engine.lwjgl;
    exports fi.jakojaannos.roguelite.engine.lwjgl.input;
    exports fi.jakojaannos.roguelite.engine.lwjgl.view;
    exports fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;
    exports fi.jakojaannos.roguelite.engine.utilities;
    exports fi.jakojaannos.roguelite.engine.utilities.io;
    exports fi.jakojaannos.roguelite.engine.view;
    exports fi.jakojaannos.roguelite.engine.view.rendering;
}
