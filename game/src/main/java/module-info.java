module roguelite.game {
    requires static lombok;
    requires org.slf4j;
    requires org.lwjgl.opengl;
    requires org.joml;

    requires roguelite.engine;
    requires transitive roguelite.engine.lwjgl;
    requires roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.game;
    exports fi.jakojaannos.roguelite.game.view;
    exports fi.jakojaannos.roguelite.game.data;
}