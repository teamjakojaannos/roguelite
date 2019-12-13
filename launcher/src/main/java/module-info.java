module roguelite.launcher {
    requires static lombok;
    requires org.slf4j;
    requires roguelite.game.app;

    // Make sure jlink packages the natives
    requires org.lwjgl.natives;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.opengl.natives;
}
