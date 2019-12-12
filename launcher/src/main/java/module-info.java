module roguelite.launcher {
    requires static lombok;
    requires org.slf4j;
    requires roguelite.engine;
    requires roguelite.game;

    // Make sure jlink packages the natives
    requires org.lwjgl.natives;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.opengl.natives;
}
