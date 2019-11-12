module roguelite.engine {
    requires static lombok;
    requires org.lwjgl.opengl;
    requires org.lwjgl.glfw;
    requires org.slf4j;
    requires org.joml;

    exports fi.jakojäännös.roguelite.engine;
    exports fi.jakojäännös.roguelite.engine.input;
    exports fi.jakojäännös.roguelite.engine.lwjgl;
    exports fi.jakojäännös.roguelite.engine.lwjgl.input;
    exports fi.jakojäännös.roguelite.engine.lwjgl.view;
    exports fi.jakojäännös.roguelite.engine.lwjgl.view.rendering;
    exports fi.jakojäännös.roguelite.engine.utilities;
    exports fi.jakojäännös.roguelite.engine.utilities.io;
    exports fi.jakojäännös.roguelite.engine.view;
}
