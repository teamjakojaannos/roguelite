module roguelite.game {
    requires static lombok;
    requires org.slf4j;
    requires org.lwjgl.opengl;
    requires org.joml;

    requires roguelite.engine;
    requires transitive roguelite.engine.lwjgl;
    requires roguelite.engine.ecs;

    opens fi.jakojaannos.roguelite.game.data.resources to roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.game to roguelite.launcher;
    exports fi.jakojaannos.roguelite.game.view to roguelite.launcher;
    exports fi.jakojaannos.roguelite.game.data to roguelite.launcher;
}