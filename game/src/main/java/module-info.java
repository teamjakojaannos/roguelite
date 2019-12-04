module roguelite.game {
    requires static lombok;
    requires org.slf4j;
    requires org.lwjgl.opengl;
    requires org.joml;
    requires jsr305;

    requires roguelite.engine;
    requires roguelite.engine.utilities;
    requires roguelite.engine.ecs;
    requires transitive roguelite.engine.lwjgl;

    opens fi.jakojaannos.roguelite.game.data.resources to roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.game;
    exports fi.jakojaannos.roguelite.game.view;
    exports fi.jakojaannos.roguelite.game.data;
}