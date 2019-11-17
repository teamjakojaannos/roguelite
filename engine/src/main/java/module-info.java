module roguelite.engine {
    requires static lombok;
    requires org.lwjgl.opengl;
    requires org.lwjgl.glfw;
    requires org.slf4j;
    requires org.joml;

    requires roguelite.engine.utilities;

    exports fi.jakojaannos.roguelite.engine;
    exports fi.jakojaannos.roguelite.engine.input;
    exports fi.jakojaannos.roguelite.engine.view;
    exports fi.jakojaannos.roguelite.engine.view.rendering;
}
